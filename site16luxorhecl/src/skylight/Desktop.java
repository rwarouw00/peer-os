package skylight;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * The desktop: the login window, the panel, the home menu, shortcuts,
 * notifications and app windows. The fixed parts of its windows are .xul files
 * in ui/; the parts that change (app icons, shortcuts, the window list,
 * notifications) are added in code.
 */
final class Desktop {

	/** An open app window. */
	private static final class AppWindow {
		final App app;
		final JDialog dialog;
		final JToggleButton button;   // in the window list

		AppWindow(App app, JDialog dialog, JToggleButton button) {
			this.app = app;
			this.dialog = dialog;
			this.button = button;
		}
	}

	private JFrame login;
	private JFrame frame;              // the desktop
	private String user;
	private Settings settings;
	private Image background;
	private JPanel area;               // the desktop area, with the shortcuts
	private JPanel windowList;
	private JLabel clock;
	private Timer clockTimer;
	private JPopupMenu menu;           // the home menu
	private JPanel pages;              // its apps and notifications pages
	private JPanel notificationList;
	private final Map<String, AppWindow> windows = new LinkedHashMap<>();
	private final Map<String, Properties> manifests = new HashMap<>();
	private final List<JWindow> toasts = new ArrayList<>();
	private final List<String[]> notifications = new ArrayList<>();   // newest first
	private int nextAppID = 1;

	// ---- Logging in and out ----

	void showLogin() {
		Xul.View view = loadUi("login.xul", command -> {
			if (command.equals("logIn")) {
				logIn();
			} else {
				quit();
			}
		});
		login = new JFrame("Skylight OS");
		login.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		login.getContentPane().add(view.root);
		JTextField username = view.get("username");
		JPasswordField password = view.get("password");
		username.addActionListener(e -> password.requestFocusInWindow());   // Enter
		password.addActionListener(e -> logIn());
		login.getRootPane().registerKeyboardAction(e -> quit(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		login.getRootPane().putClientProperty("view", view);
		login.pack();
		login.setResizable(false);
		login.setLocationRelativeTo(null);
		login.setVisible(true);
		username.requestFocusInWindow();
	}

	private void logIn() {
		Xul.View view = (Xul.View) login.getRootPane().getClientProperty("view");
		JTextField username = view.get("username");
		JPasswordField password = view.get("password");
		JLabel message = view.get("message");
		String name = username.getText().trim();
		if (!Storage.checkLogin(name, new String(password.getPassword()))) {
			message.setText("Wrong username or password.");
			password.setText("");
			return;
		}
		user = name;
		settings = Storage.loadSettings(user);
		login.dispose();
		login = null;
		showDesktop();
	}

	private void quit() {
		System.exit(0);
	}

	void logout() {
		menu.setVisible(false);
		for (AppWindow window : new ArrayList<>(windows.values())) {
			window.dialog.dispose();
			SwingUtilities.invokeLater(window.app.script::dispose);
		}
		windows.clear();
		for (JWindow toast : toasts) {
			toast.dispose();
		}
		toasts.clear();
		notifications.clear();
		manifests.clear();
		clockTimer.stop();
		frame.dispose();
		frame = null;
		user = null;
		showLogin();
	}

	// ---- The desktop ----

	private void showDesktop() {
		frame = new JFrame("Skylight OS");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				logout();
			}
		});

		Xul.View panel = loadUi("panel.xul", command -> toggleMenu());
		windowList = panel.get("windowList");
		windowList.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
		clock = panel.get("clock");
		menuButton = panel.get("apps");

