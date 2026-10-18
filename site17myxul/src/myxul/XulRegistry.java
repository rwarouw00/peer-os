package myxul;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;

/**
 * Maps each XUL tag to the JavaFX object made for it, and applies XUL
 * attributes to those objects, both when the window is built and when a
 * script calls setAttribute() later.
 */
final class XulRegistry {

	/** Makes the JavaFX object for one element (its children too, for containers). Null for non-visual tags. */
	interface Builder {
		Object build(XulElement element, XulWindow window);
	}

	/** Tags that make nothing on screen: the window reads them itself. */
	private static final List<String> NON_VISUAL = java.util.Arrays.asList(
			"script", "commandset", "command", "keyset", "key", "broadcasterset", "stringbundleset");

	private final Map<String, Builder> builders = new HashMap<>();

	XulRegistry() {
		for (String tag : NON_VISUAL) {
			builders.put(tag, (e, w) -> null);
		}
		builders.put("window", (e, w) -> box(e, w, Orientation.VERTICAL));
		builders.put("dialog", (e, w) -> box(e, w, Orientation.VERTICAL));
		builders.put("vbox", (e, w) -> box(e, w, Orientation.VERTICAL));
		builders.put("hbox", (e, w) -> box(e, w, Orientation.HORIZONTAL));
		builders.put("box", (e, w) -> box(e, w, "vertical".equals(e.attr("orient")) ? Orientation.VERTICAL : Orientation.HORIZONTAL));
		builders.put("tabpanel", (e, w) -> box(e, w, "horizontal".equals(e.attr("orient")) ? Orientation.HORIZONTAL : Orientation.VERTICAL));
		builders.put("toolbox", (e, w) -> box(e, w, Orientation.VERTICAL));
		builders.put("statusbar", this::statusbar);
		builders.put("spacer", (e, w) -> new Region());
		builders.put("label", (e, w) -> label(e, false));
		builders.put("description", (e, w) -> label(e, true));
		builders.put("caption", (e, w) -> label(e, false));
		builders.put("statusbarpanel", (e, w) -> label(e, false));
		builders.put("button", (e, w) -> labeled(new Button(), e));
		builders.put("toolbarbutton", this::toolbarbutton);
		builders.put("checkbox", (e, w) -> labeled(new CheckBox(), e));
		builders.put("radiogroup", this::radiogroup);
		builders.put("radio", (e, w) -> labeled(new RadioButton(), e));
		builders.put("textbox", this::textbox);
		builders.put("separator", this::separator);
		builders.put("toolbarseparator", (e, w) -> new Separator(Orientation.VERTICAL));
		builders.put("menuseparator", (e, w) -> new SeparatorMenuItem());
		builders.put("image", this::image);
		builders.put("progressmeter", this::progressmeter);
		builders.put("canvas", (e, w) -> new XulCanvas());
		builders.put("browser", this::browser);
		builders.put("iframe", this::browser);
		builders.put("groupbox", this::groupbox);
		builders.put("toolbar", this::toolbar);
		builders.put("menubar", this::menubar);
		builders.put("menu", this::menu);
		builders.put("menuitem", this::menuitem);
		builders.put("menulist", this::menulist);
		builders.put("listbox", this::listbox);
		builders.put("tree", this::tree);
		builders.put("tabbox", this::tabbox);
		builders.put("grid", this::grid);
		builders.put("stack", this::stack);
		builders.put("scrollbox", this::scrollbox);
		builders.put("popupset", this::popupset);
		builders.put("panel", this::panel);
		builders.put("menupopup", this::menupopup);
	}

	/** The tags this engine knows, for the docs and error messages. */
	List<String> tags() {
		List<String> all = new ArrayList<>(new TreeSet<>(builders.keySet()));
		all.add("splitter");
		Collections.sort(all);
		return all;
	}

	/** Builds an element: its JavaFX object, then the attributes every element shares, then its events. */
	Object create(XulElement e, XulWindow w) {
		Builder builder = builders.get(e.tag);
		if (builder == null) {
			if (e.tag.equals("splitter")) {
				throw e.error("<splitter> must be directly inside a box: <hbox>, <vbox>, <box>, <window>, <tabpanel> or <toolbox>.");
			}
			throw e.error("Unknown tag <" + e.tag + ">. The tags myxul knows are: " + String.join(", ", tags()) + ".");
		}
		e.window = w;
		Object node = builder.build(e, w);
		e.node = node;
		if (node != null) {
			common(e, node, w);
			XulEvents.wire(e, node, w);
		}
		return node;
	}

	// ---- Attributes every element has ----

