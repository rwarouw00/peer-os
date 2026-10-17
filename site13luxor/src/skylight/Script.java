package skylight;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * An app window's Python (Jython 2.1) script. Each window has its own
 * interpreter, with the variable app (the App API) and one variable per id in
 * the window's .xul file. A command="save" in the .xul calls the script's
 * function save().
 */
final class Script {

	private final PythonInterpreter python = new PythonInterpreter();

	/** Sets Jython up. Call once, before any script runs. */
	static void init() {
		Properties options = new Properties();
		options.setProperty("python.cachedir", Storage.DATA.resolve("jython-cache").toString());
		options.setProperty("python.verbose", "warning");   // no "processing new jar" messages
		PythonInterpreter.initialize(System.getProperties(), options, new String[0]);
	}

	Script(App app, Map<String, Object> ids) {
		for (Map.Entry<String, Object> id : ids.entrySet()) {
			python.set(id.getKey(), id.getValue());
		}
		python.set("app", app);
	}

	/** Runs the script file. */
	void run(Path file) throws IOException {
		try (InputStream in = Files.newInputStream(file)) {
			python.execfile(in, Storage.HOME.relativize(file).toString().replace('\\', '/'));
		}
	}

	/** Calls one of the script's functions, by name. */
	void call(String function) {
		PyObject f = python.get(function);
		if (!(f instanceof PyFunction)) {   // missing, or only a control with that id
			throw new IllegalStateException("The script has no function " + function + "().");
		}
		f.__call__();
	}

	/** Calls a Python function given to the App API, and returns whether its result is true. */
	static boolean callback(PyObject f) {
		PyObject result = f.__call__();
		return result == null || result.__nonzero__();
	}

	/** A short message for a Python error: its last lines, as Jython prints them. */
	static String describe(Throwable error) {
		if (error instanceof PyException) {
			String text = error.toString().trim();
			String[] lines = text.split("\n");
			return lines.length > 2 ? lines[lines.length - 2].trim() + " " + lines[lines.length - 1].trim() : text;
		}
		return error.getMessage() != null ? error.getMessage() : error.toString();
	}
}