		area = new JPanel(null) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (background != null) {   // scaled to cover the whole area
					double scale = Math.max((double) getWidth() / background.getWidth(null),
							(double) getHeight() / background.getHeight(null));
					int w = (int) Math.ceil(background.getWidth(null) * scale);
					int h = (int) Math.ceil(background.getHeight(null) * scale);
					g.drawImage(background, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
				}
			}
		};
		area.setBackground(Color.DARK_GRAY);

		frame.getContentPane().add(panel.root, BorderLayout.NORTH);
		frame.getContentPane().add(area, BorderLayout.CENTER);
		buildMenu();
		applySettings();

		clockTimer = new Timer(1000, e -> updateClock());
		updateClock();
		clockTimer.start();

		frame.setSize(1024, 700);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	private JButton menuButton;

	private void updateClock() {
		String time = new SimpleDateFormat("h:mm a", Locale.ENGLISH).format(new Date()).toLowerCase(Locale.ENGLISH);
		clock.setText(time + "   " + user + "  ");
	}

	Settings getSettings() {
		return settings;
	}

	String getUser() {
		return user;
	}

	boolean saveSettings(Settings changed) {
		String error = Storage.saveSettings(user, changed);
		if (error != null) {
			postNotification("Settings not saved", error);
			return false;
		}
		settings = changed.copy();
		applySettings();
		return true;
	}

	private void applySettings() {
		try {
			background = ImageIO.read(Storage.HOME.resolve(settings.background).toFile());
		} catch (Exception e) {
			background = null;
		}
		renderShortcuts();
		area.repaint();
	}

	// ---- Shortcuts ----

	private void renderShortcuts() {
		area.removeAll();
		for (Settings.Shortcut shortcut : settings.shortcuts) {
			Properties manifest = readManifest(shortcut.path);
			if (manifest == null) {
				continue;
			}
			JLabel item = new JLabel(manifest.getProperty("title"), icon(shortcut.path, manifest.getProperty("icon.64"), 48),
					SwingConstants.CENTER);
			item.setName("shortcut:" + shortcut.path);
			item.setVerticalTextPosition(SwingConstants.BOTTOM);
			item.setHorizontalTextPosition(SwingConstants.CENTER);
			item.setForeground(Color.WHITE);
			item.setFont(item.getFont().deriveFont(Font.BOLD));
			Dimension size = new Dimension(90, 80);
			item.setBounds((int) shortcut.x, (int) shortcut.y, size.width, size.height);
			addShortcutHandlers(item, shortcut.path);
			area.add(item);
		}
		area.revalidate();
		area.repaint();
	}

	private void addShortcutHandlers(JLabel item, String path) {
		MouseAdapter mouse = new MouseAdapter() {
			private Point grab;     // where in the shortcut the mouse was pressed
			private boolean moved;

			@Override
			public void mousePressed(MouseEvent e) {
				grab = e.getPoint();
				moved = false;
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (grab != null && SwingUtilities.isLeftMouseButton(e)) {
					Point p = SwingUtilities.convertPoint(item, e.getPoint(), area);
					item.setLocation(Math.max(0, p.x - grab.x), Math.max(0, p.y - grab.y));
					moved = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				} else if (moved) {
					Settings changed = settings.copy();
					for (Settings.Shortcut shortcut : changed.shortcuts) {
						if (shortcut.path.equals(path)) {
							shortcut.x = item.getX();
							shortcut.y = item.getY();
						}
					}
					saveSettings(changed);
				}
				grab = null;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					openWindow(path, null);
				}
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem remove = new JMenuItem("Remove from desktop");
				remove.addActionListener(a -> removeShortcut(path));
				popup.add(remove);
				popup.show(item, e.getX(), e.getY());
			}
		};
		item.addMouseListener(mouse);
		item.addMouseMotionListener(mouse);
	}

	void addShortcut(String path) {
		if (readManifest(path) == null) {
			postNotification("Cannot add shortcut", path + " is not an app folder.");
			return;
		}
		Settings changed = settings.copy();
		for (Settings.Shortcut shortcut : changed.shortcuts) {
			if (shortcut.path.equals(path)) {
				return;
			}
		}
		int count = changed.shortcuts.size();
		changed.shortcuts.add(new Settings.Shortcut(path, 10 + 100 * (count / 6), 10 + 90 * (count % 6)));
		saveSettings(changed);
	}

	private void removeShortcut(String path) {
		Settings changed = settings.copy();
		changed.shortcuts.removeIf(shortcut -> shortcut.path.equals(path));
		saveSettings(changed);
	}

	// ---- Home menu ----

	private void buildMenu() {
		Xul.View view = loadUi("menu.xul", command -> {
			switch (command) {
				case "showApps":
					((CardLayout) pages.getLayout()).show(pages, "apps");
					break;
				case "showNotifications":
					((CardLayout) pages.getLayout()).show(pages, "notifications");
					break;
				case "clearNotifications":
					notifications.clear();
					renderNotifications();
					break;
				default:
					logout();
			}
		});
		JPanel apps = new JPanel(new GridLayout(0, 4, 4, 4));
		for (String path : Storage.appList()) {
			Properties manifest = readManifest(path);
			if (manifest != null) {
				JButton app = new JButton(manifest.getProperty("title"), icon(path, manifest.getProperty("icon.48"), 48));
				app.setName("menu:" + path);
				app.setVerticalTextPosition(SwingConstants.BOTTOM);
				app.setHorizontalTextPosition(SwingConstants.CENTER);
				app.setMargin(new Insets(4, 2, 4, 2));
				app.setBorderPainted(false);
				app.setContentAreaFilled(false);
				app.addActionListener(e -> {
					menu.setVisible(false);
					openWindow(path, null);
				});
				apps.add(app);
			}
		}
		notificationList = new JPanel();
		notificationList.setLayout(new BoxLayout(notificationList, BoxLayout.Y_AXIS));
		JPanel notificationsPage = new JPanel(new BorderLayout());
		notificationsPage.add(new JScrollPane(notificationList), BorderLayout.CENTER);
		pages = view.get("pages");
		pages.setLayout(new CardLayout());
		JPanel appsPage = new JPanel(new BorderLayout());
		appsPage.add(apps, BorderLayout.NORTH);   // rows at the top, not stretched
		pages.add(appsPage, "apps");
		pages.add(notificationsPage, "notifications");
		pages.setPreferredSize(new Dimension(420, 300));
		menu = new JPopupMenu();
		menu.add(view.root);
	}

	private void toggleMenu() {
		if (menu.isVisible()) {
			menu.setVisible(false);
		} else {
			((CardLayout) pages.getLayout()).show(pages, "apps");
			menu.show(menuButton, 0, menuButton.getHeight());
		}
	}

	// ---- Notifications ----

	void postNotification(String header, String body) {
		if (frame == null) {
			return;
		}
		notifications.add(0, new String[] {header, body});
		renderNotifications();
		showToast(header, body);
	}

	private void renderNotifications() {
		notificationList.removeAll();
		for (String[] notification : notifications) {
			notificationList.add(notificationView(notification[0], notification[1], () -> {
				notifications.remove(notification);
				renderNotifications();
			}).root);
		}
		notificationList.revalidate();
		notificationList.repaint();
	}

	/** One notification, from ui/notification.xul; its × button runs onClose. */
	private Xul.View notificationView(String header, String body, Runnable onClose) {
		Xul.View view = loadUi("notification.xul", command -> onClose.run());
		view.<JLabel>get("header").setText(header);
		view.<JLabel>get("body").setText(body);
		return view;
	}

	private void showToast(String header, String body) {
		JWindow toast = new JWindow(frame);
		toast.getContentPane().add(notificationView(header, body, () -> closeToast(toast)).root);
		toast.getRootPane().setBorder(BorderFactory.createLineBorder(Color.GRAY));
		toast.pack();
		toast.setSize(Math.max(300, toast.getWidth()), toast.getHeight());
		toasts.add(toast);
		placeToasts();
		toast.setVisible(true);
		Timer timer = new Timer(settings.notificationTime, e -> closeToast(toast));
		timer.setRepeats(false);
		timer.start();
	}

	private void closeToast(JWindow toast) {
		if (toasts.remove(toast)) {
			toast.dispose();
			placeToasts();
		}
	}

	private void placeToasts() {   // top right, under the panel, newest at the top
		Rectangle screen = area.isShowing() ? new Rectangle(area.getLocationOnScreen(), area.getSize()) : frame.getBounds();
		int y = screen.y + 8;
		for (int i = toasts.size() - 1; i >= 0; i--) {
			JWindow toast = toasts.get(i);
			toast.setLocation(screen.x + screen.width - toast.getWidth() - 8, y);
			y += toast.getHeight() + 8;
		}
	}

	// ---- Dialogs ----

	void openDialog(App app, String message, String onOK) {
		JOptionPane pane = new JOptionPane(message, onOK == null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.QUESTION_MESSAGE,
				onOK == null ? JOptionPane.DEFAULT_OPTION : JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(app.window != null ? app.window : frame, app.manifest.getProperty("title"));
		dialog.setModal(false);   // the rest of the desktop keeps working
		boolean[] answered = {false};
		pane.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, e -> {   // OK, Cancel or closed
			if (!answered[0] && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
				answered[0] = true;
				dialog.dispose();
				if (onOK != null && Integer.valueOf(JOptionPane.OK_OPTION).equals(e.getNewValue())) {
					runScript(app, () -> app.script.eval(onOK));
				}
			}
		});
		dialog.setVisible(true);
	}

	// ---- Apps and windows ----

	/** An app's app.properties, read once per login; null if it's missing or incomplete. */
	Properties readManifest(String path) {
		if (!manifests.containsKey(path)) {
			Properties manifest = null;
			if (Storage.isAppPath(path)) {
				Properties p = Storage.readProperties(Storage.HOME.resolve(path).resolve("app.properties"));
				boolean complete = p.getProperty("title") != null;
				for (String size : new String[] {"16", "32", "48", "64"}) {
					complete &= p.getProperty("icon." + size) != null;
				}
				manifest = complete ? p : null;
			}
			manifests.put(path, manifest);
		}
		return manifests.get(path);
	}

	/** Opens an app in a new window and returns its AppID, or null (after telling the user why). */
	String openWindow(String path, Map<String, String> args) {
		Properties manifest = readManifest(path);
		if (manifest == null) {
			postNotification("Cannot open app", path + " is not an app folder with a complete app.properties.");
			return null;
		}
		String appID = String.valueOf(nextAppID++);
		App app = new App(this, appID, path, manifest,
				args == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(new HashMap<>(args)));

		// The window's contents, from the .xul file; each command calls the script's proc of the same name.
		Xul.View view;
		try {
			view = Xul.load(path + manifest.getProperty("xul", "app.xul"),
					command -> runScript(app, () -> app.script.call(command)));
		} catch (Xul.XulException e) {
			postNotification("Cannot open app", e.getMessage());
			return null;
		}

		JDialog dialog = new JDialog(frame, manifest.getProperty("title"), false);
		app.window = dialog;
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.getContentPane().add(view.root);
		List<Image> icons = new ArrayList<>();
		for (String size : new String[] {"16", "32", "48", "64"}) {
			ImageIcon icon = icon(path, manifest.getProperty("icon." + size), 0);
			if (icon != null) {
				icons.add(icon.getImage());
			}
		}
		dialog.setIconImages(icons);

		// The script runs once, with the window's controls as variables.
		Path file = Storage.HOME.resolve(path).resolve(manifest.getProperty("script", "app.hcl"));
		try {
			app.script = new Script(app, view.ids);
			if (Files.exists(file)) {
				app.script.run(file);
			}
		} catch (Exception e) {
			dialog.dispose();
			if (app.script != null) {
				app.script.dispose();
			}
			postNotification("Cannot open app", Script.describe(e));
			return null;
		}

		JToggleButton button = new JToggleButton(manifest.getProperty("title"), icon(path, manifest.getProperty("icon.16"), 16));
		button.setName("window:" + appID);
		button.addActionListener(e -> toggleWindow(appID));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				popup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				popup(e);
			}

			private void popup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem close = new JMenuItem("Close");
					close.addActionListener(a -> requestClose(appID));
					popup.add(close);
					popup.show(button, e.getX(), e.getY());
				}
			}
		});
		windowList.add(button);
		windowList.revalidate();
		windows.put(appID, new AppWindow(app, dialog, button));

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				requestClose(appID);
			}
		});
		int width = number(manifest.getProperty("width"), 500);
		int height = number(manifest.getProperty("height"), 350);
		view.root.setPreferredSize(new Dimension(width, height));
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
		dialog.toFront();
		button.setSelected(true);
		return appID;
	}

	/** Something that runs Hecl, and may fail. */
	interface ScriptAction {
		void run() throws Exception;
	}

	/** Runs part of an app's script; a Hecl error becomes a notification. */
	void runScript(App app, ScriptAction code) {
		try {
			code.run();
		} catch (Exception e) {
			postNotification("Error in " + app.manifest.getProperty("title"), Script.describe(e));
		}
	}

	/** The window list's button: shows a hidden window or brings it to the front, or hides the front one. */
	private void toggleWindow(String appID) {
		AppWindow window = windows.get(appID);
		if (window.dialog.isVisible() && window.dialog.isFocused()) {
			window.dialog.setVisible(false);
			window.button.setSelected(false);
		} else {
			window.dialog.setVisible(true);
			window.dialog.toFront();
			window.button.setSelected(true);
		}
	}

	/** Closes a window if its app agrees (setCanClose). */
	private void requestClose(String appID) {
		AppWindow window = windows.get(appID);
		if (window == null) {
			return;
		}
		String check = window.app.closeCheck;
		boolean[] ok = {true};
		if (check != null) {
			runScript(window.app, () -> ok[0] = window.app.script.test(check));
		}
		if (ok[0]) {
			closeWindow(appID);
		}
	}

	void closeWindow(String appID) {
		AppWindow window = windows.remove(appID);
		if (window != null) {
			window.dialog.dispose();
			windowList.remove(window.button);
			windowList.revalidate();
			windowList.repaint();
			// The window may be closing itself from its own script ($app closeWindow),
			// so free its interpreter once that Hecl has finished.
			SwingUtilities.invokeLater(window.app.script::dispose);
		}
	}

	// ---- Helpers ----

	private Xul.View loadUi(String name, java.util.function.Consumer<String> onCommand) {
		try {
			return Xul.load("ui/" + name, onCommand);
		} catch (Xul.XulException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/** An app's icon, scaled to size (0: as it is); null if it can't be read. */
	private static ImageIcon icon(String path, String name, int size) {
		if (name == null) {
			return null;
		}
		Path file = Storage.HOME.resolve(path).resolve(name);
		if (!Files.isRegularFile(file)) {
			return null;
		}
		ImageIcon icon = new ImageIcon(file.toString());
		return size == 0 || icon.getIconWidth() == size ? icon
				: new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	private static int number(String text, int fallback) {
		try {
			return Integer.parseInt(text.trim());
		} catch (RuntimeException e) {
			return fallback;
		}
	}
}
