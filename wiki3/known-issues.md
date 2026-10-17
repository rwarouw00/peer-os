# Known issues

Bugs and unfinished features in Skylight OS Arctic Eagle v1.00.00, with the place to fix each one. Security problems are listed separately in [Security](security.md). *Tested* means the problem was reproduced in a browser.

## Desktop

| Issue | Where | Suggested fix |
| --- | --- | --- |
| Toasts are mostly off screen at every screen size; about 115 of 300 pixels show (*tested*) | `.toastAnimState0`, `.toastAnimState1` in `system/css/system.css` (`margin-left: 77%`) | Remove the margin and position `#toastContainer` with `right` and a fixed width |
| A toast's × does nothing | `System.toast.send` | Add a click handler that removes the toast |
| A notification without a body shows no toast and throws an error (*tested*) | `System.toast.send` reads `text.length` | Default `header` and `text` to `''` |
| Logout leaves the desktop on screen, and throws if the user isn't in `LoggedInUsers.xml` (*tested*) | `System.logoutUser` | Check the element exists; reload the page after saving |
| A user without a settings file gets a broken desktop (*tested*) | `init` in `index.html` | Fall back to default settings |
| Enter doesn't submit the login form (*tested*) | `index.html` | Wrap the fields in a `<form>` and handle `submit` |
| Login fields aren't URL-encoded; a non-JSON reply leaves the screen stuck | Login handler in `index.html` | `encodeURIComponent` each value; wrap `JSON.parse` in `try` |
| Clock shows `0:15 am` after midnight | Clock in `index.html` | Treat hour 0 as 12 |
| Clicking a window doesn't bring it to the front; windows can't be resized | `handleClickEvent`, `system.css` | Raise `z-index` on `mousedown`; add a resize handle |
| Pin/Unpin, Remove and Delete App menu items throw errors | `system/components/context menus/` | Implement the missing `System` functions or remove the items |
| Any non-JSON `postMessage` to the desktop throws (*tested*) | `System.handleAppEvent` | Wrap `JSON.parse` in `try` (and check origin, see Security) |

## Windows and apps

| Issue | Where | Suggested fix |
| --- | --- | --- |
| Every app URL ends in `&undefined` (*tested*) | `System.openWindow` appends `args` unconditionally | Only append `args` when it is given |
| Invalid `appData.json` leaves an empty taskbar slot | `System.openWindow` adds elements before parsing | Parse the manifest first |
| `App.gotoLocation` without a second argument adds `&undefined`; the query string grows with each call | `system/js/app.js` | Default to `''`; merge values instead of appending |
| Notification `action` can't be used from apps | JSON drops functions | Remove the parameter from `App`, or support named actions |
| `App.saveFile` doesn't encode its values | `system/js/app.js` | Encode `file`, `contents` and `user` inside the function |
| Save address is hard-coded as `../../../`, so apps must be three folders deep and the desktop must be at the site root | `saveFile` in `app.js` and `system.js` | Use an absolute path or one computed from a single base URL |

## Dialog

| Issue | Where | Suggested fix |
| --- | --- | --- |
| The OK script is never cleared: opening Dialog from the home menu and clicking OK reruns the last dialog's script | `system/components/dialog/index.html` resets `'SKylight.dialog.onOK'` (capital K) | Fix the key name |
| Two dialogs opened together share one message slot | `localStorage` hand-off in `System.openDialog` | Pass the message in the dialog's query string, or key it by AppID |
| Button labels can't be changed | `System.openDialog` | Add a labels parameter (later releases did) |

## Shortcuts

| Issue | Where | Suggested fix |
| --- | --- | --- |
| Clicking or dropping a shortcut posts `[object Object]` to `shortcuts.php`, with a stale `path` | `mouseup` case of `handleDragEvent` in `index.html` | Read the path from the shortcut's hidden `.shortcutPath` element and `JSON.stringify` the object |
| Shortcuts aren't restored after a reload | Nothing reads them back | Load them from the server at login |
| All shortcuts start at the same position; two apps with the same title clash | `System.addShortcut` | Offset each new shortcut; build ids from the path, not the title |

## Built-in apps

| Issue | Where | Suggested fix |
| --- | --- | --- |
| Terminal: Save Script does nothing | Button id is `terminalSaveButtom`, code looks for `terminalSaveButton` | Fix the id and write the handler |
| Settings: notification time box has no effect | Placeholder `alert('test')` in `js/custom.js` | Save `displayNotificationsFor` to the settings file |
| Settings: Add User's Save does nothing; switching pages loses the AppID | `AddUser.html` uses `location.assign` | Use `App.gotoLocation`; add a server script for creating users |
| Settings: the fourth background is missing on Linux servers (*tested*) | File is `desktop_4.JPG`, referenced as `.jpg` | Rename the file |
| Web Browser: back/forward fail for other sites; address bar doesn't follow links | Browser security rules | Keep a history list in the app instead |
| Save File: Save and Cancel do nothing; no way for apps to use it | `system/components/save dialog/` | Finish the dialog and add an `App` function for it |
| Dialog and Save File appear in the home menu | `system/apps.xml` | Remove the two entries |

## Unused code

These files aren't referenced anywhere and can be finished or removed:

* `system/js/jse_form.js`, `system/js/jse_search.js`: a third-party client-side search script ("JSE").
* `system/components/login/login.wcf`: a prototype login window in a different format, calling functions that don't exist.
* `apps/default/terminal/js/app.js`, `system/components/dialog/js/app.js`: outdated copies of the App object.
* `system/images/home2.png`, `home2.svg`, the `min_button` images and the `.svg` window buttons.
* The panels feature: commented-out `openPanel`/`closePanel`, `.panel` styles, and `apps/panels/hello world/`.