	private void common(XulElement e, Object node, XulWindow w) {
		String id = e.attr("id");
		if (node instanceof Node) {
			Node n = (Node) node;
			if (id != null) {
				n.setId(id);
			}
			if (e.attributes.containsKey("class")) {
				n.getStyleClass().addAll(e.attr("class").trim().split("\\s+"));
			}
			if (e.attributes.containsKey("flex")) {
				XulBox.setFlex(n, e.number("flex", 0));
			}
			if (n instanceof Region) {
				size((Region) n, e);
			}
			if (e.attributes.containsKey("style")) {
				for (String ignored : XulStyle.apply(e, n, e.attr("style"))) {
					w.warn(e, "style \"" + ignored + "\" isn't supported on " + n.getClass().getSimpleName() + ", so it was ignored.");
				}
			}
			if (e.attributes.containsKey("left") || e.attributes.containsKey("top")) {
				XulBox.Stack.setPosition(n, e.number("left", Double.NaN), e.number("top", Double.NaN));
			}
			if (e.attributes.containsKey("context")) {
				context(e, n);
			}
		} else if (node instanceof MenuItem && id != null) {
			((MenuItem) node).setId(id);
		} else if (node instanceof Tab && id != null) {
			((Tab) node).setId(id);
		}
		for (String name : new String[] { "disabled", "hidden", "collapsed", "tooltiptext" }) {
			if (e.attributes.containsKey(name)) {
				applyAttribute(e, name, e.attr(name));
			}
		}
		String command = e.attr("command");
		if (command != null) {
			XulElement c = w.commands.get(command);
			if (c == null) {
				throw e.error("command=\"" + command + "\" names no <command> in this file.");
			}
			w.observers.computeIfAbsent(command, k -> new ArrayList<>()).add(e);
			if (c.flag("disabled")) {
				applyAttribute(e, "disabled", "true");
			}
		}
	}

	/** width, height, minwidth, maxwidth, minheight, maxheight (in pixels). */
	private void size(Region r, XulElement e) {
		if (e.attributes.containsKey("width") && !e.tag.equals("window") && !e.tag.equals("dialog")) {
			r.setPrefWidth(e.number("width", 0));
		}
		if (e.attributes.containsKey("height") && !e.tag.equals("window") && !e.tag.equals("dialog")) {
			r.setPrefHeight(e.number("height", 0));
		}
		if (e.attributes.containsKey("minwidth")) {
			r.setMinWidth(e.number("minwidth", 0));
		}
		if (e.attributes.containsKey("minheight")) {
			r.setMinHeight(e.number("minheight", 0));
		}
		if (e.attributes.containsKey("maxwidth")) {
			r.setMaxWidth(e.number("maxwidth", 0));
			XulBox.setLimited(r);
		}
		if (e.attributes.containsKey("maxheight")) {
			r.setMaxHeight(e.number("maxheight", 0));
			XulBox.setLimited(r);
		}
	}

	/** Applies one attribute's new value (null when removed) to the element's JavaFX object. */
	void applyAttribute(XulElement e, String name, String value) {
		Object node = e.node;
		boolean on = "true".equals(value);
		switch (name) {
		case "label":
			if (node instanceof MenuItem) {
				((MenuItem) node).setText(mnemonic(value == null ? "" : value, e.attr("accesskey"), true));
			} else if (node instanceof Tab) {
				((Tab) node).setText(value);
			} else if (node instanceof Labeled) {
				setLabel((Labeled) node, value == null ? "" : value, e.attr("accesskey"));
			} else if (node instanceof TitledPane) {
				((TitledPane) node).setText(value);
			}
			break;
		case "value":
			if (node instanceof TextInputControl) {
				((TextInputControl) node).setText(value == null ? "" : value);
			} else if (node instanceof Label) {
				((Label) node).setText(value == null ? "" : value);
			} else if (node instanceof ComboBox) {
				@SuppressWarnings("unchecked")
				ComboBox<String> box = (ComboBox<String>) node;
				box.setValue(value);
			} else if (node instanceof ProgressBar && !"undetermined".equals(e.attr("mode"))) {
				((ProgressBar) node).setProgress(value == null ? 0 : Math.max(0, Math.min(100, e.number("value", 0))) / 100);
			}
			break;
		case "disabled":
			if (node instanceof Node) {
				((Node) node).setDisable(on);
			} else if (node instanceof MenuItem) {
				((MenuItem) node).setDisable(on);
			} else if (node instanceof Tab) {
				((Tab) node).setDisable(on);
			}
			if (e.tag.equals("command") && e.window != null) {
				for (XulElement observer : e.window.observers.getOrDefault(e.getId(), Collections.emptyList())) {
					applyAttribute(observer, "disabled", value);
				}
			}
			break;
		case "checked":
			if (node instanceof CheckBox) {
				((CheckBox) node).setSelected(on);
			} else if (node instanceof Toggle) {
				((Toggle) node).setSelected(on);
			} else if (node instanceof CheckMenuItem) {
				((CheckMenuItem) node).setSelected(on);
			} else if (node instanceof RadioMenuItem) {
				((RadioMenuItem) node).setSelected(on);
			}
			break;
		case "hidden":
		case "collapsed":
			boolean hidden = on || (name.equals("hidden") ? e.flag("collapsed") : e.flag("hidden"));
			if (node instanceof Node && e.parent != null && e.parent.node instanceof SplitPane) {
				splitPaneVisibility(e, (Node) node, (SplitPane) e.parent.node, hidden);
			} else if (node instanceof Node) {
				((Node) node).setVisible(!hidden);
				((Node) node).setManaged(!hidden);
			} else if (node instanceof MenuItem) {
				((MenuItem) node).setVisible(!hidden);
			}
			break;
		case "tooltiptext":
			if (node instanceof Control) {
				((Control) node).setTooltip(value == null ? null : new Tooltip(value));
			} else if (node instanceof Node && value != null) {
				Tooltip.install((Node) node, new Tooltip(value));
			}
			break;
		case "style":
			if (node instanceof Node) {
				((Node) node).setStyle("");
				((Node) node).setEffect(null);
				if (value != null) {
					for (String ignored : XulStyle.apply(e, (Node) node, value)) {
						e.window.warn(e, "style \"" + ignored + "\" isn't supported, so it was ignored.");
					}
				}
			}
			break;
		case "flex":
			if (node instanceof Node) {
				XulBox.setFlex((Node) node, value == null ? 0 : e.number("flex", 0));
			}
			break;
		case "title":
			if (e.window != null && e.window.root == e) {
				e.window.stage.setTitle(value == null ? "" : value);
			}
			break;
		case "left":
		case "top":
			if (node instanceof Node) {
				XulBox.Stack.setPosition((Node) node, e.number("left", Double.NaN), e.number("top", Double.NaN));
			}
			break;
		case "context":
			if (node instanceof Node && value != null) {
				context(e, (Node) node);
			}
			break;
		case "image":
			if (node instanceof Labeled) {
				((Labeled) node).setGraphic(value == null || value.isEmpty() ? null : new ImageView(new Image(url(e, value))));
			}
			break;
		case "src":
			if (node instanceof ImageView && value != null) {
				((ImageView) node).setImage(new Image(url(e, value)));
			} else if (node instanceof WebView && value != null) {
				((WebView) node).getEngine().load(url(e, value));
			}
			break;
		case "mode":
			if (node instanceof ProgressBar) {
				((ProgressBar) node).setProgress("undetermined".equals(value) ? ProgressBar.INDETERMINATE_PROGRESS : e.number("value", 0) / 100);
			}
			break;
		default:
			break;   // kept as an attribute only, for scripts to read
		}
	}

