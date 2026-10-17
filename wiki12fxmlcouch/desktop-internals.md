# Desktop internals

The `Desktop` class ([`Desktop.java`](../site12fxmlcouch/src/skylight/Desktop.java)) runs the whole desktop. The fixed parts of its windows are FXML files in [`ui/`](../site12fxmlcouch/ui/), with `Desktop` as their controller; the parts that change (app icons, shortcuts, the window list, toasts) are added in code. Apps reach it through the [App API](app-api.md); this page is for people changing Skylight OS itself.

## Sections

| Section | Methods |
| --- | --- |
| Login and logout | `showLogin`, `logIn`, `quit`, `loadUi`, `logout` |
| Building the desktop | `showDesktop`, `tickClock`, `scene`, `image`, `icon` |
| Settings, background and shortcuts | `applySettings`, `saveSettings`, `renderShortcuts`, `makeShortcutInteractive`, `addShortcut`, `removeShortcut` |
| Home menu | `buildMenu`, `toggleMenu`, `showMenuPage`, `showAppsPage`, `showNotificationsPage`, `clearNotifications` |
| Notifications | `notify`, `notificationBox`, `showToast`, `layoutToasts`, `removeToast` |
| Dialogs | `openDialog` |
| Apps and windows | `readManifest`, `openWindow`, `loadAppClass`, `closeWindow` |

## The node tree

```
Stage "Skylight OS" (maximized)
└── BorderPane                                       ui/desktop.fxml
    ├── top: ToolBar #panel — Apps button | Separator | #windowList (HBox of ToggleButtons) | spacer | #clock
    └── centre: Pane #area .desktop-area (CSS background image)
        └── Label .shortcut per shortcut (icon above text), positioned with relocate()   (code)

Popup (home menu, auto-hide)
└── VBox .home-menu                                  ui/menu.fxml
    ├── StackPane: #appsPage (TilePane; app buttons added in code) | #notificationsPage (Clear + ScrollPane of #notificationList)
    └── HBox .home-menu-footer: Apps | Notifications | power button

HBox per notification, in the menu and as a toast  ui/notification.fxml
Stage per app, owned by the desktop; contents from <store>/<name>/app.fxml
Alert per dialog, owned by the app's window       (code)
```

The handler methods the `ui/` files call are `logIn`, `quit` (login), `toggleMenu` (desktop), and `showAppsPage`, `showNotificationsPage`, `clearNotifications`, `logout` (menu).

## State

| Field | Holds |
| --- | --- |
| `user`, `settings` | The logged-in user and their settings |
| `windows` | AppID → `AppWindow` (app, stage, window-list button), in opening order |
| `manifests` | App path → parsed `app.properties` (cached; `null` for broken apps, or if the store couldn't be reached) |
| `toasts` | Toast popups on screen, top to bottom |

## Details worth knowing

* **`Platform.setImplicitExit(false)`** in `Main` stops JavaFX quitting when logout closes the last window; Quit calls `Platform.exit()`.
* **The home menu** is a `Popup` with auto-hide. A click outside it closes it and is consumed, so clicking **Apps** while the menu is open just closes it.
* **Shortcuts** are `Label`s with an icon graphic. Dragging uses `sceneToLocal` to follow the mouse; positions are saved on release only if the shortcut moved. Double-click opens the app; right-click offers **Remove from desktop**.
* **App windows** are `Stage`s with `initOwner(desktop)`, which keeps them in front of the desktop and out of the operating system's taskbar. `openWindow` calls `toFront()` because some window managers open new windows behind the active one. The close request is consumed and passed to the app's `canClose()`.
* **Dialogs** run their `onOK` at most once, because JavaFX can report a dialog as hidden more than once.
* **FXML apps** (`fxml=`) are opened as an `FxmlApp`, which loads their file with `app` in the FXML namespace. **Java apps** (`class=`) come from `skylight.jar`, or from the app's `app.jar` (in the store, or in `apps/<name>/`) through a `URLClassLoader` whose parent is Skylight's own class loader. The class loader reads the jar over HTTP, like any other URL.
* **The `ui/` files** are loaded by `loadUi(name)`, which sets the `Desktop` as controller. `@FXML` fields receive the elements with matching `fx:id`s, and `onAction="#name"` calls the `@FXML` method of that name. Each file fills only its own fields: `login.fxml` the login fields, `desktop.fxml` the panel, `menu.fxml` the menu pages.
* **Notifications** load `ui/notification.fxml` without a controller and find its parts with `getNamespace().get("header")` and so on.
* **Where app files come from** is decided in one place, `Storage.appResource(path, name)`: the URL `<appStore><name>/<file>` when `skylight.properties` names an app store, otherwise a `file:` URL in `apps/`. The manifest, the FXML (and so its `fx:script source=` and `@` paths), the icons and `app.jar` all go through it, which is why nothing else in `Desktop` knows about CouchDB. The home menu's list comes from `Storage.appList()`: `<appStore>_design/store/apps.txt` or `apps/apps.txt`.
* **Logging in** closes the login window with `Platform.runLater`, after the key or click event has finished: closing a window during its own key event can crash JavaFX 8 on Linux.

## Extending the desktop

**A new App method that needs the desktop:** add a package-private method to `Desktop` and a public one to `App` that calls it:

```java
// Desktop.java
void setWindowTitle(String appID, String title) {
	AppWindow window = windows.get(appID);
	window.stage.setTitle(title);
	window.button.setText(title);
}

// App.java
public void setTitle(String title) {
	desktop.setWindowTitle(appID, title);
}
```

**A new setting:** add a field to `Settings` (and to `copy()`), read and write it in `Storage.loadSettings`/`saveSettings` with a validity check, and apply it in `Desktop.applySettings()`.

**New styles:** add `styleClass="..."` in an FXML file (or a style class in code) and a rule in `ui/skylight.css`.

**Changing a window's layout:** edit its file in `ui/`. Keep the `fx:id`s and `#handler` names that `Desktop` uses (they're listed in each file's elements); everything else, such as order, spacing, extra labels or images, can change without touching Java.
