package skylight;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.JDialog;
import tcl.lang.TclException;
import tcl.lang.TclList;
import tcl.lang.TclObject;
import tcl.lang.TclString;

/**
 * The App API: what an app's Tcl script can do, through the variable app
 * ($app postNotification Hello). One App object per window. Callbacks are
 * Tcl scripts, usually the name of a proc, run in the app's interpreter.
 * Lists come back as Tcl lists.
 */
public final class App {

	/** The result of writeFile(): ok, or an error message for the user. */
	public static final class Result {
		public final boolean ok;
		public final String error;

		Result(String error) {
			this.ok = error == null;
			this.error = error;
		}
	}

	final Desktop desktop;
	final String appID;
	final String path;                 // e.g. "apps/terminal/"
	final Properties manifest;         // app.properties
	final Map<String, String> args;   // values passed to openWindow()
	JDialog window;                    // set once the window exists
	String closeCheck;                 // set with setCanClose()
	Script script;                     // this window's Tcl interpreter

	App(Desktop desktop, String appID, String path, Properties manifest, Map<String, String> args) {
		this.desktop = desktop;
		this.appID = appID;
		this.path = path;
		this.manifest = manifest;
		this.args = args;
	}

	// ---- Windows ----

	/** Opens an app ("apps/settings/") in a new window. Returns its AppID, or null. */
	public String openWindow(String appPath) {
		return desktop.openWindow(appPath, null);
	}

	/** Opens an app with values it can read with getArgs(): $app openWindow apps/notes/ {file todo.txt}. */
	public String openWindow(String appPath, String args) throws TclException {
		Map<String, String> values = new HashMap<>();
		TclObject[] words = TclList.getElements(null, TclString.newInstance(args));
		for (int i = 0; i + 1 < words.length; i += 2) {
			values.put(words[i].toString(), words[i + 1].toString());
		}
		return desktop.openWindow(appPath, values);
	}

	/** Closes this window, without asking its setCanClose function. */
	public void closeWindow() {
		desktop.closeWindow(appID);
	}

	public void closeWindow(String otherAppID) {
		desktop.closeWindow(otherAppID);
	}

	/**
	 * Decides whether the window may close: Skylight runs the script (usually a
	 * proc's name) when the user closes the window, and keeps it open if it
	 * returns 0.
	 */
	public void setCanClose(String check) {
		closeCheck = check;
	}

	// ---- Dialogs and notifications ----

	/** A message with an OK button. */
	public void openDialog(String message) {
		desktop.openDialog(this, message, null);
	}

	/** A question with OK and Cancel; the Tcl in onOK runs only if the user clicks OK. */
	public void openDialog(String message, String onOK) {
		desktop.openDialog(this, message, onOK);
	}

	public void postNotification(String header) {
		desktop.postNotification(header, "");
	}

	public void postNotification(String header, String body) {
		desktop.postNotification(header, body);
	}

	// ---- Settings and the desktop ----

	/** A copy of the user's settings: change it, then pass it to saveSettings(). */
	public Settings getSettings() {
		return desktop.getSettings().copy();
	}

	/** Checks, saves and applies settings. Returns false (after telling the user) if they were refused. */
	public boolean saveSettings(Settings settings) {
		return desktop.saveSettings(settings);
	}

	public boolean setBackground(String imagePath) {
		Settings settings = getSettings();
		settings.background = imagePath;
		return saveSettings(settings);
	}

	public void addShortcut(String appPath) {
		desktop.addShortcut(appPath);
	}

	// ---- Identity ----

	public String getAppID() {
		return appID;
	}

	public String getUsername() {
		return desktop.getUser();
	}

	/** The values passed to openWindow(path, args), as a Tcl list of names and values: array set args [$app getArgs]. */
	public String getArgs() throws TclException {
		TclObject list = TclList.newInstance();
		for (Map.Entry<String, String> arg : args.entrySet()) {
			TclList.append(null, list, TclString.newInstance(arg.getKey()));
			TclList.append(null, list, TclString.newInstance(arg.getValue()));
		}
		return list.toString();
	}

	/** This app's window. */
	public JDialog getWindow() {
		return window;
	}

	// ---- Files ----

	/** A file in this app's folder, such as appFile("help.html"). */
	public Path appFile(String name) {
		return Storage.HOME.resolve(path).resolve(name);
	}

	/** The names of the user's own files, as a Tcl list. */
	public String listFiles() throws TclException {
		TclObject list = TclList.newInstance();
		for (String name : Storage.listFiles(getUsername())) {
			TclList.append(null, list, TclString.newInstance(name));
		}
		return list.toString();
	}

	/** One of the user's files as text; null (an empty string in Tcl) if there's no such file. */
	public String readFile(String name) {
		if (Storage.fileNameError(name) != null) {
			return null;
		}
		try {
			return new String(Files.readAllBytes(Storage.userFile(getUsername(), name)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	/** Saves one of the user's files as UTF-8 text. */
	public Result writeFile(String name, String contents) {
		String error = Storage.fileNameError(name);
		if (error != null) {
			return new Result(error);
		}
		try {
			Path file = Storage.userFile(getUsername(), name);
			Files.createDirectories(file.getParent());
			Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
			return new Result(null);
		} catch (IOException e) {
			return new Result("The file couldn't be saved: " + e.getMessage());
		}
	}
}