	/**
	 * A SplitPane can't hide one of its panes (it leaves a gap and a
	 * divider), so a hidden or collapsed pane is taken out, and put back in
	 * its place, with the dividers as they were, when shown again.
	 */
	private static void splitPaneVisibility(XulElement e, Node pane, SplitPane split, boolean hidden) {
		final String SAVED = "xul-split-saved";
		ObservableList<Node> items = split.getItems();
		if (hidden && items.contains(pane)) {
			pane.getProperties().put(SAVED, new Object[] { e.parent.children.indexOf(e), split.getDividerPositions() });
			items.remove(pane);
		} else if (!hidden && !items.contains(pane)) {
			Object[] saved = (Object[]) pane.getProperties().remove(SAVED);
			int at = 0;
			for (XulElement sibling : e.parent.children) {   // its place among the panes still there
				if (sibling == e) {
					break;
				}
				if (sibling.node instanceof Node && items.contains(sibling.node)) {
					at++;
				}
			}
			items.add(Math.min(at, items.size()), pane);
			if (saved != null && ((double[]) saved[1]).length == items.size() - 1) {
				split.setDividerPositions((double[]) saved[1]);
			}
		}
	}

	// ---- Labels and buttons ----

	/**
	 * XUL's accesskey="N" underlines the N and makes Alt+N work. JavaFX takes
	 * a "_" before the letter instead, so insert one (and double any "_"
	 * already there, which JavaFX would otherwise take as a mnemonic).
	 */
	static String mnemonic(String label, String accesskey, boolean parsing) {
		if (!parsing) {
			return label;
		}
		String escaped = label.replace("_", "__");
		if (accesskey == null || accesskey.isEmpty()) {
			return escaped;
		}
		int at = escaped.toLowerCase().indexOf(accesskey.toLowerCase().charAt(0));
		while (at > 0 && escaped.charAt(at - 1) == '_') {   // inside an escaped "__": look further on
			at = escaped.toLowerCase().indexOf(accesskey.toLowerCase().charAt(0), at + 1);
		}
		return at < 0 ? escaped : escaped.substring(0, at) + "_" + escaped.substring(at);
	}

	private static void setLabel(Labeled node, String label, String accesskey) {
		boolean parsing = accesskey != null && !accesskey.isEmpty();
		node.setMnemonicParsing(parsing);
		node.setText(mnemonic(label, accesskey, parsing));
	}

	private Object labeled(Labeled node, XulElement e) {
		setLabel(node, e.attr("label", ""), e.attr("accesskey"));
		if (e.attributes.containsKey("image")) {
			node.setGraphic(new ImageView(new Image(url(e, e.attr("image")))));
		}
		if ("vertical".equals(e.attr("orient"))) {
			node.setContentDisplay(ContentDisplay.TOP);   // the image above the label
		}
		if (node instanceof CheckBox) {
			((CheckBox) node).setSelected(e.flag("checked"));
		} else if (node instanceof Toggle) {
			((Toggle) node).setSelected(e.flag("checked"));
		}
		return node;
	}

