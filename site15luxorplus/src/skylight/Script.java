package skylight;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TCL;
import tcl.lang.TclException;

/**
 * An app window's Tcl script (Jacl 1.3.3). Each window has its own Tcl
 * interpreter, with the variable app (the App API) and one variable per id in
 * the window's .xul file, each holding the Java object: $note setText hello.
 * A command="save" in the .xul calls the script's proc save.
 *
 * Interpreters are made on Swing's event thread, so Swing events bound with
 * java::bind run straight away, on that same thread.
 */
final class Script {

	private final Interp tcl = new Interp();

	Script(App app, Map<String, Object> ids) throws TclException {
		tcl.eval("package require java");
		tcl.eval("rename exit {}");   // a script mustn't end Skylight
		for (Map.Entry<String, Object> id : ids.entrySet()) {
			Object control = id.getValue();
			tcl.setVar(id.getKey(), ReflectObject.newInstance(tcl, publicClass(control.getClass()), control), TCL.GLOBAL_ONLY);
		}
		tcl.setVar("app", ReflectObject.newInstance(tcl, App.class, app), TCL.GLOBAL_ONLY);
	}

	/** Runs the script file. */
	void run(Path file) throws IOException, TclException {
		String name = Storage.HOME.relativize(file).toString().replace('\\', '/');
		String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
		try {
			tcl.eval(text);
		} catch (TclException e) {
			throw new ScriptError(name + ": " + errorInfo());
		}
	}

	/**
	 * Runs a command from the .xul file: a proc's name, with any arguments
	 * (command="save", command="choose 3"). The first word must be one of the
	 * script's procs.
	 */
	void call(String command) throws TclException {
		String proc = command.trim().split("\\s+")[0];
		tcl.eval("info procs " + quote(proc));
		if (tcl.getResult().toString().isEmpty()) {
			throw new ScriptError("The script has no proc " + proc + ".");
		}
		eval(command);
	}

	/** Runs Tcl given to the App API (a callback such as "clearNote") and returns its result. */
	String eval(String script) throws TclException {
		try {
			tcl.eval(script);
			return tcl.getResult().toString();
		} catch (TclException e) {
			throw new ScriptError(errorInfo());
		}
	}

	/** Runs Tcl and returns whether its result is true (1, true, yes, on). */
	boolean test(String script) throws TclException {
		String result = eval(script).trim();
		return result.isEmpty() || result.equals("1") || result.equalsIgnoreCase("true")
				|| result.equalsIgnoreCase("yes") || result.equalsIgnoreCase("on");
	}

	/** The Terminal's interpreter is the window's own; this frees it when the window closes. */
	void dispose() {
		tcl.dispose();
	}

	/** Tcl's description of the last error, without its repeated lines: "invalid command name "x" while executing ...". */
	private String errorInfo() {
		try {
			String info = tcl.getVar("errorInfo", TCL.GLOBAL_ONLY).toString();
			return info.replaceAll("\\s*\\n\\s*", " ").trim();
		} catch (TclException e) {
			return tcl.getResult().toString();
		}
	}

	/**
	 * The class Tcl should see an object as: Jacl can only call methods through
	 * a public class, and Luxor makes some controls as private subclasses
	 * (a label is an anonymous JLabel), so use the nearest public one.
	 */
	private static Class<?> publicClass(Class<?> type) {
		while (!Modifier.isPublic(type.getModifiers())) {
			type = type.getSuperclass();
		}
		return type;
	}

	private static String quote(String word) {
		return "{" + word.replace("{", "").replace("}", "") + "}";
	}

	/** A Tcl error, with Tcl's own message. */
	static final class ScriptError extends TclException {
		private final String message;

		ScriptError(String message) {
			super(TCL.ERROR);
			this.message = message;
		}

		@Override
		public String getMessage() {
			return message;
		}
	}

	/** A short message for an error from a script. */
	static String describe(Throwable error) {
		return error.getMessage() != null ? error.getMessage() : error.toString();
	}
}
