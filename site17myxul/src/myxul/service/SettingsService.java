package myxul.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import myxul.Main;
import myxul.XulWindow;

/**
 * myxul's own settings, shared by every app (scripts call it Settings): the
 * background picture (the launcher's and the desktop's), how long
 * notifications stay on screen, and the desktop's shortcuts, kept in
 * ~/.myxul/settings.properties (or in the folder -Dmyxul.data gives).
 * Windows can ask to hear about changes, so the launcher repaints its
 * background as soon as Settings saves a new one.
 */
public class SettingsService implements XulService {

	/** The pictures the launcher can show, relative to the folder myxul.jar is in. */
	public static final List<String> BACKGROUNDS = Arrays.asList(
			"images/backgrounds/1.jpg", "images/backgrounds/2.jpg", "images/backgrounds/3.jpg",
			"images/backgrounds/4.jpg", "images/backgrounds/5.jpg", "images/backgrounds/6.jpg");

	static final String DEFAULT_BACKGROUND = "images/backgrounds/3.jpg";
	static final int DEFAULT_NOTIFICATION_TIME = 7000;
	static final String DEFAULT_SHORTCUTS = "leafpad 24 16;terminal 24 116;web-browser 24 216";

	private static final List<SettingsService> LISTENING = new ArrayList<>();

	private ScriptObjectMirror onChange;

	public SettingsService(XulWindow window) {
		window.addCloseListener(() -> LISTENING.remove(this));
	}

	static Path file() {
		Path base = Paths.get(System.getProperty("myxul.data", System.getProperty("user.home") + "/.myxul"));
		return base.resolve("settings.properties");
	}

	static Properties load() {
		Properties p = new Properties();
		Path file = file();
		if (Files.exists(file)) {
			try (InputStream in = Files.newInputStream(file)) {
				p.load(in);
			} catch (IOException e) {
				// unreadable settings: use the defaults
			}
		}
		return p;
	}

	/** The picture behind the launcher, such as images/backgrounds/3.jpg. */
	public String getBackground() {
		String b = load().getProperty("background", DEFAULT_BACKGROUND);
		return BACKGROUNDS.contains(b) ? b : DEFAULT_BACKGROUND;
	}

	/** The background as a URL a style can use: background-image: url(...). */
	public String getBackgroundURL() {
		return Main.home().resolve(getBackground()).toAbsolutePath().toUri().toString();
	}

	public List<String> getBackgrounds() {
		return BACKGROUNDS;
	}

	/** How long notifications stay on screen, in milliseconds. */
	public int getNotificationTime() {
		return notificationTime();
	}

	static int notificationTime() {
		try {
			int ms = Integer.parseInt(load().getProperty("notificationTime", String.valueOf(DEFAULT_NOTIFICATION_TIME)));
			return ms >= 1000 && ms <= 60000 ? ms : DEFAULT_NOTIFICATION_TIME;
		} catch (NumberFormatException e) {
			return DEFAULT_NOTIFICATION_TIME;
		}
	}

	/** Saves a new background. Throws if it isn't one of the pictures. */
	public void setBackground(String background) throws IOException {
		if (!BACKGROUNDS.contains(background)) {
			throw new IllegalArgumentException(background + " isn't one of the backgrounds.");
		}
		save("background", background);
	}

	/** Saves how long notifications stay, in milliseconds: 1 to 60 seconds. */
	public void setNotificationTime(double ms) throws IOException {
		if (ms != Math.rint(ms) || ms < 1000 || ms > 60000) {
			throw new IllegalArgumentException("Notifications can stay for 1 to 60 seconds.");
		}
		save("notificationTime", String.valueOf((int) ms));
	}

	/**
	 * The desktop's shortcuts: "app x y" for each, separated by ";", where app
	 * is a folder in apps/ (such as "terminal 24 116;settings 24 216").
	 */
	public String getShortcuts() {
		String shortcuts = load().getProperty("shortcuts", DEFAULT_SHORTCUTS);
		return checkShortcuts(shortcuts) == null ? shortcuts : DEFAULT_SHORTCUTS;
	}

	/** Saves the desktop's shortcuts. Throws if one names no app, or is badly written. */
	public void setShortcuts(String shortcuts) throws IOException {
		String problem = checkShortcuts(shortcuts);
		if (problem != null) {
			throw new IllegalArgumentException(problem);
		}
		save("shortcuts", shortcuts);
	}

	/** Null if the shortcuts are well written and name apps that exist, or else what's wrong. */
	static String checkShortcuts(String shortcuts) {
		if (shortcuts == null) {
			return "No shortcuts were given.";
		}
		if (shortcuts.isEmpty()) {
			return null;
		}
		List<String> seen = new ArrayList<>();
		String[] all = shortcuts.split(";", -1);
		if (all.length > 50) {
			return "The desktop can have 50 shortcuts at most.";
		}
		for (String shortcut : all) {
			if (!shortcut.matches("[a-z0-9-]+ \\d{1,5} \\d{1,5}")) {
				return "\"" + shortcut + "\" isn't a shortcut: write the app's folder, then its x and y, such as \"terminal 24 116\".";
			}
			String app = shortcut.split(" ")[0];
			if (!Files.isRegularFile(Main.home().resolve("apps").resolve(app).resolve("app.properties"))) {
				return "There is no app \"" + app + "\" in apps/.";
			}
			if (seen.contains(app)) {
				return app + " has two shortcuts.";
			}
			seen.add(app);
		}
		return null;
	}

	/** Calls the function (with no arguments) whenever any window saves a setting. */
	public void setOnChange(Object function) {
		onChange = function instanceof ScriptObjectMirror && ((ScriptObjectMirror) function).isFunction() ? (ScriptObjectMirror) function : null;
		if (onChange != null && !LISTENING.contains(this)) {
			LISTENING.add(this);
		}
	}

	private static void save(String key, String value) throws IOException {
		Properties p = load();
		p.setProperty(key, value);
		Path file = file();
		Files.createDirectories(file.getParent());
		try (OutputStream out = Files.newOutputStream(file)) {
			p.store(out, "myxul settings");
		}
		for (SettingsService s : new ArrayList<>(LISTENING)) {
			if (s.onChange != null) {
				s.onChange.call(null);
			}
		}
	}
}
