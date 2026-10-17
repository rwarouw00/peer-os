package skylight;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * The Skylight OS desktop: login, top panel (Apps button, window list, clock),
 * background and shortcuts, home menu, notifications, dialogs and app windows.
 */
public final class Desktop {

	private static final String CSS = Desktop.class.getResource("skylight.css").toExternalForm();

	/** One open app: the app object, its window and its window-list button. */
	private static final class AppWindow {
		final App app;
		final Stage stage;
		final ToggleButton button;

		AppWindow(App app, Stage stage, ToggleButton button) {
			this.app = app;
			this.stage = stage;
			this.button = button;
		}
	}

	private String user;
	private Settings settings;
	private Stage stage;                  // the desktop window
	private ToolBar panel;
	private HBox windowList;
	private Label clock;
	private Timeline clockTimer;
	private Pane area;                    // background and shortcuts
	private Popup menu;
	private StackPane menuPages;
	private Node appsPage;
	private Node notificationsPage;
	private VBox notificationList;
	private final Map<String, AppWindow> windows = new LinkedHashMap<>();
	private final Map<String, Properties> manifests = new HashMap<>();
	private final List<Popup> toasts = new ArrayList<>();
	private int nextAppID = 1;

	String getUser() {
		return user;
	}

	Settings getSettings() {
		return settings;
	}

	// ---- Login and logout ----

	public void showLogin() {
		Stage login = new Stage();
		login.setTitle("Skylight OS");
		login.getIcons().add(image("images/skylight.png"));
		login.setResizable(false);

		TextField username = new TextField();
		PasswordField password = new PasswordField();
		Label error = new Label();
		error.setWrapText(true);
		error.setMaxWidth(240);
		Button quit = new Button("Quit");
		quit.setCancelButton(true);
		quit.setOnAction(e -> Platform.exit());
		Button logIn = new Button("Log In");
		logIn.setDefaultButton(true);
		logIn.setOnAction(e -> {
			if (Storage.checkLogin(username.getText(), password.getText())) {
				login.close();
				showDesktop(username.getText());
			} else {
				error.setText("Either the username or password is incorrect.");
				password.clear();
				password.requestFocus();
			}
		});
		login.setOnCloseRequest(e -> Platform.exit());

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);
		form.setPadding(new Insets(20));
		form.addRow(0, new Label("Username:"), username);
		form.addRow(1, new Label("Password:"), password);
		form.add(error, 0, 2, 2, 1);
		HBox buttons = new HBox(8, quit, logIn);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		form.add(buttons, 0, 3, 2, 1);

