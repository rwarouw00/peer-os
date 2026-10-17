# Known Issues

Problems found while reviewing the `siteroot` code (Arctic Eagle v1.00.00), including the `system/server` PHP scripts. Items marked **tested** were confirmed by running the code in a browser; the rest come from reading it. File references are relative to `siteroot/`.

## Security

Fix these before putting Skylight OS on a server other people can reach. The first three are critical.

1. **Anyone can write any file on the server, without logging in (tested).** `system/server/saveFile.php` compares the cookie with the user's token from `LoggedInUsers.xml`. When the named user isn't logged in and no cookie is sent, both are `null` and PHP's `==` treats them as equal. The path isn't checked either, so a single request can create `system/anything.php`, which the server then runs. **Fix:** refuse the request unless the cookie is non-empty and equal (`===`) to a stored token; resolve the path with `realpath` and only allow writes inside the user's own folder; never allow `.php` files.
2. **Everyone's login token is public (tested).** `system/LoggedInUsers.xml` is an ordinary file under the website, and it holds the token of every logged-in user. Anyone can download it, put the token in a `Skylight_OS_Auth` cookie and save files as that user. **Fix:** keep sessions on the server (PHP's `$_SESSION`) or move the file out of the website.
3. **Any website opened in the Web Browser app can run code on the desktop (tested).** The desktop accepts messages from any window, without checking where they came from (`System.handleAppEvent`, `system/js/system.js`), and inserts notification and dialog text as HTML. A page from another site only has to call `top.postMessage(...)` with a notification whose body contains an `<img onerror=...>` tag, and its script runs with full access to the desktop and the logged-in user. **Fix:** in `handleAppEvent`, ignore messages whose `event.origin` isn't the site's own origin, and insert notification and dialog text with `textContent` instead of `innerHTML`.
4. **Passwords are plain text and public.** `auth.php` checks passwords against `system/settings/users.xml`, which stores them unencrypted and which anyone can download. The comparison uses `==`, so some different passwords match (for example `1e1` and `10`). The shipped `admin` account has an empty password. **Fix:** store hashes (`crypt()` with Blowfish on PHP 5.3+, `password_hash()` on 5.5+), keep the file outside the website, and set an admin password.
5. **Login tokens are guessable.** A token is `sha1(time())`: the hash of the second the user logged in, so anyone who knows roughly when someone logged in can work it out. The cookie is also readable by JavaScript. **Fix:** use PHP sessions, or at least random tokens (`openssl_random_pseudo_bytes`), and set the cookie's `httponly` flag.
6. **Shortcuts need no login.** `shortcuts.php` writes to one shared `shortcuts.xml` for anyone who calls it.
7. **The Save File dialog lists any folder.** `system/components/save dialog/index.php` lists whatever folder the `path` query value names, including folders outside the website.
8. **Apps have full access to the desktop.** Apps are pages on the same site as the desktop, so any app can call `parent.System` directly or change the desktop page. The [permissions](Permissions.md) described on the old wiki can't be enforced in the browser for that reason; real limits have to be enforced by the server scripts.
9. **Code is run from text in several places**: the Dialog's OK script (`eval`), context menu items (`onclick` attributes) and the Terminal. That is by design for the Terminal, but combined with issue 3 it lets outside pages run code.

## Server

10. **Saving fails on sites with a port number (tested).** `auth.php` sets the cookie's domain to the `Host` header including the port (for example `localhost:8000`); browsers reject that, so the cookie is never stored and every save is refused. **Fix:** leave the domain argument of `setcookie` empty.
11. **A second login for the same user can't save (tested).** Each login appends a token to `LoggedInUsers.xml`, but `saveFile.php` only checks the first.
12. **Failed writes are reported as success.** `saveFile.php` replies `{"error": "none"}` without checking `file_put_contents`, and the Settings app and logout ignore the reply anyway.
13. **PHP warnings break the JSON replies** when `display_errors` is on, because the scripts read `$_POST` and `$_COOKIE` values without checking they exist. A broken login reply leaves the login screen stuck.
14. **Usernames must be valid XML element names**, because `auth.php` uses them as element names in `LoggedInUsers.xml`.
15. **`get_mime_type` rereads `mime.types` for every file** the Save File dialog lists.

## Desktop and windows

