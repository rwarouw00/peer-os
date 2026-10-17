# System Object

`System` is the desktop's own object, defined in [`system/js/system.js`](../siteroot/system/js/system.js) and loaded only by the desktop page. Apps normally reach it indirectly through the [App Object](App-Object.md) (see [Architecture](Architecture.md)). This page is for people working on Skylight OS itself.

Because apps are served from the same site as the desktop, an app *can* call `parent.System` directly, and the Dialog's OK scripts rely on this. It isn't a supported API for apps, and it bypasses any checks a future release might add.

## Properties

| Property | Set by | Meaning |
| --- | --- | --- |
| `username` | login | Logged-in user |
| `background` | login | Background image path from the user's settings |
| `displayNotificationsFor` | login | Toast display time in milliseconds (default `'7000'`) |
| `selected` | right-click and taskbar click | The element last clicked, used by context menu scripts |

## Windows

| Function | What it does |
| --- | --- |
| `openWindow(path, args)` | Opens the app in folder `path` (relative to the site root, ending in `/`) and returns its [AppID](AppID.md). `args` is a query string added to the app's URL; when it is left out, the text `undefined` is added instead. See [Architecture](Architecture.md) for the steps. |
| `closeWindow(appID)` | Removes the window and its taskbar icon. |
| `hideWindow(appID)` / `showWindow(appID)` | Hide or show a window. Used by taskbar clicks. |
| `openDialog(msg, onOK)` | Opens `system/components/dialog/` and passes `msg` and `onOK` through `localStorage`. See [Dialogs](Dialogs.md). |

## Desktop

| Function | What it does |
| --- | --- |
| `addAppIcon(path)` | Adds an app's large icon to the home menu. Called once per entry in `apps.xml` at login. |
| `addShortcut(path)` | Adds a desktop shortcut and posts it to `shortcuts.php`. See [App.addShortcut](App.addShortcut.md). |
| `postNotification(header, body, action)` | Adds a notification to the tray and shows a toast. `action`, if a function, runs when the tray entry is clicked. **`body` must be given**: without it the tray entry appears but the toast fails with an error. |
| `toast.send(header, text)` | Shows a toast only. A `null` argument reuses the previous toast's text. Set `toast.autoHide = false` to keep toasts on screen. |
| `renderContextMenu(event, xmlString)` | Shows a menu built from context menu XML at the mouse position. See [Context Menus](Context-Menus.md). |
| `logoutUser()` | Removes the user from `LoggedInUsers.xml` and clears `Skylight.user`. See [Users and Login](Users-and-Login.md). |
| `handleAppEvent(event)` | Receives messages from apps and dispatches them. See [Architecture](Architecture.md). |

## Files and XML

| Function | What it does |
| --- | --- |
| `openFile(file)` | Synchronous GET; returns the text. Paths are relative to the site root, since the desktop page is there. |
| `saveFile(file, contents)` | Posts to `../../../system/server/saveFile.php` with the current user. From the desktop page, that address only reaches `system/server/` when Skylight OS is installed at the root of the website (browsers ignore the extra `../` there); in a subfolder it points to the wrong place. |
| `parseXml(xml)` / `serializeXml(dom)` | Same as the [App versions](App.parseXml.md), but errors open a dialog directly. |

## Referenced but missing

The context menu files call functions that don't exist in `system.js`, so those menu items throw errors: `removeAppFromList`, `deleteApp`, `getPinnedState`, `pinAppIcon`, `unpinAppIcon` and `closePanel`. `openPanel` and `closePanel` are present but commented out (see [Panels](Panels.md)).

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
