# Desktop internals

The `Desktop` class ([`lib/Desktop.php`](../site4phpgtk/lib/Desktop.php)) builds and runs the whole desktop. Apps reach it through the [App API](app-api.md); this page is for people changing Skylight OS itself.

## Sections

| Section | Methods |
| --- | --- |
| Start-up, login and logout | `start`, `showLogin`, `logout` |
| Building the desktop | `showDesktop`, `buildPanel`, `image`, `tickClock` |
| Settings, background and shortcuts | `applySettings`, `saveSettings`, `fitBackground`, `renderShortcuts`, `addShortcut`, `removeShortcut`, `onShortcutPress`/`Motion`/`Release` |
| Home menu | `buildMenu`, `toggleMenu`, `hideMenu`, `showMenuPage`, `onMenuAppClicked` |
| Notifications | `notify`, `addToTray`, `clearNotifications`, `showToast`, `layoutToasts`, `removeToast`, `closeButton` |
| Dialogs | `openDialog`, `onDialogResponse` |
| Apps and windows | `readManifest`, `openWindow`, `closeWindow`, `onWindowDelete`, `toggleWindow`, `onTaskbarPress` |

## The widget tree

```
GtkWindow "Skylight OS" (maximized)
└── GtkVBox
    ├── GtkEventBox "skylight-panel"
    │   └── GtkHBox: Apps button | separator | window list (GtkHBox) | clock (GtkLabel)
    └── GtkLayout (the desktop area)
        ├── GtkImage: background, scaled to cover the area
        └── GtkEventBox "skylight-shortcut" per shortcut: GtkVBox of GtkImage + GtkLabel

GtkWindow (home menu, undecorated, kept in front of the desktop)
└── GtkFrame
    └── GtkVBox: apps page (GtkIconView) | notifications page | footer buttons

GtkWindow POPUP "skylight-toast" per toast, stacked at the top right
GtkWindow per app, kept in front of the desktop
```

The desktop area is a `GtkLayout` rather than a `GtkFixed`, because a layout doesn't ask for the size of its children: the window can shrink below the background image's size.

## State

| Property | Holds |
| --- | --- |
| `user`, `settings` | The logged-in user and their settings (public; apps read them through `App`) |
| `windows` | AppID → `array('app', 'window', 'button', 'visible')` |
| `toasts` | Toast windows on screen, top to bottom |
| `drag` | The shortcut being dragged, with the pointer offset |
| `menuOpen`, `menuClosedAt` | Home menu state; see below |

## Details worth knowing

* **The background** is rescaled in `fitBackground()` whenever the desktop area changes size, and skipped when the size and file are unchanged, so it doesn't loop.
* **The home menu** is a separate undecorated window. It closes when it loses focus. Clicking **Apps** while it's open takes focus first (closing it), then delivers the click; `menuClosedAt` makes that click close the menu rather than reopen it. The menu is positioned *after* it is shown, because window managers may place new windows themselves.
* **Shortcuts** are invisible event boxes (`set_visible_window(false)`) so the background shows through. A double-click opens the app, a drag moves it (and saves on release), and a right-click offers **Remove from desktop**.
* **App windows** are created with `set_transient_for($desktop)`, which keeps them in front of the desktop window, and `set_skip_taskbar_hint(true)`, so only Skylight's own window list shows them. `onWindowDelete` asks the app's `canClose()` and always returns `true`, so GTK never destroys a window behind the desktop's back.
* **Login** uses `GtkDialog::run()`, which runs its own loop. Quitting before `Gtk::main()` has started simply returns; quitting after a logout calls `Gtk::main_quit()`.

## Extending the desktop

**A new App method** that needs the desktop: add the method to `Desktop`, and a one-line method to `App` that calls it:

```php
// lib/Desktop.php
function setWindowTitle($appID, $title)
{
	$this->windows[$appID]['window']->set_title($title);
}

// lib/App.php
function setTitle($title)
{
	$this->desktop->setWindowTitle($this->appID, $title);
}
```

**A new setting:** add a default in `Storage::defaultSettings()` and a rule in `Storage::cleanSetting()` (anything without a rule is refused), then apply it in `Desktop::applySettings()`.

**New colours:** name the widget with `set_name()` and add a style to `skylight.gtkrc` (see the [primer](gtk-primer.md#styles)).
