package myxul;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import myxul.service.XulService;

/**
 * One window, opened from one .xul file: its Stage, its elements, and its
 * own script engine. Scripts reach it as "window" (see bootstrap.js).
 */
public final class XulWindow {

	final XulEngine engine;
	final Path file;
	final Stage stage = new Stage();
	final XulElement root;
	final XulDocument document;
	final XulScriptManager scripts;
	final Map<String, XulElement> commands = new LinkedHashMap<>();
	final Map<String, List<XulElement>> observers = new HashMap<>();
	static final String ACCESSKEY = "xul-accesskey";
	final List<javafx.scene.control.Menu> menus = new ArrayList<>();
	private char swallowTyped;   // the letter of a menu access key just handled: its KEY_TYPED mustn't type it too
	private final Map<String, XulScriptManager.Handler> commandHandlers = new HashMap<>();
	private final Map<String, XulElement> keys = new LinkedHashMap<>();
	private final List<XulElement> scriptElements = new ArrayList<>();
	private final List<String> arguments;
	private final Map<Integer, Animation> timers = new HashMap<>();
	private final Map<String, XulService> services = new LinkedHashMap<>();
	private final List<Runnable> closeListeners = new ArrayList<>();
	private int nextTimer = 1;
	private boolean closed;

	XulWindow(XulEngine engine, Path file, List<String> arguments) {
		this.engine = engine;
		this.file = file.toAbsolutePath().normalize();
		this.arguments = new ArrayList<>(arguments);
		XulParser.Parsed parsed = XulParser.parse(this.file);
		root = parsed.root;
		if (!root.tag.equals("window") && !root.tag.equals("dialog")) {
			throw root.error("A XUL file's root must be <window> or <dialog>, not <" + root.tag + ">.");
		}
		if (!XulParser.XUL_NS.equals(root.namespace)) {
			engine.warn(this.file, root.line, "<" + root.tag + "> should have xmlns=\"" + XulParser.XUL_NS + "\".");
		}
		document = new XulDocument(root);
		findNonVisual(root);
		scripts = new XulScriptManager(this);
		for (String[] pi : parsed.instructions) {
			if (pi[0].equals("xul-service")) {
				addService(pi[1], Integer.parseInt(pi[2]));
			}
		}
		for (XulElement command : commands.values()) {
			String code = command.attr("oncommand");
			if (code != null) {
				commandHandlers.put(command.getId(), scripts.handler(command, "oncommand", code));
			}
			command.window = this;
		}

		Parent content = (Parent) build(root);
		bindKeys();
		menuAccessKeys();
		Scene scene = new Scene(content, root.number("width", -1), root.number("height", -1));
		scene.getStylesheets().add(XulWindow.class.getResource("myxul.css").toExternalForm());
		stage.setScene(scene);
		stage.setTitle(root.attr("title", file.getFileName().toString()));

		// Scripts run once the window is built, so getElementById finds everything; then onload.
		for (XulElement script : scriptElements) {
			if (script.attributes.containsKey("src")) {
				scripts.runFile(this.file.resolveSibling(script.attr("src")), script);
			} else {
				scripts.run(script.text, this.file, script.line);
			}
		}

		XulScriptManager.Handler onclose = handler("onclose");
		XulScriptManager.Handler onunload = handler("onunload");
		stage.setOnCloseRequest(ev -> {
			if (onclose != null && Boolean.FALSE.equals(onclose.call(root, new XulEvent("close", stage)))) {
				ev.consume();   // onclose returned false: stay open
			}
		});
		stage.setOnHidden(ev -> {
			if (onunload != null) {
				onunload.call(root, new XulEvent("unload", stage));
			}
			closed();
		});
	}

	/** Shows the window, then runs onload. */
	void show() {
		stage.show();
		XulScriptManager.Handler onload = handler("onload");
		if (onload != null) {
			onload.call(root, new XulEvent("load", stage));
		}
	}

	private XulScriptManager.Handler handler(String attribute) {
		String code = root.attr(attribute);
		return code == null ? null : scripts.handler(root, attribute, code);
	}

