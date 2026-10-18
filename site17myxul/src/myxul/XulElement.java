package myxul;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

/**
 * One element of a XUL file: its tag, attributes and children as parsed, and,
 * once the window is built, the JavaFX object made for it (a Node, or a
 * MenuItem or Tab, which aren't Nodes).
 *
 * Scripts never see this class directly: document.getElementById() wraps it
 * (see bootstrap.js) so that XUL properties (value, label, checked,
 * disabled, ...) and methods (getAttribute, setAttribute, appendItem, ...)
 * work as in XUL, and anything else is passed to the JavaFX object.
 */
public final class XulElement {

	/** Methods scripts can call on an element; everything else goes to the JavaFX object. */
	private static final Set<String> METHODS = new HashSet<>(Arrays.asList(
			"getAttribute", "setAttribute", "removeAttribute", "hasAttribute",
			"appendItem", "removeItemAt", "getRowCount", "clearItems", "focus",
			"getElementsByTagName", "loadURI", "goBack", "goForward", "reload", "stop",
			"canGoBack", "canGoForward", "appendChild", "insertBefore", "removeChild",
			"addEventListener", "removeEventListener", "openPopup", "openPopupAtScreen", "hidePopup"));

	/** XUL properties scripts can read and set: el.value, el.checked = true. */
	private static final Set<String> PROPERTIES = new HashSet<>(Arrays.asList(
			"id", "tagName", "localName", "node", "value", "label", "checked", "disabled",
			"hidden", "collapsed", "selectedIndex", "parentNode", "childNodes", "firstChild", "lastChild",
			"textContent", "currentURI", "contentTitle", "state"));

	final String tag;
	final Map<String, String> attributes = new LinkedHashMap<>();
	final List<XulElement> children = new ArrayList<>();
	final Path file;
	final int line;
	String namespace = "";  // the element's XML namespace
	XulElement parent;
	String text = "";
	Object node;            // the JavaFX object, once built
	XulWindow window;       // the window it belongs to, once built
	XulDocument document;   // for elements made by document.createElement, until they're built
	Object scriptObject;    // its script wrapper, made on first use
	private final List<Object[]> listeners = new ArrayList<>();   // addEventListener: { type, function, remover }

	XulElement(String tag, Path file, int line) {
		this.tag = tag;
		this.file = file;
		this.line = line;
	}

	// ---- For the engine ----

	String attr(String name) {
		return attributes.get(name);
	}

	String attr(String name, String fallback) {
		String value = attributes.get(name);
		return value == null ? fallback : value;
	}

	boolean flag(String name) {
		return "true".equals(attributes.get(name));
	}

	double number(String name, double fallback) {
		String value = attributes.get(name);
		if (value == null || value.trim().isEmpty()) {
			return fallback;
		}
		try {
			return Double.parseDouble(value.trim().replaceAll("px$", ""));
		} catch (NumberFormatException e) {
			throw error("The " + name + " attribute must be a number, not \"" + value + "\".");
		}
	}

	XulException error(String message) {
		return new XulException(file, line, message);
	}

	/** "<button> on line 12" */
	String where() {
		return "<" + tag + "> on line " + line;
	}

	List<XulElement> childrenNamed(String name) {
		List<XulElement> found = new ArrayList<>();
		for (XulElement child : children) {
			if (child.tag.equals(name)) {
				found.add(child);
			}
		}
		return found;
	}

	XulElement firstChild(String name) {
		for (XulElement child : children) {
			if (child.tag.equals(name)) {
				return child;
			}
		}
		return null;
	}

	void collect(String name, List<XulElement> into) {
		for (XulElement child : children) {
			if (name.equals("*") || child.tag.equals(name)) {
				into.add(child);
			}
			child.collect(name, into);
		}
	}

	// ---- For scripts, through the wrapper ----

	public boolean isMethod(String name) {
		return METHODS.contains(name);
	}

	public boolean isProperty(String name) {
		return PROPERTIES.contains(name);
	}

	public Object getNode() {
		return node;
	}

	public String getId() {
		return attr("id", "");
	}

	public String getTagName() {
		return tag;
	}