	private Object label(XulElement e, boolean wrap) {
		String text = e.attr("value", e.attr("label", e.text.trim().replaceAll("\\s+", " ")));
		Label label = new Label(text);
		label.setWrapText(wrap || !e.attributes.containsKey("value") && !e.attributes.containsKey("label") && !text.isEmpty());
		if (e.attributes.containsKey("accesskey")) {
			setLabel(label, text, e.attr("accesskey"));
		}
		if ("statusbarpanel".equals(e.tag)) {
			label.getStyleClass().add("xul-statusbarpanel");
			label.setMaxHeight(Double.MAX_VALUE);
		}
		return label;
	}

	private Object toolbarbutton(XulElement e, XulWindow w) {
		Labeled button = "checkbox".equals(e.attr("type")) ? new ToggleButton() : new Button();
		button.setFocusTraversable(false);
		button.getStyleClass().add("xul-toolbarbutton");
		return labeled(button, e);
	}

	private Object textbox(XulElement e, XulWindow w) {
		TextInputControl text;
		if (e.flag("multiline")) {
			TextArea area = new TextArea();
			area.setWrapText(!"off".equals(e.attr("wrap")) && !"false".equals(e.attr("wrap")));
			if (e.attributes.containsKey("rows")) {
				area.setPrefRowCount((int) e.number("rows", 10));
			}
			if (e.attributes.containsKey("cols")) {
				area.setPrefColumnCount((int) e.number("cols", 40));
			}
			text = area;
		} else {
			TextField field = "password".equals(e.attr("type")) ? new PasswordField() : new TextField();
			if (e.attributes.containsKey("size")) {
				field.setPrefColumnCount((int) e.number("size", 12));
			}
			text = field;
		}
		text.setText(e.attr("value", ""));
		text.setEditable(!e.flag("readonly"));
		if (e.attributes.containsKey("placeholder")) {
			text.setPromptText(e.attr("placeholder"));
		}
		return text;
	}

	private Object separator(XulElement e, XulWindow w) {
		boolean inRow = e.parent != null && (e.parent.tag.equals("hbox") || e.parent.tag.equals("toolbar")
				|| (e.parent.tag.equals("box") && !"vertical".equals(e.parent.attr("orient"))));
		if ("vertical".equals(e.attr("orient"))) {
			inRow = true;
		} else if ("horizontal".equals(e.attr("orient"))) {
			inRow = false;
		}
		return new Separator(inRow ? Orientation.VERTICAL : Orientation.HORIZONTAL);
	}

	private Object image(XulElement e, XulWindow w) {
		ImageView view = new ImageView();
		if (e.attributes.containsKey("src")) {
			view.setImage(new Image(url(e, e.attr("src"))));
		}
		if (e.attributes.containsKey("width")) {
			view.setFitWidth(e.number("width", 0));
		}
		if (e.attributes.containsKey("height")) {
			view.setFitHeight(e.number("height", 0));
		}
		view.setPreserveRatio(true);
		return view;
	}

	/**
	 * <browser src="start.html"/> (or <iframe>): JavaFX's WebView, a WebKit
	 * browser. A src without a scheme is a file relative to the .xul file.
	 */
	private Object browser(XulElement e, XulWindow w) {
		WebView view = new WebView();
		view.setMinSize(0, 0);
		if (e.attributes.containsKey("src")) {
			view.getEngine().load(url(e, e.attr("src")));
		}
		return view;
	}

	/** A URL as written, or for one without a scheme, a file relative to the .xul file. */
	static String url(XulElement e, String src) {
		if (src.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
			return src;
		}
		return e.file.resolveSibling(src).toUri().toString();
	}

	private Object progressmeter(XulElement e, XulWindow w) {
		ProgressBar bar = new ProgressBar("undetermined".equals(e.attr("mode")) ? ProgressBar.INDETERMINATE_PROGRESS
				: Math.max(0, Math.min(100, e.number("value", 0))) / 100);
		bar.setMaxWidth(Double.MAX_VALUE);
		return bar;
	}

	// ---- Boxes ----

	/**
	 * A box, or a SplitPane when it has <splitter> children: the children
	 * between splitters become the split pane's panes.
	 */
	private Object box(XulElement e, XulWindow w, Orientation orientation) {
		if (e.firstChild("splitter") != null) {
			return split(e, w, orientation);
		}
		XulBox box = new XulBox(orientation);
		box.setAlign(e.attr("align"));
		box.setPack(e.attr("pack"));
		if (e.attributes.containsKey("spacing")) {
			box.setSpacing(e.number("spacing", 0));
		}
		addChildren(e, w, box.getChildren());
		box.getStyleClass().add("xul-" + e.tag);
		return box;
	}

