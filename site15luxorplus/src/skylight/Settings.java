package skylight;

import java.util.ArrayList;
import java.util.List;

/** A user's settings. Change a copy, then save it with App.saveSettings(). */
public final class Settings {

	public String background = "images/backgrounds/3.jpg";
	public int notificationTime = 7000;   // milliseconds
	public List<Shortcut> shortcuts = new ArrayList<>();

	public static final class Shortcut {
		public final String path;
		public double x;
		public double y;

		public Shortcut(String path, double x, double y) {
			this.path = path;
			this.x = x;
			this.y = y;
		}
	}

	public Settings copy() {
		Settings copy = new Settings();
		copy.background = background;
		copy.notificationTime = notificationTime;
		for (Shortcut shortcut : shortcuts) {
			copy.shortcuts.add(new Shortcut(shortcut.path, shortcut.x, shortcut.y));
		}
		return copy;
	}
}
