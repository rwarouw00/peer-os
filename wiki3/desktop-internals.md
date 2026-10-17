# Desktop internals

The desktop is the `Desktop` object in [`system/js/desktop.js`](../site3/public/system/js/desktop.js). It runs only on the desktop page; apps use the [App API](app-api.md) instead. This page is for people changing Skylight OS itself.

The file is one ES5 module pattern, `var Desktop = (function () { ... }());`, with private DOM helpers at the top and the object grouped into sections:

| Section | Functions |
| --- | --- |
| Start-up and login | `start`, `show`, `logout`, `tickClock` |
| Settings | `applySettings`, `saveSettings` |
| Apps and windows | `readManifest`, `buildAppMenu`, `openWindow`, `closeWindow`, `focusWindow`, `windowOfFrame` |
| Dialogs | `openDialog`, `answerDialog` |
| Notifications | `notify` |
| Shortcuts | `renderShortcuts`, `addShortcut`, `saveShortcutPositions` |
| Menus | `showMenu`, `showMenuPage`, `closeContextMenu` |
| Event handling | `actions`, `onClick`, `onDoubleClick`, `onContextMenu`, `onMouseDown`, `messages`, `onMessage` |

## State

| Property | Holds |
| --- | --- |
| `user` | Logged-in username |
| `settings` | The user's settings, as returned by `api/settings.php` |
| `windows` | AppID → `{ element, frame, button }` for each open window |
| `dialogs` | Dialog AppID → `{ callback }` or `{ source, dialogId }` for each open dialog |
| `manifests` | App path → parsed `app.json` (cached) |
| `topLayer` | Highest `z-index` used so far |

## Useful functions

| Function | What it does |
| --- | --- |
| `openWindow(path, args)` | Opens the app in `path`; `args` is an object of extra query values. Returns the AppID, or `null` (with a notification) if `path` isn't an app. |
| `closeWindow(appID)` | Removes the window and its taskbar button. |
| `notify(header, body)` | Adds a notification to the tray and shows a toast. Text only. |
| `openDialog(message, callback)` | Shows the Dialog; `callback` runs if the user clicks OK. |
| `saveSettings(changes)` | Sends `changes` to `api/settings.php`; on success applies the new settings, otherwise notifies the user. |

## How events are handled

There are no per-element event handlers. Four listeners on `document` do all the work:

* **Clicks** go to `onClick`, which looks up the clicked element's `data-action` attribute in the `actions` table. A button that maximizes a window is simply `<span data-action="toggleMaximize">`.
* **Mouse-down** goes to `onMouseDown`, which brings the window under the mouse to the front and starts a drag if the element (or a parent) has `data-drag="window"` or `data-drag="shortcut"`. While dragging, the `dragging` class on `<body>` stops iframes swallowing mouse movement.
* **Right-click** goes to `onContextMenu`, which shows a Close menu for taskbar buttons.
* **Double-click** goes to `onDoubleClick`, which opens shortcuts.

Messages from apps go to `onMessage`, which checks the origin and sender (see [Architecture](architecture.md#messages-between-apps-and-the-desktop)) and looks the `eventType` up in the `messages` table.

Elements that belong to a window carry `data-app-id`; `appIdOf(element)` walks up to find it. The home menu carries `data-menu`, so clicks inside it don't close it.

## Extending the desktop

**A new button or clickable element:** give it `data-action="myAction"` and add `myAction: function (target, event) { ... }` to `actions`.

**A new App function that needs the desktop:**

1. Add a handler to `messages` in `desktop.js`:

   ```javascript
   setTitle: function (message, sender) {
       var win = Desktop.windows[sender];
       win.element.getElementsByTagName('span')[0].textContent = String(message.title);
   }
   ```

   `sender` is the AppID of the window that sent the message. Treat every field as untrusted text.

2. Add the function to `app.js`:

   ```javascript
   setTitle: function (title) {
       send('setTitle', { title: title });
   },
   ```

**A new setting:** add a default to `default_settings()` and a rule to `clean_setting()` in `lib/skylight.php`; the server rejects any setting it doesn't know. Apply it in `Desktop.applySettings`.

## Styles

[`desktop.css`](../site3/public/system/css/desktop.css) uses plain class names grouped by part (`.window-title`, `.window-close`, `.menu-footer`, `.toast`). State is shown with extra classes rather than inline styles: `.hidden`, `.window.maximized`, `.toast.visible`, `body.dragging`. Class names are added and removed with the `setClass` helper, since `classList` isn't available in older browsers.