	private void addChildren(XulElement e, XulWindow w, ObservableList<Node> into) {
		for (XulElement child : e.children) {
			Object node = w.build(child);
			if (node instanceof Node) {
				into.add((Node) node);
			} else if (node != null) {
				throw child.error("<" + child.tag + "> can't go inside <" + e.tag + ">.");
			}
		}
	}

	private Object split(XulElement e, XulWindow w, Orientation orientation) {
		SplitPane split = new SplitPane();
		split.setOrientation(orientation);
		List<List<XulElement>> panes = new ArrayList<>();
		panes.add(new ArrayList<>());
		for (XulElement child : e.children) {
			if (child.tag.equals("splitter")) {
				panes.add(new ArrayList<>());
			} else {
				panes.get(panes.size() - 1).add(child);
			}
		}
		double[] flex = new double[panes.size()];
		double total = 0;
		for (int i = 0; i < panes.size(); i++) {
			List<XulElement> group = panes.get(i);
			Node pane;
			if (group.size() == 1) {
				Object node = w.build(group.get(0));
				if (!(node instanceof Node)) {
					throw group.get(0).error("<" + group.get(0).tag + "> can't be a pane between splitters.");
				}
				pane = (Node) node;
			} else {
				XulBox wrapper = new XulBox(orientation);
				for (XulElement child : group) {
					Object node = w.build(child);
					if (node instanceof Node) {
						wrapper.getChildren().add((Node) node);
					}
				}
				pane = wrapper;
			}
			flex[i] = 0;
			for (XulElement child : group) {
				flex[i] += child.number("flex", 0);
			}
			if (flex[i] == 0) {
				SplitPane.setResizableWithParent(pane, false);
			}
			total += Math.max(flex[i], 0.0001);
			split.getItems().add(pane);
		}
		// Start the dividers in proportion to the panes' flex (equal if none flex).
		double[] positions = new double[panes.size() - 1];
		double sum = 0;
		boolean anyFlex = total > panes.size() * 0.0001 * 1.5;
		for (int i = 0; i < positions.length; i++) {
			sum += anyFlex ? Math.max(flex[i], 0.0001) / total : 1.0 / panes.size();
			positions[i] = sum;
		}
		split.setDividerPositions(positions);
		return split;
	}

	private Object statusbar(XulElement e, XulWindow w) {
		XulBox bar = (XulBox) box(e, w, Orientation.HORIZONTAL);
		bar.setSpacing(4);
		bar.getStyleClass().add("xul-statusbar");
		return bar;
	}

	private Object radiogroup(XulElement e, XulWindow w) {
		XulBox box = new XulBox("horizontal".equals(e.attr("orient")) ? Orientation.HORIZONTAL : Orientation.VERTICAL);
		box.setSpacing(e.number("spacing", 4));
		ToggleGroup group = new ToggleGroup();
		addChildren(e, w, box.getChildren());
		for (Node child : box.getChildren()) {
			if (child instanceof RadioButton) {
				((RadioButton) child).setToggleGroup(group);
			}
		}
		return box;
	}

	private Object groupbox(XulElement e, XulWindow w) {
		XulElement caption = e.firstChild("caption");
		XulBox content = new XulBox("horizontal".equals(e.attr("orient")) ? Orientation.HORIZONTAL : Orientation.VERTICAL);
		content.setSpacing(e.number("spacing", 4));
		for (XulElement child : e.children) {
			if (child != caption) {
				Object node = w.build(child);
				if (node instanceof Node) {
					content.getChildren().add((Node) node);
				}
			}
		}
		TitledPane pane = new TitledPane(caption == null ? "" : caption.attr("label", caption.text.trim()), content);
		pane.setCollapsible(false);
		if (caption != null) {
			caption.window = w;
			caption.node = pane;
		}
		pane.setMaxHeight(Double.MAX_VALUE);
		return pane;
	}

	private Object toolbar(XulElement e, XulWindow w) {
		ToolBar bar = new ToolBar();
		for (XulElement child : e.children) {
			Object node = w.build(child);
			if (node instanceof Node) {
				if (child.tag.equals("spacer") || child.number("flex", 0) > 0) {
					HBox.setHgrow((Node) node, Priority.ALWAYS);
				}
				bar.getItems().add((Node) node);
			}
		}
		return bar;
	}

	// ---- Menus ----

	private Object menubar(XulElement e, XulWindow w) {
		MenuBar bar = new MenuBar();
		for (XulElement child : e.children) {
			Object menu = w.build(child);
			if (!(menu instanceof Menu)) {
				throw child.error("Only <menu> can go inside <menubar>.");
			}
			bar.getMenus().add((Menu) menu);
		}
		return bar;
	}

	private Object menu(XulElement e, XulWindow w) {
		Menu menu = new Menu(mnemonic(e.attr("label", ""), e.attr("accesskey"), true));
		XulElement popup = e.firstChild("menupopup");
		if (popup != null) {
			popup.window = w;
			popup.node = menu;
			Map<String, ToggleGroup> groups = new HashMap<>();
			for (XulElement child : popup.children) {
				menu.getItems().add(menuItem(child, w, groups));
			}
		}
		w.menus.add(menu);
		return menu;
	}

