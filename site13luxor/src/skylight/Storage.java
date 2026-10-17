package skylight;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Users, settings and files, kept as plain files under data/. */
public final class Storage {

	/** The Skylight folder: the working directory, or -Dskylight.home=... */
	public static final Path HOME = Paths.get(System.getProperty("skylight.home", ".")).toAbsolutePath().normalize();
	public static final Path DATA = HOME.resolve("data");

	private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");
	private static final Pattern APP_PATH = Pattern.compile("apps/[a-z0-9-]+/");
	private static final Pattern BACKGROUND = Pattern.compile("images/backgrounds/[A-Za-z0-9_-]+\\.(jpg|jpeg|png)", Pattern.CASE_INSENSITIVE);
	// No slashes and no leading dot, so a name can't leave the user's folder.
	private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9_. -]{0,99}");
	private static final int ITERATIONS = 20000;

	private Storage() {
	}

	// ---- Properties files ----

	static Properties readProperties(Path file) {
		Properties properties = new Properties();
		if (Files.isRegularFile(file)) {
			try (InputStream in = Files.newInputStream(file)) {
				properties.load(in);
			} catch (IOException e) {
				// treat an unreadable file as empty
			}
		}
		return properties;
	}

	static boolean writeProperties(Path file, Properties properties, String comment) {
		try {
			Files.createDirectories(file.getParent());
			try (OutputStream out = Files.newOutputStream(file)) {
				properties.store(out, comment);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	// ---- Users ----

	public static boolean isValidUsername(String name) {
		return name != null && USERNAME.matcher(name).matches();
	}

	static Path userFolder(String user) {
		return DATA.resolve("users").resolve(user);
	}

	public static boolean checkLogin(String username, String password) {
		if (!isValidUsername(username)) {
			return false;
		}
		String stored = readProperties(DATA.resolve("users.properties")).getProperty(username);
		return stored != null && checkPassword(password, stored);
	}

	/** Adds a user or changes their password. Returns false if the file can't be written. */
	public static boolean setPassword(String username, String password) {
		Path file = DATA.resolve("users.properties");
		Properties users = readProperties(file);
		users.setProperty(username, hashPassword(password));
		return writeProperties(file, users, "Skylight OS users: name=pbkdf2-sha256$iterations$salt$hash");
	}

	// ---- Passwords: PBKDF2-SHA256, built into Java 8 ----

	static String hashPassword(String password) {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
				+ "$" + Base64.getEncoder().encodeToString(pbkdf2(password, salt, ITERATIONS));
	}

	static boolean checkPassword(String password, String stored) {
		String[] parts = stored.split("\\$");
		if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256")) {
			return false;
		}
		try {
			byte[] expected = Base64.getDecoder().decode(parts[3]);
			byte[] actual = pbkdf2(password, Base64.getDecoder().decode(parts[2]), Integer.parseInt(parts[1]));
			return MessageDigest.isEqual(expected, actual);   // constant-time comparison
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
		try {
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
		} catch (Exception e) {
			throw new IllegalStateException("PBKDF2WithHmacSHA256 is missing from this Java", e);
		}
	}

	// ---- Settings ----

	public static Settings loadSettings(String user) {
		Properties p = readProperties(userFolder(user).resolve("settings.properties"));
		Settings settings = new Settings();
		String background = p.getProperty("background");
		if (background != null && BACKGROUND.matcher(background).matches()) {
			settings.background = background;
		}
		try {
			settings.notificationTime = clampTime(Integer.parseInt(p.getProperty("notificationTime", "7000")));
		} catch (NumberFormatException e) {
			// keep the default
		}
		for (int i = 0; p.getProperty("shortcut." + i + ".path") != null; i++) {
			try {
				Settings.Shortcut shortcut = new Settings.Shortcut(p.getProperty("shortcut." + i + ".path"),
						Double.parseDouble(p.getProperty("shortcut." + i + ".x", "20")),
						Double.parseDouble(p.getProperty("shortcut." + i + ".y", "20")));
				if (isAppPath(shortcut.path)) {
					settings.shortcuts.add(shortcut);
				}
			} catch (NumberFormatException e) {
				// skip a damaged entry
			}
		}
		return settings;
	}

	/** Checks and saves settings. Returns null on success, or a message for the user. */
	public static String saveSettings(String user, Settings settings) {
		if (!BACKGROUND.matcher(settings.background).matches()) {
			return "That isn't one of the desktop backgrounds.";
		}
		if (settings.notificationTime != clampTime(settings.notificationTime)) {
			return "Notifications can stay for 1 to 60 seconds.";
		}
		if (settings.shortcuts.size() > 50) {
			return "There can be at most 50 desktop shortcuts.";
		}
		Properties p = new Properties();
		p.setProperty("background", settings.background);
		p.setProperty("notificationTime", Integer.toString(settings.notificationTime));
		for (int i = 0; i < settings.shortcuts.size(); i++) {
			Settings.Shortcut shortcut = settings.shortcuts.get(i);
			if (!isAppPath(shortcut.path)) {
				return "Bad shortcut.";
			}
			p.setProperty("shortcut." + i + ".path", shortcut.path);
			p.setProperty("shortcut." + i + ".x", Integer.toString((int) shortcut.x));
			p.setProperty("shortcut." + i + ".y", Integer.toString((int) shortcut.y));
		}
		return writeProperties(userFolder(user).resolve("settings.properties"), p, "Skylight OS settings")
				? null : "The settings could not be saved.";
	}

	private static int clampTime(int milliseconds) {
		return Math.max(1000, Math.min(60000, milliseconds));
	}

	// ---- Apps ----

	public static boolean isAppPath(String path) {
		return path != null && APP_PATH.matcher(path).matches();
	}

	/** The app folders listed in apps/apps.txt, one per line. */
	static List<String> appList() {
		try (Stream<String> lines = Files.lines(HOME.resolve("apps/apps.txt"), StandardCharsets.UTF_8)) {
			return lines.map(String::trim).filter(Storage::isAppPath).collect(Collectors.toList());
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	// ---- The user's own files (data/users/<name>/files/) ----

	static String fileNameError(String name) {
		return name != null && FILE_NAME.matcher(name).matches() ? null
				: "File names may use letters, digits, spaces, \"-\", \"_\" and \".\" (up to 100 characters).";
	}

	static Path userFile(String user, String name) {
		return userFolder(user).resolve("files").resolve(name);
	}

	static List<String> listFiles(String user) {
		Path folder = userFolder(user).resolve("files");
		List<String> names = new ArrayList<>();
		if (Files.isDirectory(folder)) {
			try (Stream<Path> files = Files.list(folder)) {
				files.filter(Files::isRegularFile).forEach(f -> names.add(f.getFileName().toString()));
			} catch (IOException e) {
				// an unreadable folder lists as empty
			}
		}
		Collections.sort(names);
		return names;
	}
}
