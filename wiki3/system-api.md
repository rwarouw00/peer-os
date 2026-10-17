# System API

`System` is the desktop's own object, defined in [`system/js/system.js`](../siteroot/system/js/system.js) and loaded only by the desktop page. It does the real work behind the [App API](app-api.md). This page is for people changing Skylight OS itself; apps should use `App`.

## Properties

| Property | Description |
| --- | --- |
| `username` | Logged-in user. Set at login. |
| `background` | Background image path from the user's settings. |
| `displayNotificationsFor` | Toast display time in milliseconds. Default `'7000'`. |
| `selected` | Element last clicked or right-clicked; used by context menu scripts. |
| `toast.autoHide` | `true` (default) to fade toasts out after `displayNotificationsFor`. |

## Functions

| Function | Description |
| --- | --- |
| `openWindow(path, args)` | Opens the app in `path` and returns its AppID. `args` is appended to the app's query string. See [Architecture](architecture.md#opening-a-window). |
| `closeWindow(appID)` | Removes a window and its taskbar icon. |
| `hideWindow(appID)`, `showWindow(appID)` | Hide or show a window (taskbar clicks). |
| `openDialog(message, onOK)` | Opens the Dialog window, passing `message` and `onOK` through `localStorage`. |
| `postNotification(header, body, action)` | Adds a notification to the tray and shows a toast. `action`, a function, runs when the tray entry is clicked. `body` is required. |
| `toast.send(header, text)` | Shows a toast without adding it to the tray. `null` reuses the previous text. |
| `addAppIcon(path)` | Adds an app's icon to the home menu. |
| `addShortcut(path)` | Adds a desktop shortcut and posts it to `shortcuts.php`. |
| `renderContextMenu(event, xmlString)` | Shows a menu defined in [context menu XML](configuration.md#context-menus) at the mouse position. |
| `logoutUser()` | Removes the user's first token from `LoggedInUsers.xml`, saves the file, and clears `Skylight.user`. Doesn't change the screen or delete the cookie. |
| `openFile(path)` | Synchronous GET, path relative to the site root. |
| `saveFile(path, contents)` | Posts to `saveFile.php`, using a `../../../` address that only works when Skylight OS is at the root of the website. |
| `parseXml(xml)`, `serializeXml(doc)` | XML helpers, as in `App`. |
| `handleAppEvent(event)` | The `message` listener that turns app messages into calls. See [Architecture](architecture.md#how-apps-talk-to-the-desktop). |

## Desktop event handling

Mouse handling for the desktop lives in [`index.html`](../siteroot/index.html), inside the `init` function that runs after login:

| Handler | Handles |
| --- | --- |
| `handleDragEvent` | `mousedown`/`mousemove`/`mouseup`: dragging any element with the `draggable` class when the press starts on its child or grandchild (in practice, window title bars and shortcuts); saving shortcut positions on `mouseup` |
| `handleClickEvent` | Home button and menu tabs, closing the menu, taskbar clicks, maximize and close buttons, the logout button, notification removal |
| `handleContextmenuEvent` | Right-clicks on taskbar icons, home-menu icons and panel title bars |

## Unfinished parts

Context menus call `removeAppFromList`, `deleteApp`, `getPinnedState`, `pinAppIcon`, `unpinAppIcon` and `closePanel`, none of which exist. `openPanel` and `closePanel` are commented out: a panel feature (small windows at the bottom of the screen, with a sample in `apps/panels/hello world/`) was started but not finished. See [Known issues](known-issues.md).
