package skylight;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hecl.Command;
import org.hecl.HeclException;
import org.hecl.Interp;
import org.hecl.ListThing;
import org.hecl.ObjectThing;
import org.hecl.Thing;
import org.hecl.java.HeclJavaCmd;
import org.hecl.java.NullCmd;

/**
 * An app window's Hecl script. Each window has its own Hecl interpreter, with
 * the variable app (the App API) and one variable per id in the window's .xul
 * file, each holding the Java object: $note setText hello. A command="save"
 * in the .xul calls the script's proc save.
 *
 * Everything runs on Swing's event thread: the script, its commands, and the
 * Swing events it binds with the bind command. (Each Hecl interpreter also has
 * its own thread, for Hecl's timers; the commands that use it are removed.)
 */
final class Script {

	/** Hecl's commands that would end Skylight, or run Hecl away from Swing's thread. */
	private static final String[] REMOVED = { "exit", "after", "twait", "tnotify", "bgerror" };

	private final App app;
	private final Interp hecl;
	private final Set<String> builtIn = new HashSet<>();

	Script(App app, Map<String, Object> ids) throws HeclException {
		this.app = app;
		hecl = new Interp();
		HeclJavaCmd.load(hecl);   // the java command, and calling methods on Java objects
		NullCmd.load(hecl);       // [null], Java's null
		hecl.addCommand("bind", new Bind());
		for (String name : REMOVED) {
			hecl.removeCommand(name);
		}
		for (Thing name : ListThing.getArray(hecl.eval(new Thing("intro commands")))) {
			builtIn.add(name.toString());
		}
		for (Map.Entry<String, Object> id : ids.entrySet()) {
			hecl.setVar(id.getKey(), ObjectThing.create(id.getValue()), 0);
		}
		hecl.setVar("app", ObjectThing.create(app), 0);
	}

	/** Runs the script file. */
	void run(Path file) throws IOException, HeclException {
		String name = Storage.HOME.relativize(file).toString().replace('\\', '/');
		String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
		try {
			hecl.eval(new Thing(text));
		} catch (HeclException e) {
			throw new ScriptError(name + ": " + describe(e));
		}
	}

	/**
	 * Runs a command from the .xul file: a proc's name, with any arguments
	 * (command="save", command="choose 3"). The first word must be one of the
	 * script's procs.
	 */
	void call(String command) throws HeclException {
		String proc = command.trim().split("\\s+")[0];
		if (!hecl.commandExists(proc) || builtIn.contains(proc)) {
			throw new ScriptError("The script has no proc " + proc + ".");
		}
		eval(command);
	}

	/** Runs Hecl given to the App API (a callback such as "clearNote") and returns its result. */
	String eval(String script) throws HeclException {
		try {
			return hecl.eval(new Thing(script)).toString();
		} catch (HeclException e) {
			throw new ScriptError(describe(e));
		}
	}

	/** Runs Hecl and returns whether its result is true (1, true, yes, on, or nothing). */
	boolean test(String script) throws HeclException {
		String result = eval(script).trim();
		return result.isEmpty() || result.equals("1") || result.equalsIgnoreCase("true")
				|| result.equalsIgnoreCase("yes") || result.equalsIgnoreCase("on");
	}

	/** Stops the interpreter's thread when the window closes. */
	void dispose() {
		hecl.terminate();
	}

	/**
	 * bind object eventMethod script: runs the Hecl script (at the top level,
	 * with the variable event holding the event) each time the object's
	 * listener method of that name is called, such as actionPerformed or
	 * hyperlinkUpdate. Skylight finds the object's addXxxListener method whose
	 * listener has that method.
	 */
	private final class Bind implements Command {
		@Override
		public Thing cmdCode(Interp interp, Thing[] argv) throws HeclException {
			if (argv.length != 4) {
				throw HeclException.createWrongNumArgsException(argv, 1, "object eventMethod script");
			}
			Object target = ObjectThing.get(argv[1]);
			String event = argv[2].toString();
			Thing script = new Thing(argv[3].toString());
			for (Method add : target.getClass().getMethods()) {
				Class<?>[] params = add.getParameterTypes();
				if (add.getName().startsWith("add") && add.getName().endsWith("Listener")
						&& params.length == 1 && params[0].isInterface() && hasMethod(params[0], event)) {
					Object listener = listener(params[0], event, script);
					try {
						add.invoke(target, listener);
					} catch (ReflectiveOperationException e) {
						throw new HeclException("bind: " + e);
					}
					return ObjectThing.create(listener);
				}
			}
			throw new HeclException("bind: " + target.getClass().getName() + " has no listener with the method " + event);
		}

		private boolean hasMethod(Class<?> type, String name) {
			for (Method method : type.getMethods()) {
				if (method.getName().equals(name)) {
					return true;
				}
			}
			return false;
		}

		/** A listener whose method event runs the script; its other methods do nothing. */
		private Object listener(Class<?> type, String event, Thing script) {
			InvocationHandler handler = (proxy, method, args) -> {
				if (method.getDeclaringClass() == Object.class) {
					switch (method.getName()) {
					case "equals":
						return proxy == args[0];
					case "hashCode":
						return System.identityHashCode(proxy);
					default:
						return "bind " + event;
					}
				}
				if (method.getName().equals(event)) {
					app.desktop.runScript(app, () -> {
						hecl.setVar("event", ObjectThing.create(args == null ? null : args[0]), 0);
						try {
							hecl.eval(script, 0);
						} catch (HeclException e) {
							throw new ScriptError(describe(e));
						}
					});
				}
				return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
			};
			return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
		}
	}

	/**
	 * Hecl's message for an error, and where it happened: "Variable x does not
	 * exist (line 3, in save at line 1)". A HeclException holds a stack of
	 * {ERROR message line}, then {command line} for each command it came
	 * through on the way out.
	 */
	private static String describe(HeclException e) {
		StringBuilder text = new StringBuilder(e.getMessage() != null ? e.getMessage() : e.toString());
		try {
			Thing[] stack = ListThing.getArray(e.getStack());
			Thing[] error = ListThing.getArray(stack[0]);
			StringBuilder where = new StringBuilder();
			if (error.length > 2 && !error[2].toString().isEmpty()) {
				where.append("line ").append(error[2]);
			}
			for (int i = 1; i < stack.length; i++) {
				Thing[] frame = ListThing.getArray(stack[i]);
				if (!frame[0].toString().equals("throw")) {   // throw itself isn't news
					where.append(where.length() > 0 ? ", " : "").append("in ").append(commandName(frame[0]))
							.append(" at line ").append(frame[1]);
				}
			}
			if (where.length() > 0) {
				text.append(" (").append(where).append(")");
			}
		} catch (HeclException | RuntimeException ignored) {
			// the message alone, then
		}
		return text.toString();
	}

	/** A command's name, or for a method call on a Java object ($label setText), the object's class: a JLabel. */
	private static String commandName(Thing word) throws HeclException {
		if (word.getVal() instanceof ObjectThing) {
			Object object = ObjectThing.get(word);
			if (object == null) {
				return "null";
			}
			Class<?> type = object.getClass();
			while (type.getSimpleName().isEmpty()) {   // Luxor's label is an anonymous JLabel
				type = type.getSuperclass();
			}
			return "a " + type.getSimpleName();
		}
		return word.toString();
	}

	/** An error from a script, with Hecl's own message. */
	static final class ScriptError extends HeclException {
		ScriptError(String message) {
			super(message);
		}
	}

	/** A short message for an error from a script. */
	static String describe(Throwable error) {
		return error.getMessage() != null ? error.getMessage() : error.toString();
	}
}