	/** Commands, keys and scripts, found before building so commands can be referenced from anywhere. */
	private void findNonVisual(XulElement e) {
		switch (e.tag) {
		case "command":
			if (e.attr("id") == null) {
				throw e.error("<command> needs an id.");
			}
			commands.put(e.getId(), e);
			break;
		case "key":
			if (e.attr("id") != null) {
				keys.put(e.getId(), e);
			} else {
				keys.put("#" + keys.size(), e);
			}
			break;
		case "script":
			scriptElements.add(e);
			break;
		default:
			break;
		}
		for (XulElement child : e.children) {
			findNonVisual(child);
		}
	}

	/**
	 * <?xul-service NativeFileService myxul.service.FileService?>: a Java
	 * object for this window's scripts, as a global. Only classes in
	 * myxul.service that implement XulService can be named.
	 */
	private void addService(String data, int line) {
		String[] parts = data.trim().split("\\s+");
		if (parts.length != 2) {
			throw new XulException(file, line, "<?xul-service?> needs a name and a class: <?xul-service NativeFileService myxul.service.FileService?>");
		}
		String name = parts[0];
		String className = parts[1];
		if (!className.startsWith("myxul.service.")) {
			throw new XulException(file, line, "Services must be classes in myxul.service, not " + className + ".");
		}
		try {
			Class<?> type = Class.forName(className);
			if (!XulService.class.isAssignableFrom(type)) {
				throw new XulException(file, line, className + " isn't a XulService.");
			}
			Constructor<?> make = type.getConstructor(XulWindow.class);
			XulService service = (XulService) make.newInstance(this);
			services.put(name, service);
			scripts.put(name, service);
		} catch (ClassNotFoundException e) {
			throw new XulException(file, line, "There is no service class " + className + ".");
		} catch (ReflectiveOperationException e) {
			throw new XulException(file, line, className + " can't be made: " + e);
		}
	}

	Object build(XulElement e) {
		return engine.registry.create(e, this);
	}

	/**
	 * <key key="S" modifiers="accel" .../>: a menu item with key="..." gets it
	 * as its accelerator (and shows it); other keys work anywhere in the window.
	 */
	private void bindKeys() {
		Map<String, XulElement> usedByMenus = new HashMap<>();
		List<XulElement> items = new ArrayList<>();
		root.collect("menuitem", items);
		for (XulElement item : items) {
			String keyId = item.attr("key");
			if (keyId == null) {
				continue;
			}
			XulElement key = keys.get(keyId);
			if (key == null) {
				throw item.error("key=\"" + keyId + "\" names no <key> in this file.");
			}
			MenuItem menuItem = (MenuItem) item.node;
			KeyCombination combination = combination(key);
			menuItem.setAccelerator(combination);
			if (menuItem.getOnAction() == null) {   // no oncommand of its own: run the key's
				menuItem.setOnAction(ev -> runKey(key, ev));
			}
			if (plain(combination)) {
				catchFirst(combination, () -> {
					if (!menuItem.isDisable()) {
						menuItem.fire();
					}
				});
			}
			usedByMenus.put(keyId, key);
		}
		for (Map.Entry<String, XulElement> entry : keys.entrySet()) {
			if (!usedByMenus.containsKey(entry.getKey())) {
				XulElement key = entry.getValue();
				KeyCombination combination = combination(key);
				Runnable run = () -> runKey(key, new XulEvent("command", stage));
				if (plain(combination)) {
					catchFirst(combination, run);
				} else {
					stage.sceneProperty().addListener((o, was, scene) -> {
						if (scene != null) {
							scene.getAccelerators().put(combination, run);
						}
					});
				}
			}
		}
	}

