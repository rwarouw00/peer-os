package myxul.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.stage.FileChooser;
import myxul.XulWindow;

/**
 * Files for scripts (Leafpad XUL calls it NativeFileService): the system's
 * open and save dialogs, and reading and writing text as UTF-8. Scripts can't
 * reach java.io themselves (see XulScriptManager's class filter), so this is
 * their only way to files.
 */
public class FileService implements XulService {

	/** Answers the open and save dialogs instead of showing them: for tests. */
	public interface Chooser {
		/** The path chosen, or null for Cancel. save is true for a save dialog. */
		String choose(boolean save, String suggestedName);
	}

	private static volatile Chooser chooser;

	/** Replaces the open and save dialogs (null puts the real ones back). */
	public static void setChooser(Chooser replacement) {
		chooser = replacement;
	}

	private final XulWindow window;
	private File lastFolder;

	public FileService(XulWindow window) {
		this.window = window;
	}

	/** Shows an open dialog. Returns the chosen file's path, or null. */
	public String showOpenDialog() {
		Chooser c = chooser;
		if (c != null) {
			return c.choose(false, null);
		}
		FileChooser dialog = dialog("Open");
		File file = dialog.showOpenDialog(window.getStage());
		return remember(file);
	}

	/** Shows a save dialog, starting with the given file name. Returns the chosen path, or null. */
	public String showSaveDialog(String suggestedName) {
		Chooser c = chooser;
		if (c != null) {
			return c.choose(true, suggestedName);
		}
		FileChooser dialog = dialog("Save As");
		if (suggestedName != null && !suggestedName.isEmpty()) {
			dialog.setInitialFileName(suggestedName);
		}
		File file = dialog.showSaveDialog(window.getStage());
		return remember(file);
	}

	private FileChooser dialog(String title) {
		FileChooser dialog = new FileChooser();
		dialog.setTitle(title);
		if (lastFolder != null && lastFolder.isDirectory()) {
			dialog.setInitialDirectory(lastFolder);
		}
		dialog.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All files", "*"),
				new FileChooser.ExtensionFilter("Text files", "*.txt"));
		return dialog;
	}

	private String remember(File file) {
		if (file == null) {
			return null;
		}
		lastFolder = file.getParentFile();
		return file.getAbsolutePath();
	}

	/** The file's text, read as UTF-8. Throws an IOException whose message says why it can't be read. */
	public String readFile(String path) throws IOException {
		try {
			return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw readable(e, path);
		}
	}

	/** Writes the text as UTF-8, replacing the file. Throws an IOException whose message says why it can't be written. */
	public void writeFile(String path, String text) throws IOException {
		try {
			Files.write(Paths.get(path), text.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw readable(e, path);
		}
	}

	/** Java's file exceptions give only the path as their message; scripts show e.message to people. */
	private static IOException readable(IOException e, String path) {
		Path parent = Paths.get(path).toAbsolutePath().getParent();
		String why;
		if (e instanceof NoSuchFileException) {
			why = parent != null && !Files.isDirectory(parent) ? "the folder " + parent + " doesn't exist" : "there is no such file";
		} else if (e instanceof AccessDeniedException) {
			why = "permission denied";
		} else if (e instanceof FileSystemException && ((FileSystemException) e).getReason() != null) {
			why = ((FileSystemException) e).getReason();
		} else {
			why = e.getMessage() == null ? e.toString() : e.getMessage();
		}
		return new IOException(why, e);
	}

	public boolean exists(String path) {
		return Files.isRegularFile(Paths.get(path));
	}

	/** "notes.txt" for "/home/sam/notes.txt". */
	public String getFileName(String path) {
		Path name = Paths.get(path).getFileName();
		return name == null ? path : name.toString();
	}

	/**
	 * A file of this app's own, kept between runs: ~/.myxul/<app>/<name>,
	 * where <app> is the folder the window's .xul file is in (apps/notes/
	 * gives "notes"). The folder is made if it isn't there. -Dmyxul.data=...
	 * puts the data somewhere other than ~/.myxul.
	 */
	public String getDataFile(String name) throws IOException {
		if (name.isEmpty() || name.contains("/") || name.contains("\\") || name.startsWith(".")) {
			throw new IOException("\"" + name + "\" isn't a plain file name");
		}
		Path base = Paths.get(System.getProperty("myxul.data", System.getProperty("user.home") + File.separator + ".myxul"));
		Path folder = base.resolve(window.getFile().getParent().getFileName().toString());
		Files.createDirectories(folder);
		return folder.resolve(name).toString();
	}

	/** An absolute path, for a path relative to the folder myxul was started in. */
	public String resolve(String path) {
		return Paths.get(path).toAbsolutePath().normalize().toString();
	}
}
