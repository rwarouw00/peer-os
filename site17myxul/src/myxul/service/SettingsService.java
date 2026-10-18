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
 * launcher's background picture and how long notifications stay on screen,
 * kept in ~/.myxul/settings.properties (or in the folder -Dmyxul.data gives).
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