	/** Builds one item of a menu's or a context menu's <menupopup>. */
	private static MenuItem menuItem(XulElement child, XulWindow w, Map<String, ToggleGroup> groups) {
		Object item = w.build(child);
		if (!(item instanceof MenuItem)) {
			throw child.error("Only <menuitem>, <menu> and <menuseparator> can go inside a <menupopup>.");
		}
		if (item instanceof RadioMenuItem) {
			String name = child.attr("name", "");
			((RadioMenuItem) item).setToggleGroup(groups.computeIfAbsent(name, k -> new ToggleGroup()));
		}
		if (child.attributes.containsKey("accesskey") && !child.attr("accesskey").isEmpty()) {
			((MenuItem) item).getProperties().put(XulWindow.ACCESSKEY, Character.toLowerCase(child.attr("accesskey").charAt(0)));
		}
		return (MenuItem) item;
	}

	private Object menuitem(XulElement e, XulWindow w) {
		String type = e.attr("type", "");
		MenuItem item = type.equals("checkbox") ? new CheckMenuItem() : type.equals("radio") ? new RadioMenuItem(null) : new MenuItem();
		item.setText(mnemonic(e.attr("label", ""), e.attr("accesskey"), true));
		if (e.attributes.containsKey("checked")) {
			e.node = item;
			applyAttribute(e, "checked", e.attr("checked"));
		}
		return item;
	}

	/** A drop-down list: <menulist><menupopup><menuitem label="..."/>...</menupopup></menulist>. */
	private Object menulist(XulElement e, XulWindow w) {
		ComboBox<String> box = new ComboBox<>();
		XulElement popup = e.firstChild("menupopup");
		int selected = -1;
		if (popup != null) {
			for (XulElement item : popup.childrenNamed("menuitem")) {
				box.getItems().add(item.attr("label", ""));
				if (item.flag("selected")) {
					selected = box.getItems().size() - 1;
				}
			}
		}
		box.getSelectionModel().select(selected >= 0 ? selected : 0);
		if (e.attributes.containsKey("value")) {
			box.setValue(e.attr("value"));
		}
		box.setEditable(e.flag("editable"));
		return box;
	}

	// ---- Lists, trees, tabs, grids ----

	private Object listbox(XulElement e, XulWindow w) {
		ListView<String> list = new ListView<>();
		int selected = -1;
		for (XulElement item : e.childrenNamed("listitem")) {
			list.getItems().add(item.attr("label", item.text.trim()));
			if (item.flag("selected")) {
				selected = list.getItems().size() - 1;
			}
		}
		if (selected >= 0) {
			list.getSelectionModel().select(selected);
		}
		if (e.attributes.containsKey("rows")) {
			list.setPrefHeight(e.number("rows", 5) * 24 + 2);
		}
		return list;
	}

	/**
	 * <tree> with <treechildren>/<treeitem>/<treerow>/<treecell label="..."/>:
	 * a TreeView showing each row's first cell. container="true" with
	 * open="true" starts expanded.
	 */
	private Object tree(XulElement e, XulWindow w) {
		TreeItem<String> root = new TreeItem<>("");
		root.setExpanded(true);
		XulElement children = e.firstChild("treechildren");
		if (children != null) {
			addTreeItems(children, root);
		}
		TreeView<String> tree = new TreeView<>(root);
		tree.setShowRoot(false);
		if (e.attributes.containsKey("rows")) {
			tree.setPrefHeight(e.number("rows", 5) * 24 + 2);
		}
		return tree;
	}

	private void addTreeItems(XulElement treechildren, TreeItem<String> into) {
		for (XulElement item : treechildren.childrenNamed("treeitem")) {
			XulElement row = item.firstChild("treerow");
			XulElement cell = row == null ? null : row.firstChild("treecell");
			TreeItem<String> node = new TreeItem<>(cell == null ? item.attr("label", "") : cell.attr("label", ""));
			node.setExpanded(item.flag("open"));
			XulElement sub = item.firstChild("treechildren");
			if (sub != null) {
				addTreeItems(sub, node);
			}
			into.getChildren().add(node);
		}
	}

	/** <tabbox><tabs><tab label="..."/></tabs><tabpanels><tabpanel>...</tabpanel></tabpanels></tabbox>. */
	private Object tabbox(XulElement e, XulWindow w) {
		TabPane pane = new TabPane();
		pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		XulElement tabs = e.firstChild("tabs");
		XulElement panels = e.firstChild("tabpanels");
		if (tabs == null) {
			throw e.error("<tabbox> needs a <tabs> element.");
		}
		List<XulElement> tabList = tabs.childrenNamed("tab");
		List<XulElement> panelList = panels == null ? Collections.<XulElement>emptyList() : panels.childrenNamed("tabpanel");
		tabs.window = w;
		tabs.node = pane;
		if (panels != null) {
			panels.window = w;
			panels.node = pane;
		}
		for (int i = 0; i < tabList.size(); i++) {
			XulElement t = tabList.get(i);
			Tab tab = new Tab(t.attr("label", ""));
			t.window = w;
			t.node = tab;
			common(t, tab, w);
			if (i < panelList.size()) {
				tab.setContent((Node) w.build(panelList.get(i)));
			}
			pane.getTabs().add(tab);
		}
		int selected = (int) (tabs.attributes.containsKey("selectedIndex") ? tabs.number("selectedIndex", 0) : e.number("selectedIndex", 0));
		pane.getSelectionModel().select(Math.max(0, Math.min(selected, pane.getTabs().size() - 1)));
		return pane;
	}

