# Desktop internals

The `Desktop` class ([`Desktop.java`](../site16luxorhecl/src/skylight/Desktop.java)) runs the whole desktop. The fixed parts of its windows are `.xul` files in [`ui/`](../site16luxorhecl/ui/); the parts that change (app buttons, shortcuts, the window list, notifications) are made in code. Apps reach it through the [App API](app-api.md); this page is for people changing Skylight OS itself.

## Windows and components

```
JFrame "Skylight OS" (login)
└── ui/login.xul: #username (JTextField), #password (JPasswordField), #message; commands logIn, quit

JFrame "Skylight OS" (the desktop, maximized)
├── NORTH: ui/panel.xul: #apps (button: toggleMenu) | #windowList (gadget: a JToggleButton per window) | #clock
└── CENTER: desktop area (JPanel, null layout): paints the background; a JLabel per shortcut   (code)

JPopupMenu (home menu)
└── ui/menu.xul: #pages (gadget: CardLayout with the apps page and the notifications page, from code)
                  footer: showApps | showNotifications | clearNotifications | #logout

JWindow per toast, and a panel per notification in the menu: ui/notification.xul (#header, #body, close)
JDialog per app, owned by the desktop window: the app's app.xul
JDialog per message or question (JOptionPane, not modal), owned by the app's window   (code)
```

## State

| Field | Holds |
| --- | --- |
| `user`, `settings` | The logged-in user and their settings |
| `windows` | AppID → `AppWindow` (app, dialog, window-list button), in opening order |
| `manifests` | App path → parsed `app.properties` (cached; `null` for broken apps) |
| `toasts` | Toasts on screen |
| `notifications` | Header and body of each notification, newest first |

## Details worth knowing

* **Every `.xul` file is loaded when its window is made**, not at start-up: Luxor keeps one table of definitions, and loading a file just before building it makes sure its ids are the ones used. The toast and the menu's notification list load `notification.xul` once per notification, so each one's close command closes that one.
* **Commands are Java lambdas** for the desktop's own files (`command -> logIn()`) and Hecl procs for apps.
* **The home menu** is a `JPopupMenu`, which closes when the user clicks elsewhere.
* **Shortcuts** are `JLabel`s with the icon above the text. Dragging moves them with `setLocation`; the new position is saved on release only if the shortcut moved. Double-click opens the app; right-click offers **Remove from desktop**.
* **App windows** are `JDialog`s owned by the desktop window, which keeps them in front of it and out of the operating system's taskbar. Closing one calls the app's `setCanClose` function first.
* **Dialogs** are `JOptionPane`s turned into non-modal `JDialog`s. Their answer is read from the pane's `value` property, so OK runs the app's function exactly once, and Cancel or closing the dialog doesn't.
* **Hecl errors** in a command, callback or bound event become an "Error in <app>" notification with Hecl's message and where it happened, made from the error's stack of `{command line}` pairs.
* **Padding**, colours and tooltips are all in the `.xul` files now, thanks to the [improved Luxor](luxor-improved.md); site13 had to set them in Java.
* **Each window's Hecl interpreter** is created when the window opens, and its thread stopped with `invokeLater` after the window closes, so a proc that closes its own window finishes first. `bind` handlers run on Swing's event thread, where the events happen.

## Extending the desktop

**A new App method that needs the desktop:** add a package-private method to `Desktop` and a public one to `App` that calls it:

```java
// Desktop.java
void setWindowTitle(String appID, String title) {
	AppWindow window = windows.get(appID);
	window.dialog.setTitle(title);
	window.button.setText(title);
}

// App.java
public void setTitle(String title) {
	desktop.setWindowTitle(appID, title);
}
```

Scripts can call it straight away: `$app setTitle {My notes}`.

**A new setting:** add a field to `Settings` (and to `copy()`), read and write it in `Storage.loadSettings`/`saveSettings` with a validity check, and apply it in `Desktop.applySettings()`.

**Changing a window's layout:** edit its file in `ui/`. Keep the ids and commands that `Desktop` uses (listed in each file's comment); order, labels, spacers and styles can change without touching Java. Check the result with `tools/preview.sh`.
