# Desktop internals

The `Desktop` class ([`skylight/desktop.py`](../site08py27tk/skylight/desktop.py)) builds and runs the whole desktop. Apps reach it through the [App API](app-api.md); this page is for people changing Skylight OS itself.

## Sections

| Section | Methods |
| --- | --- |
| Starting | `__init__`, `start` |
| Login and logout | `showLogin`, `quit`, `logout` |
| Building the desktop | `showDesktop`, `tickClock` |
| Settings, background and shortcuts | `applySettings`, `saveSettings`, `drawBackground`, `renderShortcuts`, `makeShortcutInteractive`, `addShortcut`, `removeShortcut` |
| Home menu | `buildMenu`, `openFromMenu`, `toggleMenu`, `hideMenu`, `showMenuPage`, `onAnyClick` |
| Notifications | `notify`, `addToTray`, `notificationBox`, `clearNotifications`, `showToast`, `layoutToasts`, `removeToast` |
| Dialogs | `openDialog` |
| Apps and windows | `readManifest`, `loadAppClass`, `openWindow`, `toggleWindow`, `closeWindow`, `reportError` |

Helpers at the bottom of the file: `ScrollFrame` (a frame with a scroll bar), `describe` (an exception as one line), `setIcons`, `maximize`, `keepOnTop` and `centre`.

## The widget tree

```
Tk (hidden)
├── Toplevel "Skylight OS" (maximized)
│   ├── ttk.Frame panel: Apps button | separator | window list (ttk.Checkbuttons) | clock
│   ├── Canvas: background picture, and per shortcut an icon and two texts (shadow, label)
│   ├── Toplevel home menu (borderless, topmost, hidden until opened)
│   │   └── pages (apps grid | Clear + ScrollFrame of notifications), footer: Apps | Notifications | power
│   ├── Toplevel per toast (borderless, topmost)
│   └── Toplevel per app window (transient)
│       └── Toplevel per dialog (transient, with a grab)
└── Toplevel login (while logged out)
```

## State

| Attribute | Holds |
| --- | --- |
| `user`, `settings` | The logged-in user and their settings (a dictionary) |
| `windows` | AppID → `{'app', 'window', 'button'}` |
| `manifests` | App path → parsed `app.json` (cached; `None` for broken apps) |
| `appClasses` | App path → its loaded class |
| `toasts` | Toast windows on screen, top to bottom |

## Details worth knowing

* **The hidden root.** Tkinter needs one `Tk` window for the whole program. Skylight hides it and uses a `Toplevel` for everything else, so the login window and the desktop can come and go.
* **The background** is redrawn when the canvas changes size (`<Configure>`), but only if the size really changed, because scaling a picture takes a moment.
* **Shortcuts** are canvas items sharing a tag such as `shortcut0`, so they move together. Dragging moves the items by the mouse's movement and remembers the total; on release, the new position is saved. Saving redraws every shortcut, so it's scheduled with `after_idle` to run after the mouse event has finished.
* **The home menu** is a borderless `Toplevel`. It closes on any click elsewhere in Skylight through a `bind_all('<ButtonPress>')` handler, which leaves alone clicks inside the menu and on the Apps button (whose own command toggles the menu).
* **Toasts** are borderless, topmost `Toplevel`s, each removed by its own `after(notificationTime, ...)`.
* **Dialogs** are `Toplevel`s that are transient to the app's window and take a *grab*, so all input goes to them until they're answered. `wait_visibility()` comes first, because Tk can only grab a window that's on screen.
* **App windows** are transient to the desktop, which keeps them in front of it. The window's close button calls `canClose()`; `closeWindow` destroys the window without asking.
* **Errors** in any Tk callback reach `reportError` (installed as `report_callback_exception`), which prints the traceback and posts a notification.
* **Pictures** are cached by `images.photo`, which also keeps them referenced so Tk doesn't lose them.

## Extending the desktop

**A new App method that needs the desktop:** add a method to `Desktop` and one to `App` that calls it:

```python
# skylight/desktop.py
def setWindowTitle(self, appID, title):
    entry = self.windows[appID]
    entry['window'].title(title)
    entry['button'].configure(text=title)

# skylight/app.py
def setTitle(self, title):
    self.desktop.setWindowTitle(self.appID, title)
```

**A new setting:** add its default to `DEFAULT_SETTINGS` and a check to `clean_setting` in `storage.py`, then apply it in `Desktop.applySettings()`.