	/**
	 * <grid><columns><column flex="1"/>...</columns><rows><row>cells</row>...</rows></grid>.
	 * Columns and rows with flex grow (equally, as GridPane does); the others keep their size.
	 */
	private Object grid(XulElement e, XulWindow w) {
		GridPane grid = new GridPane();
		grid.setHgap(e.number("spacing", 4));
		grid.setVgap(e.number("spacing", 4));
		XulElement columns = e.firstChild("columns");
		if (columns != null) {
			columns.window = w;
			for (XulElement column : columns.childrenNamed("column")) {
				ColumnConstraints c = new ColumnConstraints();
				if (column.number("flex", 0) > 0) {
					c.setHgrow(Priority.ALWAYS);
					c.setFillWidth(true);
				} else {
					c.setMinWidth(Region.USE_PREF_SIZE);   // as in XUL, a column without flex never squeezes its content
				}
				grid.getColumnConstraints().add(c);
			}
		}
		XulElement rows = e.firstChild("rows");
		if (rows == null) {
			throw e.error("<grid> needs a <rows> element.");
		}
		int r = 0;
		for (XulElement row : rows.childrenNamed("row")) {
			RowConstraints rc = new RowConstraints();
			if (row.number("flex", 0) > 0) {
				rc.setVgrow(Priority.ALWAYS);
			}
			grid.getRowConstraints().add(rc);
			row.window = w;
			int c = 0;
			for (XulElement cell : row.children) {
				Object node = w.build(cell);
				if (node instanceof Node) {
					if (node instanceof Region && !(node instanceof Label)) {
						((Region) node).setMaxWidth(Double.MAX_VALUE);
					}
					grid.add((Node) node, c, r);
					c++;
				}
			}
			r++;
		}
		return grid;
	}

	// ---- Stacks, scroll boxes, popups and context menus ----

	/** <stack>: children layered, each filling it, or placed with left and top. */
	private Object stack(XulElement e, XulWindow w) {
		XulBox.Stack stack = new XulBox.Stack();
		addChildren(e, w, stack.getChildren());
		stack.getStyleClass().add("xul-stack");
		return stack;
	}

	/** <scrollbox orient="vertical">: a box that scrolls when its children don't fit. */
	private Object scrollbox(XulElement e, XulWindow w) {
		boolean vertical = "vertical".equals(e.attr("orient"));
		XulBox box = new XulBox(vertical ? Orientation.VERTICAL : Orientation.HORIZONTAL);
		box.setAlign(e.attr("align"));
		box.setPack(e.attr("pack"));
		if (e.attributes.containsKey("spacing")) {
			box.setSpacing(e.number("spacing", 0));
		}
		addChildren(e, w, box.getChildren());
		ScrollPane scroll = new ScrollPane(box);
		scroll.setFitToWidth(vertical);
		scroll.setFitToHeight(!vertical);
		scroll.setMinSize(0, 0);
		scroll.getStyleClass().add("xul-scrollbox");
		return scroll;
	}

	/** <popupset>: holds panels and context menus, and shows nothing itself. */
	private Object popupset(XulElement e, XulWindow w) {
		for (XulElement child : e.children) {
			Object node = w.build(child);
			if (!(node instanceof PopupWindow)) {
				throw child.error("Only <panel> and <menupopup> can go inside <popupset>.");
			}
		}
		return null;
	}

	/**
	 * <panel>: a popup holding any elements, as a box (orient="horizontal"
	 * for a row). It closes when the user clicks outside it or presses
	 * Escape, unless noautohide="true". Opened with openPopup().
	 */
	private Object panel(XulElement e, XulWindow w) {
		XulBox content = new XulBox("horizontal".equals(e.attr("orient")) ? Orientation.HORIZONTAL : Orientation.VERTICAL);
		content.setAlign(e.attr("align"));
		content.setPack(e.attr("pack"));
		if (e.attributes.containsKey("spacing")) {
			content.setSpacing(e.number("spacing", 0));
		}
		addChildren(e, w, content.getChildren());
		content.getStyleClass().add("xul-panel");
		size(content, e);
		if (e.attributes.containsKey("style")) {
			for (String ignored : XulStyle.apply(e, content, e.attr("style"))) {
				w.warn(e, "style \"" + ignored + "\" isn't supported on a panel, so it was ignored.");
			}
		}
		Popup popup = new Popup();
		popup.getContent().add(content);
		popup.setAutoHide(!e.flag("noautohide"));
		popup.setHideOnEscape(true);
		popup.setAutoFix(true);
		return popup;
	}

