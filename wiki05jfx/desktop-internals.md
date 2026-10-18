# Desktop internals

The `Desktop` class ([`Desktop.java`](../site05jfx/src/skylight/Desktop.java)) builds and runs the whole desktop. Apps reach it through the [App API](app-api.md); this page is for people changing Skylight OS itself.

## Sections

| Section | Methods |
| --- | --- |
| Login and logout | `showLogin`, `logout` |
| Building the desktop | `showDesktop`, `tickClock`, `scene`, `image`, `icon` |
| Settings, background and shortcuts | `applySettings`, `saveSettings`, `renderShortcuts`, `makeShortcutInteractive`, `addShortcut`, `removeShortcut` |
| Home menu | `buildMenu`, `toggleMenu`, `showMenuPage` |
| Notifications | `notify`, `notificationBox`, `showToast`, `layoutToasts`, `removeToast` |
| Dialogs | `openDialog` |
| Apps and windows | `readManifest`, `openWindow`, `loadAppClass`, `closeWindow` |

## The node tree

```
Stage "Skylight OS" (maximized)
└── BorderPane
    ├── top: ToolBar — Apps button | Separator | window list (HBox of ToggleButtons) | spacer | clock Label
    └── centre: Pane .desktop-area (CSS background image)
        └── Label .shortcut per shortcut (icon above text), positioned with relocate()

Popup (home menu, auto-hide)
└── VBox .home-menu
    ├── StackPane: apps page (TilePane of Buttons) | notifications page (Clear + ScrollPane of rows)
    └── HBox .home-menu-footer: Apps | Notifications | power button

Popup per toast: HBox .toast, stacked below the panel on the right
Stage per app, owned by the desktop
Alert per dialog, owned by the app's window
```

## State

| Field | Holds |
| --- | --- |
| `user`, `settings` | The logged-in user and their settings |
| `windows` | AppID → `AppWindow` (app, stage, window-list button), in opening order |
| `manifests` | App path → parsed `app.properties` (cached; `null` for broken apps) |
| `toasts` | Toast popups on screen, top to bottom |

## Details worth knowing

* **`Platform.setImplicitExit(false)`** in `Main` stops JavaFX quitting when logout closes the last window; Quit calls `Platform.exit()`.
* **The home menu** is a `Popup` with auto-hide. A click outside it closes it and is consumed, so clicking **Apps** while the menu is open just closes it.
* **Shortcuts** are `Label`s with an icon graphic. Dragging uses `sceneToLocal` to follow the mouse; positions are saved on release only if the shortcut moved. Double-click opens the app; right-click offers **Remove from desktop**.
* **App windows** are `Stage`s with `initOwner(desktop)`, which keeps them in front of the desktop and out of the operating system's taskbar. `openWindow` calls `toFront()` because some window managers open new windows behind the active one. The close request is consumed and passed to the app's `canClose()`.
* **Dialogs** run their `onOK` at most once, because JavaFX can report a dialog as hidden more than once.
* **App classes** come from `skylight.jar`, or from `apps/<name>/app.jar` through a `URLClassLoader` whose parent is Skylight's own class loader, so external apps can use `skylight.App`.

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

**New styles:** add a style class in code and a rule in `skylight.css`.
