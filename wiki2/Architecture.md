# Architecture

Skylight OS is a desktop environment that runs in the browser. Everything the user sees is one page, [`index.html`](../siteroot/index.html), and each app runs inside an `<iframe>` window on that page.

## The pieces

| Piece | File | Runs in | Job |
| --- | --- | --- | --- |
| Desktop page | [`index.html`](../siteroot/index.html) | top-level page | Login screen, desktop, event handling for windows, taskbar and menus |
| `System` object | [`system/js/system.js`](../siteroot/system/js/system.js) | desktop page | Creates windows, shows notifications and dialogs, reads files. See [System Object](System-Object.md) |
| `App` object | [`system/js/app.js`](../siteroot/system/js/app.js) | each app's iframe | The API apps call. See [App Object](App-Object.md) |
| Desktop styles | [`system/css/system.css`](../siteroot/system/css/system.css) | desktop page | Windows, taskbar, home menu, toasts, context menus |
| UI styles | [`system/css/ui.css`](../siteroot/system/css/ui.css) | desktop and apps | Buttons, text boxes and button bars. See [UI elements](UI-elements.md) |
| Server scripts | [`system/server/*.php`](../siteroot/system/server/) | web server | Login, saving files, shortcuts. See [Server Endpoints](Server-Endpoints.md) |

## How an app talks to the desktop

An app cannot touch the desktop directly, because it lives in a separate iframe. Instead, most `App` functions build a small JSON message and send it to the desktop with `parent.postMessage(...)`. The desktop listens for these messages in `System.handleAppEvent` and calls the matching `System` function.

```
App.postNotification('Hi', 'There')                       (in the app's iframe)
  -> parent.postMessage('{"eventType":"postNotification","header":"Hi","body":"There"}', '*')
  -> System.handleAppEvent(event)                         (in the desktop page)
  -> System.postNotification('Hi', 'There', undefined)
```

The messages the desktop understands:

| `eventType` | Other fields | Sent by | Desktop action |
| --- | --- | --- | --- |
| `openDialog` | `message`, `onOK` | [App.openDialog](App.openDialog.md) | `System.openDialog(message, onOK)` |
| `openWindow` | `path` | [App.openWindow](App.openWindow.md) | `System.openWindow(path)` |
| `closeWindow` | `title` (actually an [AppID](AppID.md)) | [App.closeWindow](App.closeWindow.md) | `System.closeWindow(title)` |
| `postNotification` | `header`, `body`, `action` | [App.postNotification](App.postNotification.md) | `System.postNotification(header, body, action)` |
| `setBackground` | `path` | [App.setBackground](App.setBackground.md) | Sets the desktop's background image |
| `addShortcut` | `path` | [App.addShortcut](App.addShortcut.md) | `System.addShortcut(path)` |

Because the messages are JSON, only plain data gets through: a JavaScript function passed as an argument is silently dropped. Messages are fire-and-forget, so an `App` function that goes through the desktop never returns a result.

Some `App` functions don't involve the desktop at all and run entirely inside the app: [App.openFile](App.openFile.md), [App.saveFile](App.saveFile.md), [App.parseXml](App.parseXml.md), [App.serializeXml](App.serializeXml.md), [App.getQueryStringArgs](App.getQueryStringArgs.md), [App.getAppID](App.getAppID.md), [App.getUsername](App.getUsername.md) and [App.gotoLocation](App.gotoLocation.md).

## What happens when a window opens

`System.openWindow(path)`:

1. Downloads `path + 'appData.json'` (see [appData.json](appData.json.md)).
2. Picks a new [AppID](AppID.md) with `Math.random()`.
3. Builds the window: a title bar with the small icon, the title, and maximize and close buttons, plus an iframe for the content.
4. Points the iframe at `path?app-id=<AppID>&username=<user>&<args>`, which is how the app learns its AppID and the current user.
5. Sizes the window from `width` and `height`, centers it if `position` is `"centered"` (otherwise places it at 100px, 100px), and adds the medium icon to the taskbar.

Paths given to `openWindow` are relative to the site root, because they are resolved by the desktop page.

## Directory layout

```
siteroot/
├── index.html                  desktop page (login + desktop)
├── apps/
│   ├── default/                apps that ship with the system
│   │   ├── terminal/
│   │   ├── settings/
│   │   └── web browser/
│   └── panels/hello world/     unfinished panel example (see Panels)
└── system/
    ├── apps.xml                apps listed in the home menu
    ├── LoggedInUsers.xml       login tokens of users currently logged in
    ├── settings/               users.xml (accounts) and one <username>.xml per user
    ├── components/             system windows: dialog, save dialog, context menus, login prototype
    ├── css/                    system.css, ui.css
    ├── js/                     system.js, app.js (+ two unused search scripts)
    ├── images/                 desktop backgrounds, window buttons, file-type icons
    └── server/                 PHP: auth.php, saveFile.php, shortcuts.php, functions.php,
                                MIME tables, shortcuts.xml
```

## Login and saving

`auth.php` checks the password against `system/settings/users.xml`, then sends the browser a `Skylight_OS_Auth` cookie and records the same token in `system/LoggedInUsers.xml`. Each save sends the cookie back, and `saveFile.php` compares it with the stored token. See [Users and Login](Users-and-Login.md) and [Server Endpoints](Server-Endpoints.md).

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._

_Updated after the `system/server` scripts were added to `siteroot`._
