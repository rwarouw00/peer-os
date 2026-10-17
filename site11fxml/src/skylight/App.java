package skylight;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Base class for Skylight OS apps. Most apps need no Java: an app.fxml file
 * describes the window, and scripts in it call this API through the variable
 * app (see FxmlApp). An app can also extend App in Java and return its window's
 * contents from build(), with or without FXML (loadFxml).
 */
public abstract class App {

	/** The result of writeFile(): ok, or an error message for the user. */
	public static final class Result {
		public final boolean ok;
		public final String error;

		Result(String error) {
			this.ok = error == null;
			this.error = error;
		}
	}

	protected Desktop desktop;
	protected String appID;
	protected String path;                 // e.g. "apps/terminal/"
	protected Properties manifest;         // app.properties
	protected Stage window;                // this app's window
	protected Map<String, String> args;   // values passed to openWindow()
	private BooleanSupplier closeCheck;    // set with setCanClose()

	final void init(Desktop desktop, String appID, String path, Properties manifest, Stage window, Map<String, String> args) {
		this.desktop = desktop;
		this.appID = appID;
		this.path = path;
		this.manifest = manifest;
		this.window = window;
		this.args = args;
	}

	/** Returns the contents of the window. Called once, before the window is shown. */
	protected abstract Parent build();

	/**
	 * Return false to keep the window open, e.g. to ask about unsaved work first.
	 * Java apps override this; FXML apps call setCanClose() instead.
	 */
	protected boolean canClose() {
		return closeCheck == null || closeCheck.getAsBoolean();
	}

	/** What canClose() should ask, e.g. app.setCanClose(function () { return !changed; }) in a script. */
	public void setCanClose(BooleanSupplier check) {
		closeCheck = check;
	}

	/**
	 * Loads an FXML file from this app's folder. In it, the variable app is this
	 * App, and this App is the controller, so onAction="#save" calls a method
	 * save() here and fx:id="note" fills an @FXML field called note.
	 */
	protected Parent loadFxml(String name) throws IOException {
		FXMLLoader loader = new FXMLLoader(appFile(name).toUri().toURL());
		loader.getNamespace().put("app", this);
		loader.setController(this);
		return loader.load();
	}

	// ---- Windows ----

	public String openWindow(String path) {
		return desktop.openWindow(path, Collections.<String, String>emptyMap());
	}

	public String openWindow(String path, Map<String, String> args) {
		return desktop.openWindow(path, args);
	}

	public void closeWindow() {
		desktop.closeWindow(appID);
	}

	public void closeWindow(String appID) {
		desktop.closeWindow(appID);
	}

	// ---- Dialogs and notifications ----

	/** Shows a message with an OK button. */
	public void openDialog(String message) {
		desktop.openDialog(message, null, window);
	}

	/** Asks with OK and Cancel; onOK runs only if the user clicks OK. */
	public void openDialog(String message, Runnable onOK) {
		desktop.openDialog(message, onOK, window);
	}

	public void postNotification(String header) {
		desktop.notify(header, "");
	}

	public void postNotification(String header, String body) {
		desktop.notify(header, body == null ? "" : body);
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

	/** The values passed to openWindow(path, args); an empty map if none. */
	public Map<String, String> getArgs() {
		return args;
	}

	/** This app's window. */
	public Stage getWindow() {
		return window;
	}

	// ---- Files ----

	/** A file in this app's folder, such as appFile("icons/16.png"). */
	public Path appFile(String name) {
		return Storage.HOME.resolve(path).resolve(name);
	}

	/** The names of the user's own files. */
	public List<String> listFiles() {
		return Storage.listFiles(getUsername());
	}

	/** One of the user's files as text, or null if there's no such file. */
	public String readFile(String name) {
		if (Storage.fileNameError(name) != null) {
			return null;
		}
		Path file = Storage.userFile(getUsername(), name);
		try {
			return Files.isRegularFile(file) ? new String(Files.readAllBytes(file), StandardCharsets.UTF_8) : null;
		} catch (IOException e) {
			return null;
		}
	}

	/** Saves text to one of the user's files. */
	public Result writeFile(String name, String contents) {
		String error = Storage.fileNameError(name);
		if (error != null) {
			return new Result(error);
		}
		Path file = Storage.userFile(getUsername(), name);
		try {
			Files.createDirectories(file.getParent());
			Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
			return new Result(null);
		} catch (IOException e) {
			return new Result("The file could not be saved.");
		}
	}
}
