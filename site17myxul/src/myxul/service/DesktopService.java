package myxul.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import myxul.Main;
import myxul.XulEngine;
import myxul.XulWindow;

/**
 * The desktop (scripts call it Desktop): ui/desktop.xul asks for it, and
 * from then on every window myxul opens is shown on the desktop, in a frame
 * like Skylight OS's: a title bar with the app's icon, minimize, maximize
 * and close, edges to resize it by, and kept within the desktop's area
 * below its panel. The desktop's script draws the rest (the home menu, the
 * taskbar, shortcuts, the clock) from what this service tells it: the apps
 * in apps/, and the windows that are open.
 *
 * Each app window is still a window of its own, drawn without the system's
 * frame and kept in front of the desktop, so its keys, menus and dialogs
 * work as they do anywhere else.
 */
public class DesktopService implements XulService, XulEngine.WindowHost {

	private static final double BAR = 30;      // the title bar's height
	private static final double EDGE = 4;      // the frame's edges, which resize it
	private static final double CORNER = 14;   // the corners that resize both ways
	private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
	private static final PseudoClass MAXIMIZED = PseudoClass.getPseudoClass("maximized");

	/** An app the desktop can open: a folder in apps/ with an app.properties, listed in apps/apps.txt. */
	public static final class App {
		private final String id;
		private final Properties manifest;
		private final Path folder;

		App(String id, Path folder, Properties manifest) {
			this.id = id;
			this.folder = folder;
			this.manifest = manifest;
		}

		/** The app's folder name, such as "terminal". */
		public String getId() {
			return id;
		}

		public String getTitle() {
			return manifest.getProperty("title");
		}

		public String getDescription() {
			return manifest.getProperty("description", "");
		}

		/** The 48-pixel icon, as a URL for an image attribute. */
		public String getIcon() {
			return folder.resolve(manifest.getProperty("icon")).toUri().toString();
		}

		/** The 16-pixel icon, for title bars and the taskbar. */
		public String getSmallIcon() {
			return folder.resolve(manifest.getProperty("icon.small")).toUri().toString();
		}

		Path xul() {
			return folder.resolve(manifest.getProperty("xul"));
		}

		@Override
		public String toString() {
			return getTitle();
		}
	}

	/** One window on the desktop: its frame, and what the taskbar needs. */
	public final class Frame {
		private final XulWindow window;
		private final int id;
		private final App app;
		private final BorderPane root;
		private final Label title = new Label();
		private Rectangle2D restored;   // its bounds before it was maximized
		private boolean placed;

		Frame(XulWindow window, Parent content) {
			this.window = window;
			this.id = nextId++;
			this.app = appOf(window.getFile());
			Stage stage = window.getStage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initOwner(desktop.getStage());   // in front of the desktop, and minimized with it
			stage.getIcons().add(new Image(getIcon()));

			ImageView icon = new ImageView(new Image(getIcon()));
			icon.setFitWidth(16);
			icon.setFitHeight(16);
			title.getStyleClass().add("desktop-frame-title");
			title.textProperty().bind(stage.titleProperty());
			title.setMinWidth(0);
			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);
			Button min = barButton("min", "Minimize", this::minimize);
			Button max = barButton("max", "Maximize", this::toggleMaximized);
			Button close = barButton("close", "Close", this::close);
			close.getStyleClass().add("desktop-frame-close");
			HBox bar = new HBox(6, icon, title, spacer, min, max, close);
			bar.setAlignment(Pos.CENTER_LEFT);
			bar.setMinHeight(BAR);
			bar.setPrefHeight(BAR);
			bar.getStyleClass().add("desktop-frame-bar");
			dragging(bar);

			content.getStyleClass().add("desktop-frame-content");
			root = new BorderPane(content);
			root.setTop(bar);
			root.getStyleClass().add("desktop-frame");
			resizing();
			double width = window.getRequestedWidth();
			double height = window.getRequestedHeight();
			if (width > 0 && height > 0) {
				root.setPrefSize(width + 2 * EDGE, height + BAR + EDGE);
			}

			// Placed in a cascade from the desktop's top left; checked against the area once its size is known.
			Rectangle2D area = area();
			int step = cascade++ % 8;
			stage.setX(area.getMinX() + 24 + 28 * step);
			stage.setY(area.getMinY() + 16 + 28 * step);

