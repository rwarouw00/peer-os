package myxul;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Connects an element's event attributes (oncommand="save()", onclick, ...)
 * to the JavaFX events of its control. Each attribute's code is compiled
 * once, when the window is built, as function (event) { code }, and called
 * with "this" as the element (see XulScriptManager).
 */
final class XulEvents {

	/** Something to run when an event happens: an attribute's compiled code, or a function given to addEventListener. */
	interface Listener {
		void handle(Object event);
	}

	/** Event attributes that work through listen() as well: XUL's name, and the event type. */
	private static final String[][] LISTENED = {
		{ "onmousedown", "mousedown" }, { "onmouseup", "mouseup" }, { "onmousemove", "mousemove" },
		{ "oncontextmenu", "contextmenu" }, { "onpopupshown", "popupshown" }, { "onpopuphidden", "popuphidden" },
	};

	static void wire(XulElement e, Object node, XulWindow w) {
		XulScriptManager.Handler own = handler(e, "oncommand", w);
		String commandId = e.attr("command");
		if (own != null || commandId != null) {
			if (node instanceof ButtonBase) {
				((ButtonBase) node).setOnAction(ev -> command(e, own, commandId, ev, w));
			} else if (node instanceof MenuItem) {
				((MenuItem) node).setOnAction(ev -> command(e, own, commandId, ev, w));
			} else if (node instanceof ComboBox) {
				((ComboBox<?>) node).setOnAction(ev -> command(e, own, commandId, ev, w));
			} else if (node instanceof TextField) {
				((TextField) node).setOnAction(ev -> command(e, own, commandId, ev, w));   // Enter in a one-line textbox
			} else {
				w.warn(e, "oncommand does nothing on " + node.getClass().getSimpleName() + "; use onclick.");
			}
		}

		XulScriptManager.Handler click = handler(e, "onclick", w);
		XulScriptManager.Handler dblclick = handler(e, "ondblclick", w);
		if ((click != null || dblclick != null) && node instanceof Node) {
			((Node) node).setOnMouseClicked(ev -> {
				if (click != null && ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 1) {
					click.call(e, ev);
				}
				if (dblclick != null && ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
					dblclick.call(e, ev);
				}
			});
		}
		XulScriptManager.Handler keydown = handler(e, "onkeydown", w);
		XulScriptManager.Handler keypress = handler(e, "onkeypress", w);
		if ((keydown != null || keypress != null) && node instanceof Node) {
			((Node) node).setOnKeyPressed(ev -> {
				if (keydown != null) {
					keydown.call(e, ev);
				}
				if (keypress != null) {
					keypress.call(e, ev);
				}
			});
		}
		XulScriptManager.Handler keyup = handler(e, "onkeyup", w);
		if (keyup != null && node instanceof Node) {
			((Node) node).setOnKeyReleased(ev -> keyup.call(e, ev));
		}
		XulScriptManager.Handler over = handler(e, "onmouseover", w);
		if (over != null && node instanceof Node) {
			((Node) node).setOnMouseEntered(ev -> over.call(e, ev));
		}
		XulScriptManager.Handler out = handler(e, "onmouseout", w);
		if (out != null && node instanceof Node) {
			((Node) node).setOnMouseExited(ev -> out.call(e, ev));
		}

		for (String[] pair : LISTENED) {
			XulScriptManager.Handler h = handler(e, pair[0], w);
			if (h != null) {
				listen(e, node, pair[1], ev -> h.call(e, ev));
			}
		}

		if (node instanceof TextInputControl) {
			TextInputControl text = (TextInputControl) node;
			XulScriptManager.Handler input = handler(e, "oninput", w);
			if (input != null) {
				text.textProperty().addListener((o, was, now) -> input.call(e, new XulEvent("input", text)));
			}
			XulScriptManager.Handler change = handler(e, "onchange", w);
			if (change != null) {
				// XUL fires onchange when the text was changed and the box loses focus.
				String[] atFocus = { text.getText() };
				text.focusedProperty().addListener((o, was, focused) -> {
					if (focused) {
						atFocus[0] = text.getText();
					} else if (!text.getText().equals(atFocus[0])) {
						change.call(e, new XulEvent("change", text));
					}
				});
			}
		}
		if (node instanceof Node) {
			Node n = (Node) node;
			XulScriptManager.Handler focus = handler(e, "onfocus", w);
			XulScriptManager.Handler blur = handler(e, "onblur", w);
			if (focus != null || blur != null) {
				n.focusedProperty().addListener((o, was, focused) -> {
					if (focused && focus != null) {
						focus.call(e, new XulEvent("focus", n));
					} else if (!focused && blur != null) {
						blur.call(e, new XulEvent("blur", n));
					}
				});
			}
		}
		if (node instanceof WebView) {
			// <browser>: onload when a page has loaded, onerror when one failed, onlocationchange when a new one starts.
			WebEngine web = ((WebView) node).getEngine();
			XulScriptManager.Handler load = handler(e, "onload", w);
			XulScriptManager.Handler error = handler(e, "onerror", w);
			if (load != null || error != null) {
				web.getLoadWorker().stateProperty().addListener((o, was, state) -> {
					if (state == Worker.State.SUCCEEDED && load != null) {
						load.call(e, new XulEvent("load", node));
					} else if (state == Worker.State.FAILED && error != null) {
						error.call(e, new XulEvent("error", node));
					}
				});
			}
			XulScriptManager.Handler location = handler(e, "onlocationchange", w);
			if (location != null) {
				web.locationProperty().addListener((o, was, now) -> location.call(e, new XulEvent("locationchange", node)));
			}
		}
		XulScriptManager.Handler select = handler(e, "onselect", w);
		if (select != null) {
			ObservableValue<?> selection = null;
			if (node instanceof ListView) {
				selection = ((ListView<?>) node).getSelectionModel().selectedIndexProperty();
			} else if (node instanceof TreeView) {
				selection = ((TreeView<?>) node).getSelectionModel().selectedIndexProperty();
			} else if (node instanceof TabPane) {
				selection = ((TabPane) node).getSelectionModel().selectedIndexProperty();
			} else if (node instanceof ComboBox) {
				selection = ((ComboBox<?>) node).getSelectionModel().selectedIndexProperty();
			}
			if (selection != null) {
				selection.addListener((o, was, now) -> select.call(e, new XulEvent("select", node)));
			} else if (node instanceof Tab) {
				Tab tab = (Tab) node;
				tab.setOnSelectionChanged(ev -> {
					if (tab.isSelected()) {
						select.call(e, ev);
					}
				});
			} else {
				w.warn(e, "onselect does nothing on " + node.getClass().getSimpleName() + ".");
			}
		}
	}