	/** A <menupopup> outside a <menu> or <menulist> (in a <popupset>): a context menu. */
	private Object menupopup(XulElement e, XulWindow w) {
		ContextMenu menu = new ContextMenu();
		Map<String, ToggleGroup> groups = new HashMap<>();
		for (XulElement child : e.children) {
			menu.getItems().add(menuItem(child, w, groups));
		}
		w.popupMenus.add(menu);
		return menu;
	}

	/**
	 * context="menu-id": right-clicking the element (or the menu key) opens
	 * that <menupopup> or <panel> at the pointer, with document.popupNode
	 * set to the element. The id is looked up each time, so it can change.
	 */
	private static void context(XulElement e, Node node) {
		final String INSTALLED = "xul-context";
		if (node.getProperties().containsKey(INSTALLED)) {
			return;
		}
		node.getProperties().put(INSTALLED, Boolean.TRUE);
		node.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, ev -> {
			String id = e.attr("context");
			XulElement popup = id == null || e.window == null ? null : e.window.document.getElementById(id);
			if (popup == null || !(popup.node instanceof PopupWindow)) {
				if (id != null && e.window != null) {
					e.window.warn(e, "context=\"" + id + "\" names no <menupopup> or <panel> in a <popupset>.");
				}
				return;
			}
			ev.consume();
			e.window.document.popupNode = e;
			((PopupWindow) popup.node).hide();
			showPopup(popup, (PopupWindow) popup.node, node, ev.getScreenX(), ev.getScreenY());
		});
	}

	/** Shows a panel or context menu at a point on screen, with the window's styles. */
	static void showPopup(XulElement e, PopupWindow popup, Node owner, double x, double y) {
		if (popup instanceof ContextMenu) {
			((ContextMenu) popup).show(owner, x, y);
			return;
		}
		Node content = ((Popup) popup).getContent().get(0);
		if (owner.getScene() != null && popup.getScene() != null) {
			popup.getScene().getStylesheets().setAll(owner.getScene().getStylesheets());
		}
		popup.show(owner, x, y);
		content.applyCss();
		content.autosize();   // a Popup doesn't size its content itself
		popup.sizeToScene();
	}

	// ---- Adding and removing children after the window is built (appendChild, removeChild) ----

	/** Where an element's children's JavaFX objects go: a list of Nodes, or of MenuItems. */
	private static List<?> items(XulElement parent) {
		Object node = parent.node;
		if (node instanceof ToolBar) {
			return ((ToolBar) node).getItems();
		} else if (node instanceof ScrollPane && ((ScrollPane) node).getContent() instanceof Pane) {
			return ((Pane) ((ScrollPane) node).getContent()).getChildren();
		} else if (node instanceof Popup) {
			return ((Pane) ((Popup) node).getContent().get(0)).getChildren();
		} else if (node instanceof ContextMenu) {
			return ((ContextMenu) node).getItems();
		} else if (node instanceof Menu) {
			return ((Menu) node).getItems();
		} else if (node instanceof SplitPane || node instanceof GridPane || node instanceof TitledPane) {
			return null;
		} else if (node instanceof Pane) {
			return ((Pane) node).getChildren();
		}
		return null;
	}

	/** Shows a child just added to parent's children (insertBefore), in its place among its siblings. */
	@SuppressWarnings("unchecked")
	static void insert(XulElement parent, XulElement child) {
		List<?> items = items(parent);
		if (items == null) {
			throw new UnsupportedOperationException("Elements can be added to boxes, stacks, toolbars, scrollboxes, panels and menupopups, not to " + parent + ".");
		}
		boolean menu = parent.node instanceof ContextMenu || parent.node instanceof Menu;
		if (child.node == null) {
			return;   // a non-visual element: nothing to show
		}
		if (menu != child.node instanceof MenuItem || !menu && !(child.node instanceof Node)) {
			throw new IllegalArgumentException(child + " can't go inside " + parent + ".");
		}
		int at = 0;
		for (XulElement sibling : parent.children) {
			if (sibling == child) {
				break;
			}
			if (sibling.node != null && items.contains(sibling.node)) {
				at++;
			}
		}
		if (menu) {
			((List<MenuItem>) items).add(at, (MenuItem) child.node);
			if (child.attributes.containsKey("accesskey") && !child.attr("accesskey").isEmpty()) {
				((MenuItem) child.node).getProperties().put(XulWindow.ACCESSKEY, Character.toLowerCase(child.attr("accesskey").charAt(0)));
			}
		} else {
			if (parent.node instanceof ToolBar && (child.tag.equals("spacer") || child.number("flex", 0) > 0)) {
				HBox.setHgrow((Node) child.node, Priority.ALWAYS);
			}
			((List<Node>) items).add(at, (Node) child.node);
		}
	}

	static void remove(XulElement parent, XulElement child) {
		List<?> items = items(parent);
		if (items != null && child.node != null) {
			items.remove(child.node);
		}
	}
}