		login.setScene(scene(form, -1, -1));
		login.show();
	}

	void logout() {
		for (String appID : new ArrayList<>(windows.keySet())) {
			closeWindow(appID);
		}
		for (Popup toast : new ArrayList<>(toasts)) {
			removeToast(toast);
		}
		menu.hide();
		clockTimer.stop();
		stage.close();
		user = null;
		showLogin();
	}

	// ---- Building the desktop ----

	void showDesktop(String user) {
		this.user = user;
		this.settings = Storage.loadSettings(user);

		Button apps = new Button("Apps", icon("images/home.png", 22));
		apps.setOnAction(e -> toggleMenu());
		windowList = new HBox(4);
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		clock = new Label();
		panel = new ToolBar(apps, new Separator(Orientation.VERTICAL), windowList, spacer, clock);

		area = new Pane();
		area.getStyleClass().add("desktop-area");

		BorderPane root = new BorderPane(area);
		root.setTop(panel);

		stage = new Stage();
		stage.setTitle("Skylight OS");
		stage.getIcons().add(image("images/skylight.png"));
		stage.setScene(scene(root, 1024, 700));
		stage.setMaximized(true);
		stage.setOnCloseRequest(e -> {
			e.consume();
			logout();
		});
		buildMenu();
		applySettings();
		stage.show();

		tickClock();
		clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> tickClock()));
		clockTimer.setCycleCount(Timeline.INDEFINITE);
		clockTimer.play();
	}

	private void tickClock() {
		clock.setText(new SimpleDateFormat("h:mm a").format(new Date()).toLowerCase() + "   " + user);
	}

	/** A Scene with Skylight's stylesheet. Width and height of -1 mean "fit the content". */
	static Scene scene(Parent root, double width, double height) {
		Scene scene = new Scene(root, width, height);
		scene.getStylesheets().add(CSS);
		return scene;
	}

	static Image image(String relativePath) {
		return new Image(Storage.HOME.resolve(relativePath).toUri().toString());
	}

	static ImageView icon(String relativePath, double size) {
		ImageView view = new ImageView(image(relativePath));
		view.setFitWidth(size);
		view.setFitHeight(size);
		view.setPreserveRatio(true);
		view.setSmooth(true);
		return view;
	}

	// ---- Settings, background and shortcuts ----

	private void applySettings() {
		// The background is drawn by CSS: see .desktop-area in skylight.css
		area.setStyle("-fx-background-image: url('" + Storage.HOME.resolve(settings.background).toUri() + "');");
		renderShortcuts();
	}

	/** Checks, saves and applies settings. Returns false, after telling the user why, if they were refused. */
	boolean saveSettings(Settings changed) {
		String error = Storage.saveSettings(user, changed);
		if (error != null) {
			notify("Settings not saved", error);
			return false;
		}
		settings = changed.copy();
		applySettings();
		return true;
	}

	private void renderShortcuts() {
		area.getChildren().clear();
		for (Settings.Shortcut shortcut : settings.shortcuts) {
			Properties manifest = readManifest(shortcut.path);
			if (manifest == null) {
				continue;
			}
			Label label = new Label(manifest.getProperty("title"), icon(shortcut.path + manifest.getProperty("icon.64"), 48));
			label.setContentDisplay(ContentDisplay.TOP);
			label.getStyleClass().add("shortcut");
			label.relocate(shortcut.x, shortcut.y);
			makeShortcutInteractive(label, shortcut.path);
			area.getChildren().add(label);
		}
	}

	private void makeShortcutInteractive(Label item, String path) {
		double[] offset = new double[2];
		boolean[] moved = new boolean[1];
		item.setOnMousePressed(e -> {
			offset[0] = e.getX();
			offset[1] = e.getY();
			moved[0] = false;
		});
		item.setOnMouseDragged(e -> {
			Point2D point = area.sceneToLocal(e.getSceneX(), e.getSceneY());
			item.relocate(Math.max(0, point.getX() - offset[0]), Math.max(0, point.getY() - offset[1]));
			moved[0] = true;
		});
		item.setOnMouseReleased(e -> {
			if (moved[0]) {
				Settings changed = settings.copy();
				for (Settings.Shortcut shortcut : changed.shortcuts) {
					if (shortcut.path.equals(path)) {
						shortcut.x = item.getLayoutX();
						shortcut.y = item.getLayoutY();
					}
				}
				saveSettings(changed);
			}
		});
		item.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				openWindow(path, new HashMap<>());
			}
		});
		MenuItem remove = new MenuItem("Remove from desktop");
		remove.setOnAction(e -> removeShortcut(path));
		ContextMenu shortcutMenu = new ContextMenu(remove);
		item.setOnContextMenuRequested(e -> shortcutMenu.show(item, e.getScreenX(), e.getScreenY()));
	}

	void addShortcut(String path) {
		Settings changed = settings.copy();
		for (Settings.Shortcut shortcut : changed.shortcuts) {
			if (shortcut.path.equals(path)) {
				return;
			}
		}
		int count = changed.shortcuts.size();
		changed.shortcuts.add(new Settings.Shortcut(path, 20 + 100 * (count / 6), 20 + 100 * (count % 6)));
		saveSettings(changed);
	}

	private void removeShortcut(String path) {
		Settings changed = settings.copy();
		changed.shortcuts.removeIf(shortcut -> shortcut.path.equals(path));
		saveSettings(changed);
	}

	// ---- Home menu ----

	private void buildMenu() {
		TilePane apps = new TilePane(8, 8);
		apps.setPadding(new Insets(12));
		apps.setPrefColumns(4);
		for (String path : Storage.appList()) {
			Properties manifest = readManifest(path);
			if (manifest != null) {
				Button app = new Button(manifest.getProperty("title"), icon(path + manifest.getProperty("icon.48"), 48));
				app.setContentDisplay(ContentDisplay.TOP);
				app.getStyleClass().add("menu-app");
				app.setOnAction(e -> {
					menu.hide();
					openWindow(path, new HashMap<>());
				});
				apps.getChildren().add(app);
			}
		}
		appsPage = apps;

		Button clear = new Button("Clear");
		clear.setOnAction(e -> notificationList.getChildren().clear());
		HBox toolbar = new HBox(clear);
		toolbar.setAlignment(Pos.CENTER_RIGHT);
		toolbar.setPadding(new Insets(6));
		notificationList = new VBox();
		ScrollPane scroller = new ScrollPane(notificationList);
		scroller.setFitToWidth(true);
		VBox notifications = new VBox(toolbar, scroller);
		VBox.setVgrow(scroller, Priority.ALWAYS);
		notificationsPage = notifications;

		menuPages = new StackPane(appsPage, notificationsPage);
		VBox.setVgrow(menuPages, Priority.ALWAYS);

		Button showApps = new Button("Apps");
		showApps.setOnAction(e -> showMenuPage(appsPage));
		Button showNotifications = new Button("Notifications");
		showNotifications.setOnAction(e -> showMenuPage(notificationsPage));
		Button logout = new Button(null, icon("images/power.png", 20));
		logout.getStyleClass().add("flat-button");
		logout.setOnAction(e -> logout());
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox footer = new HBox(6, showApps, showNotifications, spacer, logout);
		footer.getStyleClass().add("home-menu-footer");

		VBox content = new VBox(menuPages, footer);
		content.getStyleClass().add("home-menu");
		content.setPrefSize(430, 470);

		menu = new Popup();
		menu.getContent().add(content);
		menu.setAutoHide(true);   // a click elsewhere closes it
	}

	private void toggleMenu() {
		if (menu.isShowing()) {
			menu.hide();
			return;
		}
		showMenuPage(appsPage);
		Point2D corner = panel.localToScreen(0, panel.getHeight());
		menu.show(stage, corner.getX() + 4, corner.getY() + 4);
	}

	private void showMenuPage(Node page) {
		appsPage.setVisible(page == appsPage);
		notificationsPage.setVisible(page == notificationsPage);
	}

	// ---- Notifications ----

	void notify(String header, String body) {
		notificationList.getChildren().add(0, notificationBox(header, body, "notification", item -> notificationList.getChildren().remove(item)));
		showToast(header, body);
	}

	private interface Remover {
		void remove(Node item);
	}

	/** A header, an optional body and a close button. */
	private static HBox notificationBox(String header, String body, String styleClass, Remover remover) {
		Label title = new Label(header);
		title.getStyleClass().add("notification-header");
		VBox text = new VBox(2, title);
		if (!body.isEmpty()) {
			Label message = new Label(body);
			message.setWrapText(true);
			text.getChildren().add(message);
		}
		HBox.setHgrow(text, Priority.ALWAYS);
		Button close = new Button("×");
		close.getStyleClass().add("close-button");
		HBox box = new HBox(6, text, close);
		box.getStyleClass().add(styleClass);
		close.setOnAction(e -> remover.remove(box));
		return box;
	}

	private void showToast(String header, String body) {
		Popup toast = new Popup();
		HBox content = notificationBox(header, body, "toast", item -> removeToast(toast));
		content.setPrefWidth(300);
		content.setMaxWidth(300);
		content.setOpacity(0);
		toast.getContent().add(content);
		toasts.add(toast);
		toast.show(stage);
		layoutToasts();

		FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
		fadeIn.setToValue(1);
		fadeIn.play();
		PauseTransition wait = new PauseTransition(Duration.millis(settings.notificationTime));
		wait.setOnFinished(e -> removeToast(toast));
		wait.play();
	}

	/** Stacks toasts down the right-hand side, just below the panel. */
	private void layoutToasts() {
		Point2D corner = panel.localToScreen(panel.getWidth(), panel.getHeight());
		double y = corner.getY() + 10;
		for (Popup toast : toasts) {
			toast.setX(corner.getX() - 320);
			toast.setY(y);
			y += toast.getHeight() + 10;
		}
	}

	private void removeToast(Popup toast) {
		if (toasts.remove(toast)) {
			toast.hide();
			layoutToasts();
		}
	}

	// ---- Dialogs ----

	/** With onOK: OK and Cancel, and onOK runs on OK. Without: an OK button only. */
	Alert openDialog(String message, Runnable onOK, Window owner) {
		Alert dialog = onOK == null
				? new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK)
				: new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(owner == null ? stage : owner);
		dialog.setTitle("Skylight OS");
		dialog.setHeaderText(null);
		boolean[] answered = new boolean[1];   // JavaFX can report "hidden" more than once
		dialog.setOnHidden(e -> {
			if (onOK != null && !answered[0] && dialog.getResult() == ButtonType.OK) {
				answered[0] = true;
				onOK.run();
			}
		});
		dialog.show();
		return dialog;
	}

	// ---- Apps and windows ----

	/** Reads apps/<name>/app.properties, or returns null if it's missing or incomplete. */
	Properties readManifest(String path) {
		if (!Storage.isAppPath(path)) {
			return null;
		}
		if (!manifests.containsKey(path)) {
			Properties manifest = Storage.readProperties(Storage.HOME.resolve(path).resolve("app.properties"));
			boolean complete = true;
			for (String key : new String[] {"title", "class", "icon.16", "icon.32", "icon.48", "icon.64"}) {
				complete &= manifest.getProperty(key) != null;
			}
			manifests.put(path, complete ? manifest : null);
		}
		return manifests.get(path);
	}

	/** Opens the app in folder path (e.g. "apps/terminal/"). Returns its AppID, or null. */
	String openWindow(String path, Map<String, String> args) {
		Properties manifest = readManifest(path);
		App app;
		try {
			if (manifest == null) {
				throw new IllegalArgumentException(path + " is not an app folder with a complete app.properties.");
			}
			app = loadAppClass(path, manifest.getProperty("class")).newInstance();
		} catch (Exception | LinkageError e) {
			// Our own message for a bad manifest; the full exception (e.g. ClassNotFoundException) otherwise.
			notify("Cannot open app", e instanceof IllegalArgumentException ? e.getMessage() : e.toString());
			return null;
		}
		String appID = Integer.toString(nextAppID++);

		Stage window = new Stage();
		window.initOwner(stage);   // stays in front of the desktop
		window.setTitle(manifest.getProperty("title"));
		for (String size : new String[] {"16", "32", "48", "64"}) {
			window.getIcons().add(image(path + manifest.getProperty("icon." + size)));
		}
		app.init(this, appID, path, manifest, window, args);
		window.setScene(scene(app.build(),
				Double.parseDouble(manifest.getProperty("width", "500")),
				Double.parseDouble(manifest.getProperty("height", "350"))));
		window.setOnCloseRequest(e -> {
			e.consume();
			if (app.canClose()) {
				closeWindow(appID);
			}
		});

		ToggleButton button = new ToggleButton(manifest.getProperty("title"), icon(path + manifest.getProperty("icon.16"), 16));
		button.setSelected(true);
		button.setOnAction(e -> {
			if (button.isSelected()) {
				window.show();
				window.toFront();
			} else {
				window.hide();
			}
		});
		MenuItem close = new MenuItem("Close");
		close.setOnAction(e -> closeWindow(appID));
		button.setContextMenu(new ContextMenu(close));
		windowList.getChildren().add(button);

		windows.put(appID, new AppWindow(app, window, button));
		window.show();
		window.toFront();   // some window managers open windows behind the active one
		return appID;
	}

	/** Built-in apps are on the class path; others bring their classes in apps/<name>/app.jar. */
	private Class<? extends App> loadAppClass(String path, String className) throws Exception {
		Path jar = Storage.HOME.resolve(path).resolve("app.jar");
		ClassLoader loader = Desktop.class.getClassLoader();
		if (Files.isRegularFile(jar)) {
			loader = new URLClassLoader(new URL[] {jar.toUri().toURL()}, loader);
		}
		return Class.forName(className, true, loader).asSubclass(App.class);
	}

	/** Closes a window without asking the app. */
	void closeWindow(String appID) {
		AppWindow window = windows.remove(appID);
		if (window != null) {
			window.stage.close();
			windowList.getChildren().remove(window.button);
		}
	}
}