16. **Toasts are mostly off screen (tested).** The `.toastAnimState0/1` styles in `system/css/system.css` push each 300px toast past the right edge at every screen size; only about 115px is visible. The toast's × button has no click handler either.
17. **A notification without a body breaks the toast (tested).** `System.toast.send` reads `text.length` on `undefined`; the tray entry is added but no toast appears.
18. **Notification click actions can't be used by apps.** Functions can't be sent through `postMessage` as JSON, so the `action` argument is lost.
19. **Every app URL ends in `&undefined` (tested).** `System.openWindow` appends its `args` parameter even when it is missing, and apps never pass it. [App.getQueryStringArgs](App.getQueryStringArgs.md) then reports an `"undefined"` key.
20. **The Dialog's OK script is never cleared.** `system/components/dialog/index.html` resets `'SKylight.dialog.onOK'` (capital K) instead of `'Skylight.dialog.onOK'`. Opening the Dialog from the home menu and clicking OK reruns the last dialog's script.
21. **Dialogs share one slot.** The message and script are passed through `localStorage`, so two dialogs opened together can swap or lose their text.
22. **Logging out leaves the desktop on screen** (tested). It also throws an error if the user isn't in `LoggedInUsers.xml`, and when the session's cookie wasn't stored (see the port issue above) the token isn't removed at all. See [Users and Login](Users-and-Login.md).
23. **A user without a settings file gets a broken desktop (tested):** an XML error dialog, no background and an empty home menu.
24. **Login:** Enter doesn't submit (tested); the username and password aren't URL-encoded, so a password containing `&` or `+` is sent wrongly; a reply that isn't JSON leaves the screen stuck on the progress icon.
25. **Clicking a desktop shortcut shows an error dialog (tested).** On every `mouseup` over a shortcut, `index.html` posts `[object Object]` to `shortcuts.php` (it passes an object to `encodeURIComponent` and uses a `path` variable left over from building the home menu); the script can't decode it, replies with nothing, and the desktop shows "There was an error creating the shortcut". Shortcuts also aren't reloaded at login (though `shortcuts.php` saves them), all start at the same spot, and apps with the same title clash.
26. **Unimplemented menu items:** Pin/Unpin, Remove and Delete App call `System` functions that don't exist. See [Context Menus](Context-Menus.md).
27. **Clicking a window doesn't bring it to the front**, and windows can't be resized.
28. **The clock shows `0:15 am` between midnight and 1 am.**
29. **An invalid `appData.json` leaves an empty taskbar slot**, because the taskbar element is added before the file is parsed.
30. **Any non-JSON message to the desktop throws an error** in `handleAppEvent` (tested). Harmless, but noisy, and browser extensions send such messages.

## Files and paths

31. **[App.saveFile](App.saveFile.md) doesn't encode values**, so contents with `&`, `+` or `%` are cut off or changed.
32. **Save paths are hard-coded as `../../../system/server/saveFile.php`.** Apps must be exactly three folders deep, and `System.saveFile` (used by logout) only works when Skylight OS is installed at the root of the website.
33. **`system/images/desktop/desktop_4.JPG`** is referred to as `desktop_4.jpg`, so it doesn't load on case-sensitive servers (tested on Linux).

## Built-in apps

34. **Terminal:** Save Script does nothing; the button's id is `terminalSaveButtom` but the code looks for `terminalSaveButton`.
35. **Settings:** the notification time box has no effect (its handler is a placeholder that only contains `alert('test')`); Add User's Save does nothing; switching pages uses `location.assign` and loses the AppID.
36. **Save File dialog:** Save and Cancel do nothing, and the dialog can't return a file to an app.
37. **Dialog and Save File are listed in `system/apps.xml`**, so they show up in the home menu as if they were apps.

## Unused files

These aren't referenced by anything and can be removed or finished:

* `system/js/jse_form.js` and `system/js/jse_search.js`: a third-party client-side search script ("JSE").
* `system/components/login/login.wcf`: a prototype of a login window in a different component format, calling `System` functions that don't exist.
* `apps/default/terminal/js/app.js` and `system/components/dialog/js/app.js`: old copies of the App object.
* `system/images/home2.png`, `home2.svg`, the `min_button` images, and the `.svg` versions of the window buttons.
* `apps/panels/hello world/` and the panel code. See [Panels](Panels.md).
* `system/server/shortcuts.json`: one line of test data; nothing reads it.

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._

_Updated after the `system/server` scripts were added to `siteroot`._
