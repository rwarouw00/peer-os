package myxul;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * One window's JavaScript: its own Nashorn engine, so two windows never share
 * variables. Loads bootstrap.js (document, window, alert, the element
 * wrappers), the services the XUL file asks for, then its <script> blocks.
 *
 * Scripts may use the JavaFX classes for building UI (Java.type(
 * "javafx.scene.control.TreeItem")), but no other Java class: no files,
 * processes or reflection, except through the services the window was given.
 */
final class XulScriptManager {

	/** JavaFX packages a script may name with Java.type: building UI, not reaching files or the web. */
	static final ClassFilter FILTER = name ->
			(name.startsWith("javafx.scene.") && !name.startsWith("javafx.scene.web.") && !name.startsWith("javafx.scene.media."))
			|| name.startsWith("javafx.geometry.") || name.startsWith("javafx.collections.")
			|| name.startsWith("javafx.beans.") || name.startsWith("javafx.event.")
			|| name.startsWith("javafx.animation.") || name.equals("javafx.util.Duration");

	private static final Pattern LOCATION = Pattern.compile("\\s+in\\s+.*\\s+at line number \\d+$");

	/** A compiled event attribute: function (event) { code }, called with "this" as the element. */
	final class Handler {
		private final ScriptObjectMirror function;
		private final XulElement element;
		private final String attribute;

		Handler(ScriptObjectMirror function, XulElement element, String attribute) {
			this.function = function;
			this.element = element;
			this.attribute = attribute;
		}

		/** Runs the handler. Returns its result, or null if it failed (the error is reported). */
		Object call(XulElement self, Object event) {
			try {
				return function.call(wrap(self), event);
			} catch (RuntimeException e) {
				window.scriptError(element.file, element.line, attribute, e);
				return null;
			}
		}
	}

	final ScriptEngine engine;
	private final XulWindow window;

	XulScriptManager(XulWindow window) {
		this.window = window;
		this.engine = new NashornScriptEngineFactory().getScriptEngine(new String[] { "--language=es5" },
				XulScriptManager.class.getClassLoader(), FILTER);
		engine.put("__window", window);
		engine.put("__document", window.document);
		try (Reader bootstrap = new InputStreamReader(resource("bootstrap.js"), StandardCharsets.UTF_8)) {
			engine.put(ScriptEngine.FILENAME, "bootstrap.js");
			engine.eval(bootstrap);
		} catch (IOException | ScriptException e) {
			throw new IllegalStateException("myxul's bootstrap.js failed: " + e.getMessage(), e);
		}
	}

	private static InputStream resource(String name) {
		InputStream in = XulScriptManager.class.getResourceAsStream(name);
		if (in == null) {
			throw new IllegalStateException(name + " is missing from myxul.jar");
		}
		return in;
	}

	/** Makes a Java object available to scripts as a global: a service such as NativeFileService. */
	void put(String name, Object value) {
		engine.put(name, value);
	}

	/** The script-side wrapper of an element (made once, then reused, so == works). */
	Object wrap(XulElement element) {
		if (element == null) {
			return null;
		}
		try {
			return ((Invocable) engine).invokeFunction("__wrap", element);
		} catch (ScriptException | NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Compiles an event attribute as the body of function (event) { ... }. That
	 * keeps the window's globals (its <script> functions, document, window),
	 * gives the code its event, and reports syntax errors when the window
	 * loads instead of at the first click.
	 */
	Handler handler(XulElement element, String attribute, String code) {
		String name = element.file.getFileName() + " (" + attribute + " on line " + element.line + ")";
		try {
			engine.getContext().setAttribute(ScriptEngine.FILENAME, name, ScriptContext.ENGINE_SCOPE);
			Object function = engine.eval("(function (event) {\n" + code + "\n})");
			return new Handler((ScriptObjectMirror) function, element, attribute);
		} catch (ScriptException e) {
			throw new XulException(element.file, element.line, "Script error in " + attribute + ": " + message(e), e);
		}
	}

	/** Runs a <script src="..."> file. */
	void runFile(Path file, XulElement from) {
		String code;
		try {
			code = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
		} catch (NoSuchFileException e) {
			throw new XulException(from.file, from.line, "<script src=\"" + from.attr("src") + "\">: there is no such file.");
		} catch (IOException e) {
			throw new XulException(from.file, from.line, "<script src=\"" + from.attr("src") + "\"> can't be read: " + e.getMessage());
		}
		run(code, file, 1);
	}

	/**
	 * Runs script text. Inline <script> text is padded with blank lines so
	 * that error line numbers match the .xul file.
	 */
	void run(String code, Path file, int firstLine) {
		StringBuilder padded = new StringBuilder();
		for (int i = 1; i < firstLine; i++) {
			padded.append('\n');
		}
		padded.append(code);
		try {
			engine.getContext().setAttribute(ScriptEngine.FILENAME, file.getFileName().toString(), ScriptContext.ENGINE_SCOPE);
			engine.eval(padded.toString());
		} catch (ScriptException e) {
			throw new XulException(file, e.getLineNumber(), "Script error: " + message(e), e);
		} catch (RuntimeException e) {
			int line = e instanceof NashornException ? ((NashornException) e).getLineNumber() : 0;
			throw new XulException(file, line, "Script error: " + message(e), e);
		}
	}

	/** Calls a script function given to Java (addEventListener, setTimeout) with "this" as the element. Errors are reported. */
	Object call(ScriptObjectMirror function, XulElement self, Object event) {
		try {
			return function.call(wrap(self), event);
		} catch (RuntimeException e) {
			window.runtimeError(e);
			return null;
		}
	}

	/** Calls a global function the scripts defined, if they did (onload="..." is handled as a Handler instead). */
	Object callIfDefined(String function, Object... args) {
		Object f = engine.get(function);
		if (f instanceof ScriptObjectMirror && ((ScriptObjectMirror) f).isFunction()) {
			return ((ScriptObjectMirror) f).call(null, args);
		}
		return null;
	}

	/** "ReferenceError: "x" is not defined", without Nashorn's " in file at line number n". */
	static String message(Throwable e) {
		Throwable t = e;
		if (t instanceof ScriptException && t.getCause() != null) {
			t = t.getCause();
		}
		String message = t.getMessage();
		if (message == null) {
			message = t.toString();
		}
		// A syntax error reads "file:2:8 Expected an operand but found )", then the line and a caret: keep the reason.
		message = message.split("\n")[0].replaceFirst("^.*?:\\d+:\\d+ ", "");
		Matcher m = LOCATION.matcher(message);
		message = m.find() ? message.substring(0, m.start()) : message;
		Matcher refused = Pattern.compile("ClassNotFoundException: ([\\w.$]+)").matcher(message);
		if (refused.find() && !FILTER.exposeToScripts(refused.group(1))) {
			return refused.group(1) + " isn't available to scripts: they may use JavaFX's UI classes, and Java objects given to them, such as services";
		}
		if (!(t instanceof NashornException) && !message.startsWith(t.getClass().getSimpleName())) {
			message = t.getClass().getSimpleName() + ": " + message;   // a Java exception thrown by a service
		}
		return message;
	}

	/** The line a script error happened on, within its file or attribute. */
	static int line(Throwable e) {
		Throwable t = e instanceof ScriptException && e.getCause() != null ? e.getCause() : e;
		if (t instanceof NashornException) {
			return ((NashornException) t).getLineNumber();
		}
		if (e instanceof ScriptException) {
			return ((ScriptException) e).getLineNumber();
		}
		return -1;
	}
}