	/**
	 * addEventListener("command", f) and the like: calls the listener when
	 * the element's JavaFX object sends the matching event. Returns
	 * something that stops listening (for removeEventListener).
	 */
	static Runnable listen(XulElement e, Object node, String type, Listener listener) {
		switch (type) {
		case "command":
			EventHandler<ActionEvent> action = listener::handle;
			if (node instanceof ButtonBase || node instanceof ComboBox || node instanceof TextField) {
				((Node) node).addEventHandler(ActionEvent.ACTION, action);
				return () -> ((Node) node).removeEventHandler(ActionEvent.ACTION, action);
			} else if (node instanceof MenuItem) {
				((MenuItem) node).addEventHandler(ActionEvent.ACTION, action);
				return () -> ((MenuItem) node).removeEventHandler(ActionEvent.ACTION, action);
			}
			throw new IllegalArgumentException("command events come from buttons, menu items, menulists and one-line textboxes, not " + e + ".");
		case "click":
		case "dblclick":
			int clicks = type.equals("click") ? 1 : 2;
			return on(e, node, type, MouseEvent.MOUSE_CLICKED, ev -> {
				if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == clicks) {
					listener.handle(ev);
				}
			});
		case "mousedown":
			return on(e, node, type, MouseEvent.MOUSE_PRESSED, listener::handle);
		case "mouseup":
			return on(e, node, type, MouseEvent.MOUSE_RELEASED, listener::handle);
		case "mousemove":   // moving, and dragging (JavaFX sends those to the element the button went down on)
			Runnable moved = on(e, node, type, MouseEvent.MOUSE_MOVED, listener::handle);
			Runnable dragged = on(e, node, type, MouseEvent.MOUSE_DRAGGED, listener::handle);
			return () -> {
				moved.run();
				dragged.run();
			};
		case "mouseover":
			return on(e, node, type, MouseEvent.MOUSE_ENTERED, listener::handle);
		case "mouseout":
			return on(e, node, type, MouseEvent.MOUSE_EXITED, listener::handle);
		case "contextmenu":
			return on(e, node, type, ContextMenuEvent.CONTEXT_MENU_REQUESTED, listener::handle);
		case "keydown":
		case "keypress":
			return on(e, node, type, KeyEvent.KEY_PRESSED, listener::handle);
		case "keyup":
			return on(e, node, type, KeyEvent.KEY_RELEASED, listener::handle);
		case "focus":
		case "blur":
			if (!(node instanceof Node)) {
				throw new IllegalArgumentException(type + " events come from elements on screen, not " + e + ".");
			}
			boolean gained = type.equals("focus");
			Node n = (Node) node;
			ChangeListener<Boolean> focus = (o, was, now) -> {
				if (now == gained) {
					listener.handle(new XulEvent(type, n));
				}
			};
			n.focusedProperty().addListener(focus);
			return () -> n.focusedProperty().removeListener(focus);
		case "popupshown":
		case "popuphidden":
			if (!(node instanceof PopupWindow)) {
				throw new IllegalArgumentException(type + " events come from <panel> and <menupopup>, not " + e + ".");
			}
			EventType<WindowEvent> windowType = type.equals("popupshown") ? WindowEvent.WINDOW_SHOWN : WindowEvent.WINDOW_HIDDEN;
			EventHandler<WindowEvent> shown = listener::handle;
			((PopupWindow) node).addEventHandler(windowType, shown);
			return () -> ((PopupWindow) node).removeEventHandler(windowType, shown);
		default:
			throw new IllegalArgumentException("myxul has no \"" + type + "\" event: it has command, click, dblclick, mousedown, mousemove, mouseup, "
					+ "mouseover, mouseout, contextmenu, keydown, keyup, focus, blur, popupshown and popuphidden.");
		}
	}

	private static <T extends Event> Runnable on(XulElement e, Object node, String type, EventType<T> eventType, EventHandler<T> handler) {
		if (!(node instanceof Node)) {
			throw new IllegalArgumentException(type + " events come from elements on screen, not " + e + ".");
		}
		((Node) node).addEventHandler(eventType, handler);
		return () -> ((Node) node).removeEventHandler(eventType, handler);
	}

	private static XulScriptManager.Handler handler(XulElement e, String attribute, XulWindow w) {
		String code = e.attr(attribute);
		return code == null || code.trim().isEmpty() ? null : w.scripts.handler(e, attribute, code);
	}

	/** Runs the element's own oncommand, or else its <command>'s. */
	private static void command(XulElement e, XulScriptManager.Handler own, String commandId, Object event, XulWindow w) {
		if (own != null) {
			own.call(e, event);
		} else {
			w.runCommand(commandId, event);
		}
	}

	private XulEvents() {
	}
}
