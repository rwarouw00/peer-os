package myxul;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Opens XUL files as windows. One engine runs a whole program: all its
 * windows, the tag registry, and the dialogs for alerts and errors.
 */
public final class XulEngine {

	final XulRegistry registry = new XulRegistry();
	XulDialogs dialogs = XulDialogs.JAVAFX;
	private final List<XulWindow> windows = new ArrayList<>();
	private final List<String> errors = new ArrayList<>();
	private final List<String> warnings = new ArrayList<>();

	/** Replaces the dialogs used for alert(), confirm(), prompt() and errors. */
	public void setDialogs(XulDialogs dialogs) {
		this.dialogs = dialogs;
	}

	/**
	 * Opens a .xul file in a new window. If the file or its scripts have a
	 * mistake, the user is told (with the file and line) and null is returned.
	 */
	public XulWindow openWindow(Path file, List<String> arguments) {
		XulWindow window = null;
		try {
			window = new XulWindow(this, file, arguments);
			windows.add(window);
			window.show();
			return window;
		} catch (XulException e) {
			if (window != null) {
				window.stage.close();
				windows.remove(window);
			}
			report(null, e);
			return null;
		}
	}

	public List<XulWindow> getWindows() {
		return Collections.unmodifiableList(windows);
	}

	/** Every error reported so far, as "file:line: message". */
	public List<String> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	/** Every warning so far (styles ignored and the like), as "file:line: message". */
	public List<String> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}

	/** The XUL tags this engine knows. */
	public List<String> getTags() {
		return registry.tags();
	}

	void report(XulWindow window, XulException e) {
		String message = e.toString();
		errors.add(message);
		System.err.println("myxul: " + message);
		dialogs.error(window, message);
	}

	void warn(Path file, int line, String message) {
		String text = (file == null ? "" : file.getFileName() + ":" + line + ": ") + message;
		warnings.add(text);
		System.err.println("myxul warning: " + text);
	}

	void closed(XulWindow window) {
		windows.remove(window);
	}
}
