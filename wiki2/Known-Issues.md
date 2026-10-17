# Known Issues

Problems found while reviewing the `siteroot` code (Arctic Eagle v1.00.00). Items marked **tested** were confirmed by running the code in a browser; the rest come from reading it. File references are relative to `siteroot/`.

## Security

Fix these before putting Skylight OS on a server other people can reach.

1. **Any website opened in the Web Browser app can run code on the desktop (tested).** The desktop accepts messages from any window, without checking where they came from (`System.handleAppEvent`, `system/js/system.js`), and inserts notification and dialog text as HTML. A page from another site only has to call `top.postMessage(...)` with a notification whose body contains an `<img onerror=...>` tag, and its script runs with full access to the desktop and the logged-in user. **Fix:** in `handleAppEvent`, ignore messages whose `event.origin` isn't the site's own origin, and insert notification and dialog text with `textContent` instead of `innerHTML`.
2. **Apps have full access to the desktop.** Apps are pages on the same site as the desktop, so any app can call `parent.System` directly or change the desktop page. The [permissions](Permissions.md) described on the old wiki can't be enforced in the browser for that reason; real limits have to be enforced by the server scripts.
3. **The user list is public.** `system/settings/users.xml` (plain-text passwords) and every user's settings file are ordinary files the web server hands to anyone who asks. Keep password data outside the published folder, and store password hashes rather than passwords.
4. **The server can't tell who is calling.** After login, the browser keeps the username in `localStorage` and passes it around in URLs; nothing proves the user logged in. Unless `auth.php` starts a server session that `saveFile.php` checks, anyone can call `saveFile.php` directly with any `user` and `file`. The scripts aren't in `siteroot`, so this couldn't be checked; see [Server Endpoints](Server-Endpoints.md).
5. **The Save File dialog lists any folder.** `system/components/save dialog/index.php` lists whatever folder the `path` query value names, including folders outside the website.
6. **Code is run from text in several places**: the Dialog's OK script (`eval`), context menu items (`onclick` attributes) and the Terminal. That is by design for the Terminal, but combined with issue 1 it lets outside pages run code.

## Desktop and windows

7. **Toasts are mostly off screen (tested).** The `.toastAnimState0/1` styles in `system/css/system.css` push each 300px toast past the right edge at every screen size; only about 115px is visible. The toast's × button has no click handler either.
8. **A notification without a body breaks the toast (tested).** `System.toast.send` reads `text.length` on `undefined`; the tray entry is added but no toast appears.
9. **Notification click actions can't be used by apps.** Functions can't be sent through `postMessage` as JSON, so the `action` argument is lost.
10. **Every app URL ends in `&undefined` (tested).** `System.openWindow` appends its `args` parameter even when it is missing, and apps never pass it. [App.getQueryStringArgs](App.getQueryStringArgs.md) then reports an `"undefined"` key.
11. **The Dialog's OK script is never cleared.** `system/components/dialog/index.html` resets `'SKylight.dialog.onOK'` (capital K) instead of `'Skylight.dialog.onOK'`. Opening the Dialog from the home menu and clicking OK reruns the last dialog's script.
12. **Dialogs share one slot.** The message and script are passed through `localStorage`, so two dialogs opened together can swap or lose their text.
13. **Logging out leaves the desktop on screen**, and throws an error if the user isn't in `LoggedInUsers.xml` (tested). See [Users and Login](Users-and-Login.md).
14. **A user without a settings file gets a broken desktop (tested):** an XML error dialog, no background and an empty home menu.
15. **Login:** Enter doesn't submit (tested); the username and password aren't URL-encoded, so a password containing `&` or `+` is sent wrongly; a reply that isn't JSON leaves the screen stuck on the progress icon.
16. **Clicking or dropping a desktop shortcut posts `[object Object]` to `shortcuts.php`.** The `mouseup` handler in `index.html` passes an object to `encodeURIComponent` and uses a `path` variable left over from building the home menu. Shortcuts also aren't reloaded at login, all start at the same spot, and apps with the same title clash.
17. **Unimplemented menu items:** Pin/Unpin, Remove and Delete App call `System` functions that don't exist. See [Context Menus](Context-Menus.md).
18. **Clicking a window doesn't bring it to the front**, and windows can't be resized.
19. **The clock shows `0:15 am` between midnight and 1 am.**
20. **An invalid `appData.json` leaves an empty taskbar slot**, because the taskbar element is added before the file is parsed.
21. **Any non-JSON message to the desktop throws an error** in `handleAppEvent` (tested). Harmless, but noisy, and browser extensions send such messages.

## Files and paths

22. **[App.saveFile](App.saveFile.md) doesn't encode values**, so contents with `&`, `+` or `%` are cut off or changed.
23. **Save paths are hard-coded as `../../../system/server/saveFile.php`.** Apps must be exactly three folders deep, and `System.saveFile` (used by logout) only works when Skylight OS is installed at the root of the website.
24. **`system/images/desktop/desktop_4.JPG`** is referred to as `desktop_4.jpg`, so it doesn't load on case-sensitive servers (tested on Linux).
25. **`system/server/` is missing** from `siteroot`. See [Server Endpoints](Server-Endpoints.md).

## Built-in apps

26. **Terminal:** Save Script does nothing; the button's id is `terminalSaveButtom` but the code looks for `terminalSaveButton`.
27. **Settings:** the notification time box has no effect (its handler is a placeholder that only contains `alert('test')`); Add User's Save does nothing; switching pages uses `location.assign` and loses the AppID.
28. **Save File dialog:** Save and Cancel do nothing, and the dialog can't return a file to an app.
29. **Dialog and Save File are listed in `system/apps.xml`**, so they show up in the home menu as if they were apps.

## Unused files

These aren't referenced by anything and can be removed or finished:

* `system/js/jse_form.js` and `system/js/jse_search.js`: a third-party client-side search script ("JSE").
* `system/components/login/login.wcf`: a prototype of a login window in a different component format, calling `System` functions that don't exist.
* `apps/default/terminal/js/app.js` and `system/components/dialog/js/app.js`: old copies of the App object.
* `system/images/home2.png`, `home2.svg`, the `min_button` images, and the `.svg` versions of the window buttons.
* `apps/panels/hello world/` and the panel code. See [Panels](Panels.md).

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
