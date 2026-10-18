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
			"canGoBack", "canGoForward"));

	/** XUL properties scripts can read and set: el.value, el.checked = true. */
	private static final Set<String> PROPERTIES = new HashSet<>(Arrays.asList(
			"id", "tagName", "localName", "node", "value", "label", "checked", "disabled",
			"hidden", "collapsed", "selectedIndex", "parentNode", "childNodes", "textContent",
			"currentURI", "contentTitle"));

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
	Object scriptObject;    // its script wrapper, made on first use

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
		attributes.put(name, text);
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