			stage.titleProperty().addListener((o, was, now) -> changed());
			stage.focusedProperty().addListener((o, was, focused) -> {
				if (focused && !window.isMinimized()) {
					active = this;
					changed();
				}
			});
			stage.addEventHandler(WindowEvent.WINDOW_SHOWN, ev -> {
				if (!placed) {
					placed = true;
					frames.add(this);
					fit();
					active = this;
				}
				changed();
			});
			window.addCloseListener(() -> {
				frames.remove(this);
				if (active == this) {
					active = null;
				}
				changed();
			});
		}

		private Button barButton(String name, String tip, Runnable action) {
			Button button = new Button();
			button.setGraphic(new ImageView(new Image(Main.home().resolve("images/desktop/" + name + ".png").toUri().toString())));
			button.getStyleClass().add("desktop-frame-button");
			button.setFocusTraversable(false);
			button.setTooltip(new Tooltip(tip));
			button.setOnAction(ev -> action.run());
			return button;
		}

		/** Dragging the title bar moves the window; double-clicking it maximizes. */
		private void dragging(HBox bar) {
			double[] grab = new double[2];
			bar.setOnMousePressed(ev -> {
				grab[0] = ev.getScreenX() - window.getStage().getX();
				grab[1] = ev.getScreenY() - window.getStage().getY();
			});
			bar.setOnMouseDragged(ev -> {
				if (restored != null || ev.getButton() != MouseButton.PRIMARY) {
					return;   // a maximized window stays put
				}
				Stage stage = window.getStage();
				Rectangle2D area = area();
				// Keep the title bar on the desktop, so the window can always be dragged back.
				stage.setX(clamp(ev.getScreenX() - grab[0], area.getMinX() - stage.getWidth() + 80, area.getMaxX() - 80));
				stage.setY(clamp(ev.getScreenY() - grab[1], area.getMinY(), area.getMaxY() - BAR));
			});
			bar.setOnMouseClicked(ev -> {
				if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
					toggleMaximized();
				}
			});
		}

		/** The frame's left, right and bottom edges and its bottom corners resize the window. */
		private void resizing() {
			Cursor[] zone = new Cursor[1];
			double[] start = new double[6];
			root.setOnMouseMoved(ev -> root.setCursor(ev.getTarget() == root ? zoneAt(ev) : Cursor.DEFAULT));
			root.setOnMouseExited(ev -> root.setCursor(Cursor.DEFAULT));
			root.setOnMousePressed(ev -> {
				zone[0] = ev.getTarget() == root && restored == null ? zoneAt(ev) : Cursor.DEFAULT;
				Stage stage = window.getStage();
				start[0] = ev.getScreenX();
				start[1] = ev.getScreenY();
				start[2] = stage.getX();
				start[3] = stage.getY();
				start[4] = stage.getWidth();
				start[5] = stage.getHeight();
			});
			root.setOnMouseDragged(ev -> {
				Cursor z = zone[0];
				if (z == null || z == Cursor.DEFAULT) {
					return;
				}
				Stage stage = window.getStage();
				double dx = ev.getScreenX() - start[0];
				double dy = ev.getScreenY() - start[1];
				double minWidth = 160;
				double minHeight = BAR + 60;
				if (z == Cursor.E_RESIZE || z == Cursor.SE_RESIZE) {
					stage.setWidth(Math.max(minWidth, start[4] + dx));
				}
				if (z == Cursor.W_RESIZE || z == Cursor.SW_RESIZE) {
					double width = Math.max(minWidth, start[4] - dx);
					stage.setX(start[2] + start[4] - width);
					stage.setWidth(width);
				}
				if (z == Cursor.S_RESIZE || z == Cursor.SE_RESIZE || z == Cursor.SW_RESIZE) {
					stage.setHeight(Math.max(minHeight, start[5] + dy));
				}
			});
		}

		private Cursor zoneAt(MouseEvent ev) {
			double w = root.getWidth();
			double h = root.getHeight();
			boolean bottom = ev.getY() >= h - CORNER;
			if (ev.getX() <= CORNER && bottom) {
				return Cursor.SW_RESIZE;
			} else if (ev.getX() >= w - CORNER && bottom) {
				return Cursor.SE_RESIZE;
			} else if (ev.getY() >= h - EDGE) {
				return Cursor.S_RESIZE;
			} else if (ev.getX() <= EDGE) {
				return Cursor.W_RESIZE;
			} else if (ev.getX() >= w - EDGE) {
				return Cursor.E_RESIZE;
			}
			return Cursor.DEFAULT;
		}

		/** Keeps the window within the desktop's area: smaller if it must be. */
		private void fit() {
			Stage stage = window.getStage();
			Rectangle2D area = area();
			if (stage.getWidth() > area.getWidth()) {
				stage.setWidth(area.getWidth());
			}
			if (stage.getHeight() > area.getHeight()) {
				stage.setHeight(area.getHeight());
			}
			stage.setX(clamp(stage.getX(), area.getMinX(), area.getMaxX() - stage.getWidth()));
			stage.setY(clamp(stage.getY(), area.getMinY(), area.getMaxY() - stage.getHeight()));
		}

		void styleActive() {
			root.pseudoClassStateChanged(ACTIVE, active == this);
			root.pseudoClassStateChanged(MAXIMIZED, restored != null);
		}

		// ---- For the desktop's script ----

		/** A number for this window, unique while the desktop is open. */
		public int getId() {
			return id;
		}

		public String getTitle() {
			String t = window.getTitle();
			return t == null ? "" : t;
		}

		/** The app's 16-pixel icon, as a URL. */
		public String getIcon() {
			return app != null ? app.getSmallIcon() : Main.home().resolve("images/desktop/window.png").toUri().toString();
		}

		/** The app this window belongs to (by its folder), or null. */
		public App getApp() {
			return app;
		}

		public boolean isMinimized() {
			return window.isMinimized();
		}

		public boolean isMaximized() {
			return restored != null;
		}

		/** The window the user was last working in, unless it was minimized since. */
		public boolean isActive() {
			return active == this;
		}

		/** Shows the window, in front, with the keyboard. */
		public void activate() {
			window.setMinimized(false);
			window.getStage().toFront();
			window.getStage().requestFocus();
			active = this;
			changed();
		}

		public void minimize() {
			window.setMinimized(true);
			if (active == this) {
				active = null;
			}
			changed();
		}

		/** What clicking its taskbar button does: minimizes the active window, or brings any other to the front. */
		public void toggle() {
			if (!window.isMinimized() && active == this) {
				minimize();
			} else {
				activate();
			}
		}

		public void toggleMaximized() {
			Stage stage = window.getStage();
			if (restored == null) {
				restored = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
				Rectangle2D area = area();
				stage.setX(area.getMinX());
				stage.setY(area.getMinY());
				stage.setWidth(area.getWidth());
				stage.setHeight(area.getHeight());
			} else {
				stage.setX(restored.getMinX());
				stage.setY(restored.getMinY());
				stage.setWidth(restored.getWidth());
				stage.setHeight(restored.getHeight());
				restored = null;
			}
			changed();
		}

		/** Asks the window to close, as its close button does: it may ask about unsaved changes first. Returns whether it closed. */
		public boolean close() {
			if (window.isMinimized()) {
				activate();
			}
			return window.requestClose();
		}

		/** The window itself, for Java code (tests). */
		public XulWindow getWindow() {
			return window;
		}
	}

	private final XulWindow desktop;
	private final XulEngine engine;
	private final List<App> apps;
	private final List<Frame> frames = new ArrayList<>();
	private Frame active;
	private Object onChange;
	private Node area;
	private int nextId = 1;
	private int cascade;
	private boolean changePending;

	public DesktopService(XulWindow window) {
		engine = window.getEngine();
		if (engine.getWindowHost() != null) {
			throw new IllegalStateException("A desktop is open already.");
		}
		desktop = window;
		apps = readApps();
		engine.setWindowHost(this);
		window.getStage().setMaximized(true);
		window.addCloseListener(() -> {
			if (engine.getWindowHost() == this) {
				engine.setWindowHost(null);
			}
			NotificationService.setArea(null);
		});
	}

	/** Every new window but the desktop's own is framed and shown on the desktop. */
	@Override
	public Parent frame(XulWindow window, Parent content) {
		if (window == desktop) {
			return content;
		}
		if (desktop.isClosed() || !desktop.getStage().isShowing()) {
			// The desktop never opened (its XUL had a mistake), or has gone: windows are plain again.
			if (engine.getWindowHost() == this) {
				engine.setWindowHost(null);
			}
			return content;
		}
		return new Frame(window, content).root;
	}

	/** apps/apps.txt lists the apps' folders, in the order the home menu shows them. */
	private static List<App> readApps() {
		Path dir = Main.home().resolve("apps");
		List<App> found = new ArrayList<>();
		List<String> order;
		try {
			order = Files.readAllLines(dir.resolve("apps.txt"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return found;
		}
		for (String line : order) {
			String id = line.trim();
			if (id.isEmpty() || id.startsWith("#") || !id.matches("[a-z0-9-]+")) {
				continue;
			}
			Path folder = dir.resolve(id);
			Properties manifest = new Properties();
			try (InputStream in = Files.newInputStream(folder.resolve("app.properties"))) {
				manifest.load(in);
			} catch (IOException e) {
				continue;   // no app.properties: not an app
			}
			if (manifest.getProperty("title") != null && manifest.getProperty("xul") != null
					&& manifest.getProperty("icon") != null && manifest.getProperty("icon.small") != null) {
				found.add(new App(id, folder, manifest));
			}
		}
		return found;
	}

	private App appOf(Path file) {
		for (App app : apps) {
			if (file.toAbsolutePath().normalize().startsWith(app.folder.toAbsolutePath().normalize())) {
				return app;
			}
		}
		return null;
	}

	/** The desktop's area (below its panel) on screen. */
	Rectangle2D area() {
		if (area != null && area.getScene() != null && area.getScene().getWindow() != null && area.getScene().getWindow().isShowing()) {
			Bounds b = area.localToScreen(area.getLayoutBounds());
			if (b != null && b.getWidth() > 0 && b.getHeight() > 0) {
				return new Rectangle2D(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
			}
		}
		return Screen.getPrimary().getVisualBounds();
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(Math.max(min, max), value));
	}

	/** Tells the desktop's script that windows changed, once, after the event that changed them. */
	private void changed() {
		for (Frame f : frames) {
			f.styleActive();
		}
		if (changePending) {
			return;
		}
		changePending = true;
		Platform.runLater(() -> {
			changePending = false;
			desktop.call(onChange);
		});
	}

	// ---- For the desktop's script ----

	/** The apps in apps/, in the order of apps/apps.txt. */
	public List<App> getApps() {
		return Collections.unmodifiableList(apps);
	}

	/** The app in apps/<id>/, or null. */
	public App getApp(String id) {
		for (App app : apps) {
			if (app.id.equals(id)) {
				return app;
			}
		}
		return null;
	}

	/** Opens an app by its folder name. Returns false if there is no such app, or its window couldn't open. */
	public boolean open(String id) {
		App app = getApp(id);
		return app != null && engine.openWindow(app.xul(), Collections.<String>emptyList()) != null;
	}

	/** The windows on the desktop, in the order they opened. */
	public List<Frame> getWindows() {
		return Collections.unmodifiableList(new ArrayList<>(frames));
	}

	/** The window with this id, or null. */
	public Frame getWindow(int id) {
		for (Frame f : frames) {
			if (f.id == id) {
				return f;
			}
		}
		return null;
	}

	/** Calls the function (with no arguments) after windows open, close, change title, or are minimized or activated. */
	public void setOnChange(Object function) {
		onChange = function;
	}

	/** The element below the panel that windows are kept within, and notifications appear in: Desktop.setArea(document.getElementById("area").node). */
	public void setArea(Object node) {
		if (!(node instanceof Node)) {
			throw new IllegalArgumentException("setArea needs an element's node.");
		}
		area = (Node) node;
		NotificationService.setArea(this::area);
	}

	/** The name of the user myxul runs as. There is no login: the desktop is theirs. */
	public String getUserName() {
		return System.getProperty("user.name", "");
	}

	/**
	 * Asks every window to close, as their close buttons do. Returns true if
	 * they all closed; false if one stayed open (it had unsaved changes, and
	 * the user said Cancel), which is then shown in front.
	 */
	public boolean quit() {
		for (Frame f : new ArrayList<>(frames)) {
			if (!f.close()) {
				f.activate();
				return false;
			}
		}
		return true;
	}
}