	/**
	 * accesskey on a <menuitem>: while its menu is open, pressing the letter
	 * chooses it, as in XUL. (JavaFX 8 handles Alt+letter for the menu bar's
	 * menus, but not letters for the items of an open menu.)
	 */
	private void menuAccessKeys() {
		stage.sceneProperty().addListener((o, was, scene) -> {
			if (scene == null) {
				return;
			}
			scene.addEventFilter(KeyEvent.KEY_TYPED, ev -> {
				if (swallowTyped != 0 && ev.getCharacter().length() == 1 && Character.toLowerCase(ev.getCharacter().charAt(0)) == swallowTyped) {
					ev.consume();
				}
				swallowTyped = 0;
			});
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
				if (ev.isControlDown() || ev.isAltDown() || ev.isMetaDown() || ev.getText().length() != 1) {
					return;
				}
				char letter = Character.toLowerCase(ev.getText().charAt(0));
				for (javafx.scene.control.Menu menu : menus) {   // submenus are built before their menus: innermost first
					if (!menu.isShowing()) {
						continue;
					}
					for (MenuItem item : menu.getItems()) {
						if (Character.valueOf(letter).equals(item.getProperties().get(ACCESSKEY)) && !item.isDisable() && item.isVisible()) {
							ev.consume();
							swallowTyped = letter;
							if (item instanceof javafx.scene.control.Menu) {
								((javafx.scene.control.Menu) item).show();
							} else {
								hideMenus();
								if (item instanceof javafx.scene.control.CheckMenuItem) {
									javafx.scene.control.CheckMenuItem check = (javafx.scene.control.CheckMenuItem) item;
									check.setSelected(!check.isSelected());
								} else if (item instanceof javafx.scene.control.RadioMenuItem) {
									((javafx.scene.control.RadioMenuItem) item).setSelected(true);
								}
								item.fire();
							}
							return;
						}
					}
					ev.consume();   // an open menu with no such letter: the key does nothing (it mustn't type behind the menu)
					swallowTyped = letter;
					return;
				}
			});
		});
	}

	private void hideMenus() {
		for (javafx.scene.control.Menu menu : menus) {
			if (menu.isShowing()) {
				menu.hide();
			}
		}
	}

	/** A key without Ctrl, Alt or Meta (F3, or Shift+F3): text controls take these before accelerators see them. */
	private static boolean plain(KeyCombination c) {
		return c.getControl() != KeyCombination.ModifierValue.DOWN && c.getAlt() != KeyCombination.ModifierValue.DOWN
				&& c.getMeta() != KeyCombination.ModifierValue.DOWN && c.getShortcut() != KeyCombination.ModifierValue.DOWN;
	}

	/**
	 * Catches a key on its way down to the focused control, so F3 works while
	 * typing in a <textbox> (a TextArea uses up keys without modifiers, so
	 * they never reach the window's accelerators).
	 */
	private void catchFirst(KeyCombination combination, Runnable action) {
		stage.sceneProperty().addListener((o, was, scene) -> {
			if (scene != null) {
				scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
					if (combination.match(ev)) {
						ev.consume();
						action.run();
					}
				});
			}
		});
	}

	private KeyCombination combination(XulElement key) {
		KeyCode code;
		if (key.attributes.containsKey("keycode")) {
			String name = key.attr("keycode").replaceFirst("^VK_", "");
			switch (name) {
			case "RETURN":
				name = "ENTER";
				break;
			case "BACK":
				name = "BACK_SPACE";
				break;
			default:
				break;
			}
			try {
				code = KeyCode.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw key.error("Unknown keycode \"" + key.attr("keycode") + "\".");
			}
		} else {
			String k = key.attr("key", "");
			if (k.length() != 1) {
				throw key.error("<key> needs key=\"S\" (one character) or keycode=\"VK_F3\".");
			}
			code = KeyCode.getKeyCode(k.toUpperCase());
			if (code == null) {
				throw key.error("key=\"" + k + "\" isn't a key myxul can bind.");
			}
		}
		List<KeyCombination.Modifier> modifiers = new ArrayList<>();
		for (String m : key.attr("modifiers", "").split("[\\s,]+")) {
			switch (m) {
			case "":
				break;
			case "accel":
				modifiers.add(KeyCombination.SHORTCUT_DOWN);
				break;
			case "control":
				modifiers.add(KeyCombination.CONTROL_DOWN);
				break;
			case "shift":
				modifiers.add(KeyCombination.SHIFT_DOWN);
				break;
			case "alt":
				modifiers.add(KeyCombination.ALT_DOWN);
				break;
			case "meta":
				modifiers.add(KeyCombination.META_DOWN);
				break;
			default:
				throw key.error("Unknown modifier \"" + m + "\": use accel, control, shift, alt or meta.");
			}
		}
		return new KeyCodeCombination(code, modifiers.toArray(new KeyCombination.Modifier[0]));
	}

	private void runKey(XulElement key, Object event) {
		String code = key.attr("oncommand");
		if (code != null) {
			XulScriptManager.Handler h = commandHandlers.computeIfAbsent("key:" + key.line, k -> scripts.handler(key, "oncommand", code));
			h.call(key, event);
		} else if (key.attr("command") != null) {
			runCommand(key.attr("command"), event);
		}
	}

	/** Runs <command id="...">'s oncommand, unless the command is disabled. */
	void runCommand(String id, Object event) {
		XulElement command = commands.get(id);
		if (command == null || command.flag("disabled")) {
			return;
		}
		XulScriptManager.Handler h = commandHandlers.get(id);
		if (h != null) {
			h.call(command, event);
		}
	}

	void warn(XulElement e, String message) {
		engine.warn(e.file, e.line, message);
	}

	/** An error in a running script: reported, and the window carries on. */
	void scriptError(Path where, int elementLine, String attribute, Throwable error) {
		int line = elementLine;
		int inCode = XulScriptManager.line(error);
		if (attribute != null && inCode > 1) {
			line = elementLine + inCode - 2;   // the code starts on line 2 of function (event) {\n...
		} else if (attribute == null && inCode > 0) {
			line = inCode;
		}
		engine.report(this, new XulException(where, line, (attribute == null ? "" : attribute + ": ") + XulScriptManager.message(error), error));
	}

	private void closed() {
		if (closed) {
			return;
		}
		closed = true;
		for (Animation timer : timers.values()) {
			timer.stop();
		}
		timers.clear();
		for (Runnable listener : closeListeners) {
			listener.run();
		}
		closeListeners.clear();
		engine.closed(this);
	}

	// ---- For scripts, as window (see bootstrap.js), and for services ----

	public Stage getStage() {
		return stage;
	}

	public Path getFile() {
		return file;
	}

	public String getTitle() {
		return stage.getTitle() == null ? "" : stage.getTitle();
	}

	public void setTitle(String title) {
		stage.setTitle(title);
		root.attributes.put("title", title);
	}

	public List<String> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	/** Closes the window without asking its onclose. */
	public void close() {
		stage.close();
		closed();
	}

	/** Opens another .xul file, relative to this one. extra holds its window.arguments, separated by \0. */
	public void open(String path, String extra) {
		List<String> args = extra == null || extra.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(extra.split("\u0000", -1));
		engine.openWindow(file.resolveSibling(path), args);
	}

	public void alert(String message) {
		engine.dialogs.alert(this, message);
	}

	public boolean confirm(String message) {
		return engine.dialogs.confirm(this, message);
	}

	public String prompt(String message, String value) {
		return engine.dialogs.prompt(this, message, value);
	}

	/** window.setTimeout and setInterval: runs a script function later, on the JavaFX thread. */
	public int setTimeout(Object function, double ms, boolean repeat) {
		if (!(function instanceof ScriptObjectMirror) || !((ScriptObjectMirror) function).isFunction()) {
			throw new IllegalArgumentException("setTimeout needs a function");
		}
		ScriptObjectMirror f = (ScriptObjectMirror) function;
		int id = nextTimer++;
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(Math.max(1, ms)), ev -> {
			if (!repeat) {
				timers.remove(id);
			}
			try {
				f.call(null);
			} catch (RuntimeException e) {
				scriptError(file, 0, null, e);
			}
		}));
		timeline.setCycleCount(repeat ? Animation.INDEFINITE : 1);
		timers.put(id, timeline);
		timeline.play();
		return id;
	}

	public void clearTimeout(Object id) {
		if (id instanceof Number) {
			Animation timer = timers.remove(((Number) id).intValue());
			if (timer != null) {
				timer.stop();
			}
		}
	}

	/** Runs when the window closes: services use it to forget the window. */
	public void addCloseListener(Runnable listener) {
		closeListeners.add(listener);
	}

	/** The element with this id, for Java code (tests, services). */
	public XulElement getElement(String id) {
		return document.getElementById(id);
	}

	/** A service this window was given, by its script name. */
	public XulService getService(String name) {
		return services.get(name);
	}
}