	/** The attribute's current value, or "" if it isn't set, as XUL does. Some follow the control: value, checked. */
	public String getAttribute(String name) {
		switch (name) {
		case "value":
			Object value = liveValue();
			if (value != null) {
				return String.valueOf(value);
			}
			break;
		case "checked":
			Boolean checked = liveChecked();
			if (checked != null) {
				return checked ? "true" : "false";
			}
			break;
		default:
			break;
		}
		return attr(name, "");
	}

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name) || (name.equals("value") && liveValue() != null);
	}

	/** Sets an attribute and applies it to the control: setAttribute("disabled", true). */
	public void setAttribute(String name, Object value) {
		String text = value == null ? "" : String.valueOf(value);
		if (value instanceof Double && !((Double) value).isInfinite() && (Double) value == Math.rint((Double) value) && Math.abs((Double) value) < 1e15) {
			text = String.valueOf(((Double) value).longValue());   // setAttribute("left", 100): "100", as in XUL, not "100.0"
		}
		String old = attributes.put(name, text);
		if (name.equals("id") && window != null && inDocument()) {
			window.document.renamed(this, old);
		}
		if (window != null) {
			window.engine.registry.applyAttribute(this, name, text);
		}
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
		if (window != null) {
			window.engine.registry.applyAttribute(this, name, null);
		}
	}

	/** XUL properties: el.value, el.label, el.checked, el.disabled, el.hidden, el.selectedIndex, ... */
	public Object getProperty(String name) {
		switch (name) {
		case "id":
			return getId();
		case "tagName":
		case "localName":
			return tag;
		case "node":
			return node;
		case "parentNode":
			return parent;
		case "childNodes":
			return Collections.unmodifiableList(children);
		case "firstChild":
			return children.isEmpty() ? null : children.get(0);
		case "lastChild":
			return children.isEmpty() ? null : children.get(children.size() - 1);
		case "state":
			return node instanceof javafx.stage.PopupWindow ? (((javafx.stage.PopupWindow) node).isShowing() ? "open" : "closed") : null;
		case "textContent":
			return text;
		case "checked":
		case "disabled":
		case "hidden":
		case "collapsed":
			return "true".equals(getAttribute(name));
		case "selectedIndex":
			return selectedIndex();
		case "currentURI":
			return node instanceof WebView ? ((WebView) node).getEngine().getLocation() : null;
		case "contentTitle":
			return node instanceof WebView ? ((WebView) node).getEngine().getTitle() : null;
		default:
			return getAttribute(name);
		}
	}

	public void setProperty(String name, Object value) {
		switch (name) {
		case "selectedIndex":
			select(((Number) value).intValue());
			break;
		case "id":
		case "tagName":
		case "localName":
		case "node":
		case "parentNode":
		case "childNodes":
		case "firstChild":
		case "lastChild":
		case "state":
		case "currentURI":
		case "contentTitle":
			throw new IllegalArgumentException(name + " can't be changed.");
		case "textContent":
			text = String.valueOf(value);
			break;
		default:
			setAttribute(name, value);
			break;
		}
	}

	public void focus() {
		if (node instanceof Node) {
			((Node) node).requestFocus();
		}
	}

	public List<XulElement> getElementsByTagName(String name) {
		List<XulElement> found = new ArrayList<>();
		collect(name, found);
		return found;
	}

	// ---- Adding and removing elements: appendChild, insertBefore, removeChild ----

	/** Whether this element is in its window's tree (not made by createElement and left unattached). */
	private boolean inDocument() {
		XulElement e = this;
		while (e.parent != null) {
			e = e.parent;
		}
		return window != null && e == window.root;
	}

	public XulElement appendChild(XulElement child) {
		return insertBefore(child, null);
	}

	/**
	 * Puts child (made by document.createElement, or already in the window,
	 * which moves it) among this element's children, before ref, or at the
	 * end when ref is null. Once this element is in the window, the child is
	 * built and shown.
	 */
	public XulElement insertBefore(XulElement child, XulElement ref) {
		if (child == null) {
			throw new IllegalArgumentException("appendChild needs an element.");
		}
		for (XulElement e = this; e != null; e = e.parent) {
			if (e == child) {
				throw new IllegalArgumentException(child + " can't go inside itself.");
			}
		}
		if (ref != null && ref.parent != this) {
			throw new IllegalArgumentException(ref + " isn't a child of " + this + ".");
		}
		if (child.parent != null) {
			child.parent.removeChild(child);
		}
		children.add(ref == null ? children.size() : children.indexOf(ref), child);
		child.parent = this;
		if (node != null && window != null && inDocument()) {
			try {
				window.document.index(child);
			} catch (XulException e) {
				children.remove(child);
				child.parent = null;
				throw new IllegalArgumentException(e.getMessage());
			}
			if (child.node == null) {
				window.build(child);
			}
			XulRegistry.insert(this, child);
		}
		return child;
	}

	public XulElement removeChild(XulElement child) {
		if (child == null || child.parent != this) {
			throw new IllegalArgumentException(child + " isn't a child of " + this + ".");
		}
		if (window != null && inDocument()) {
			window.document.unindex(child);
		}
		XulRegistry.remove(this, child);
		children.remove(child);
		child.parent = null;
		return child;
	}

	// ---- Events: addEventListener("command", function (event) { ... }) ----

	public void addEventListener(String type, Object function) {
		if (!(function instanceof jdk.nashorn.api.scripting.ScriptObjectMirror) || !((jdk.nashorn.api.scripting.ScriptObjectMirror) function).isFunction()) {
			throw new IllegalArgumentException("addEventListener needs a function.");
		}
		for (Object[] l : listeners) {
			if (l[0].equals(type) && l[1].equals(function)) {
				return;   // already listening, as in the DOM
			}
		}
		if (node == null) {
			throw new IllegalStateException("addEventListener works once " + this + " is in the window: append it first.");
		}
		jdk.nashorn.api.scripting.ScriptObjectMirror f = (jdk.nashorn.api.scripting.ScriptObjectMirror) function;
		XulWindow w = window;
		Runnable remover = XulEvents.listen(this, node, type, ev -> w.scripts.call(f, this, ev));
		listeners.add(new Object[] { type, function, remover });
	}

	public void removeEventListener(String type, Object function) {
		for (Object[] l : new ArrayList<>(listeners)) {
			if (l[0].equals(type) && l[1].equals(function)) {
				((Runnable) l[2]).run();
				listeners.remove(l);
			}
		}
	}

	// ---- <panel> and <menupopup>: openPopup, openPopupAtScreen, hidePopup ----

	/**
	 * Opens a <panel> or <menupopup> next to anchor: position is XUL's
	 * "after_start" (below, left edges lined up; the default), "after_end",
	 * "before_start", "before_end", "end_before", "start_before" or
	 * "overlap", then moved by x and y.
	 */
	public void openPopup(XulElement anchor, String position, double x, double y) {
		if (anchor == null || !(anchor.node instanceof Node) || ((Node) anchor.node).getScene() == null) {
			openPopupAtScreen(x, y);
			return;
		}
		Node a = (Node) anchor.node;
		javafx.geometry.Bounds b = a.localToScreen(a.getBoundsInLocal());
		String where = position == null || position.isEmpty() || position.equals("undefined") ? "after_start" : position;
		javafx.stage.PopupWindow popup = popup("openPopup");
		XulRegistry.showPopup(this, popup, a, 0, 0);
		double w = popup.getWidth();
		double h = popup.getHeight();
		double px;
		double py;
		switch (where) {
		case "after_end":
			px = b.getMaxX() - w;
			py = b.getMaxY();
			break;
		case "before_start":
			px = b.getMinX();
			py = b.getMinY() - h;
			break;
		case "before_end":
			px = b.getMaxX() - w;
			py = b.getMinY() - h;
			break;
		case "end_before":
			px = b.getMaxX();
			py = b.getMinY();
			break;
		case "start_before":
			px = b.getMinX() - w;
			py = b.getMinY();
			break;
		case "overlap":
			px = b.getMinX();
			py = b.getMinY();
			break;
		case "after_start":
			px = b.getMinX();
			py = b.getMaxY();
			break;
		default:
			throw new IllegalArgumentException("Unknown popup position \"" + position + "\".");
		}
		popup.setX(px + x);
		popup.setY(py + y);
	}

	public void openPopupAtScreen(double x, double y) {
		javafx.stage.PopupWindow popup = popup("openPopupAtScreen");
		Object owner = window.root.node;
		XulRegistry.showPopup(this, popup, (Node) owner, x, y);
	}

	public void hidePopup() {
		popup("hidePopup").hide();
	}

	private javafx.stage.PopupWindow popup(String method) {
		if (!(node instanceof javafx.stage.PopupWindow)) {
			throw new UnsupportedOperationException(method + " works on <panel> and on a <menupopup> in a <popupset>, not " + this + ".");
		}
		return (javafx.stage.PopupWindow) node;
	}

	// ---- <browser>: XUL's loadURI, goBack, goForward, reload, stop, canGoBack, canGoForward ----

	private WebView browser(String method) {
		if (!(node instanceof WebView)) {
			throw new UnsupportedOperationException(method + " works on <browser>, not <" + tag + ">.");
		}
		return (WebView) node;
	}

	/** Loads a page: a URL, or a file relative to the .xul file. */
	public void loadURI(String uri) {
		browser("loadURI").getEngine().load(XulRegistry.url(this, uri));
	}

	public void goBack() {
		if (canGoBack()) {
			browser("goBack").getEngine().getHistory().go(-1);
		}
	}

	public void goForward() {
		if (canGoForward()) {
			browser("goForward").getEngine().getHistory().go(1);
		}
	}

	public void reload() {
		browser("reload").getEngine().reload();
	}

	public void stop() {
		browser("stop").getEngine().getLoadWorker().cancel();
	}

	public boolean canGoBack() {
		return browser("canGoBack").getEngine().getHistory().getCurrentIndex() > 0;
	}

	public boolean canGoForward() {
		WebHistory history = browser("canGoForward").getEngine().getHistory();
		return history.getCurrentIndex() < history.getEntries().size() - 1;
	}

	// ---- Lists and trees: XUL's listbox.appendItem(label), and the same for menulist and tree ----

	@SuppressWarnings("unchecked")
	public void appendItem(String label) {
		if (node instanceof ListView) {
			((ListView<String>) node).getItems().add(label);
		} else if (node instanceof ComboBox) {
			((ComboBox<String>) node).getItems().add(label);
		} else if (node instanceof TreeView) {
			((TreeView<String>) node).getRoot().getChildren().add(new TreeItem<>(label));
		} else {
			throw new UnsupportedOperationException("appendItem works on <listbox>, <menulist> and <tree>, not <" + tag + ">.");
		}
	}

	@SuppressWarnings("unchecked")
	public void removeItemAt(int index) {
		if (node instanceof ListView) {
			((ListView<String>) node).getItems().remove(index);
		} else if (node instanceof ComboBox) {
			((ComboBox<String>) node).getItems().remove(index);
		} else if (node instanceof TreeView) {
			((TreeView<String>) node).getRoot().getChildren().remove(index);
		} else {
			throw new UnsupportedOperationException("removeItemAt works on <listbox>, <menulist> and <tree>, not <" + tag + ">.");
		}
	}

	public int getRowCount() {
		if (node instanceof ListView) {
			return ((ListView<?>) node).getItems().size();
		} else if (node instanceof ComboBox) {
			return ((ComboBox<?>) node).getItems().size();
		} else if (node instanceof TreeView) {
			return ((TreeView<?>) node).getExpandedItemCount();   // the visible rows (myxul's tree root is hidden and not counted)
		}
		return 0;
	}

	public void clearItems() {
		if (node instanceof ListView) {
			((ListView<?>) node).getItems().clear();
		} else if (node instanceof ComboBox) {
			((ComboBox<?>) node).getItems().clear();
		} else if (node instanceof TreeView) {
			((TreeView<?>) node).getRoot().getChildren().clear();
		}
	}

	private int selectedIndex() {
		if (node instanceof ListView) {
			return ((ListView<?>) node).getSelectionModel().getSelectedIndex();
		} else if (node instanceof ComboBox) {
			return ((ComboBox<?>) node).getSelectionModel().getSelectedIndex();
		} else if (node instanceof TreeView) {
			return ((TreeView<?>) node).getSelectionModel().getSelectedIndex();
		} else if (node instanceof TabPane) {
			return ((TabPane) node).getSelectionModel().getSelectedIndex();
		}
		return -1;
	}

	private void select(int index) {
		if (node instanceof ListView) {
			((ListView<?>) node).getSelectionModel().select(index);
		} else if (node instanceof ComboBox) {
			((ComboBox<?>) node).getSelectionModel().select(index);
		} else if (node instanceof TreeView) {
			((TreeView<?>) node).getSelectionModel().select(index);
		} else if (node instanceof TabPane) {
			((TabPane) node).getSelectionModel().select(index);
		}
	}

	private Object liveValue() {
		if (node instanceof TextInputControl) {
			return ((TextInputControl) node).getText();
		} else if (node instanceof ComboBox) {
			return ((ComboBox<?>) node).getValue();
		} else if (node instanceof ProgressBar) {
			double progress = ((ProgressBar) node).getProgress();
			return progress < 0 ? null : (Object) (int) Math.round(progress * 100);
		}
		return null;
	}

	private Boolean liveChecked() {
		if (node instanceof CheckBox) {
			return ((CheckBox) node).isSelected();
		} else if (node instanceof Toggle) {
			return ((Toggle) node).isSelected();
		} else if (node instanceof CheckMenuItem) {
			return ((CheckMenuItem) node).isSelected();
		} else if (node instanceof RadioMenuItem) {
			return ((RadioMenuItem) node).isSelected();
		} else if (node instanceof MenuItem) {
			return null;
		}
		return null;
	}

	@Override
	public String toString() {
		return "<" + tag + (attributes.containsKey("id") ? " id=\"" + attributes.get("id") + "\"" : "") + ">";
	}
}
