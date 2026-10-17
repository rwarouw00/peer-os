# Server Endpoints

The server side of Skylight OS is a handful of PHP scripts in [`system/server/`](../siteroot/system/server/). They are plain PHP 5-style code with no framework: each script reads and writes XML files next to it. All calls from the browser are synchronous form posts (`application/x-www-form-urlencoded`).

| File | Called by | Job |
| --- | --- | --- |
| [`auth.php`](../siteroot/system/server/auth.php) | Login screen | Checks a username and password, starts a login |
| [`saveFile.php`](../siteroot/system/server/saveFile.php) | [App.saveFile](App.saveFile.md), Settings app, logout | Writes a file for a logged-in user |
| [`shortcuts.php`](../siteroot/system/server/shortcuts.php) | [App.addShortcut](App.addShortcut.md), shortcut drag | Records desktop shortcuts |
| [`functions.php`](../siteroot/system/server/functions.php) | [Save File Dialog](Save-File-Dialog.md) | `get_mime_type()` helper |
| [`mime.types`](../siteroot/system/server/mime.types) | `functions.php` | File extension → MIME type table |
| [`mime-types.xml`](../siteroot/system/server/mime-types.xml) | Save File Dialog | MIME type → icon table |
| [`shortcuts.xml`](../siteroot/system/server/shortcuts.xml) | `shortcuts.php` | Saved shortcuts |
| [`shortcuts.json`](../siteroot/system/server/shortcuts.json) | nothing | Leftover test data |

The scripts open their data files with relative paths such as `../settings/users.xml`, so they rely on PHP running with the script's own folder as the working directory (true for Apache's PHP module and PHP's built-in server).

> **Read [Known Issues](Known-Issues.md#security) before deploying.** The scripts were tested while writing this page: anyone can write any file on the server without logging in, including PHP files that then run.

## auth.php

| | |
| --- | --- |
| Request | `username`, `password` |
| Success | `{"error": "none"}` and a cookie |
| Failure | `{"error": "true"}` (wrong password or unknown user) |

How it works:

1. Loads [`system/settings/users.xml`](../siteroot/system/settings/users.xml) and looks for a `<user>` whose `username` and `password` attributes match. Passwords are stored and compared as **plain text**, with PHP's loose `==`, so for example the passwords `1e1` and `10` are treated as equal.
2. On a match, creates a login token: the SHA-1 hash of the current time in seconds (`sha1(time())`).
3. Sends the token as a cookie named `Skylight_OS_Auth` (path `/`, domain set to the request's `Host`, lasting until the browser closes, readable by JavaScript).
4. Appends `<username>token</username>` to [`system/LoggedInUsers.xml`](../siteroot/system/LoggedInUsers.xml).

Behaviour found in testing:

* **Saving fails if the site uses a port number.** The cookie's domain is the full `Host` header, port included (for example `localhost:8000`). Browsers reject a cookie domain with a port, so the cookie is never stored: logging in seems to work, but every save is refused. On the standard ports (80 or 443) it works.
* **Logging in twice breaks saving for the second session.** Each login adds another element to `LoggedInUsers.xml`, and `saveFile.php` only checks the first one, so only the oldest session for a user can save.
* The username becomes an XML element name, so usernames must be valid XML names (no spaces, not starting with a digit).
* If PHP is set to display warnings (`display_errors=On`), any warning is printed before the JSON and the login screen gets stuck, because the reply is no longer valid JSON.

## saveFile.php

| | |
| --- | --- |
| Request | `file`, `contents`, `user`, plus the `Skylight_OS_Auth` cookie |
| Success | `{"error": "none"}` |
| Refused | `{"error": "Operation not performed by a logged in user"}` |

It finds the first element named after `user` in `LoggedInUsers.xml`; if its token equals the cookie, it writes `contents` to `../../` + `file`, which is `file` relative to the site root (for example `system/settings/admin.xml`).

* **It writes without a login.** If `user` names someone who isn't logged in and no cookie is sent, both sides of the comparison are `null`, which PHP treats as equal, so the file is written. (Tested.)
* **Any path is accepted.** `file` can contain `../` and point anywhere the web server can write, and can end in `.php`. Combined with the point above, anyone can upload and run PHP code on the server. (Tested.)
* It reports success even if the write fails (for example, because the folder doesn't exist).
* The Settings app and logout ignore the reply, so a refused save looks the same to the user as a successful one.

## shortcuts.php

| | |
| --- | --- |
| Request | `contents`: URL-encoded JSON |
| Success | `NO ERROR` |
| Failure | No reply (the desktop then shows "There was an error creating the shortcut") |

The JSON's `action` decides what happens:

* `{"action": "add", "path": ..., "top": ..., "left": ...}` appends `<shortcut top="..." left="...">path</shortcut>` to `system/server/shortcuts.xml`.
* `{"action": "edit", ...}` finds the shortcut with the same `path` and updates its position. If several match, `NO ERROR` is printed once per match, which the desktop also treats as an error.

Limitations: there is **no login check** and one list is shared by all users; the browser never reads `shortcuts.xml` back; and the desktop's drag code sends `[object Object]` instead of an `edit` request, so clicking any shortcut produces the error dialog (tested; see [Known Issues](Known-Issues.md)). The shipped `shortcuts.xml` already contains three test entries for the Settings app.

## Save File dialog helpers

[`system/components/save dialog/index.php`](../siteroot/system/components/save%20dialog/index.php) lists a folder and gives each file an icon:

1. `get_mime_type($file, 'mime.types')` in `functions.php` takes the text after the last `.` and searches `mime.types` (Apache's standard list, plus a home-made `application/php` entry) for a line ending in that extension. It rereads the whole 1,000-line file for every file listed, and returns `false` for files without an extension.
2. `mime-types.xml` maps 46 MIME types to 14 icons in `system/images/mime/`, for example `<mime value="application/pdf" icon="pdf.png"/>`.
3. Folders get `folder.png`, executable files `executable.png`, and anything else `file.png`.

## Data files

The scripts keep their data inside the website, so the web server will hand these files to anyone who asks for them:

| File | Contains |
| --- | --- |
| `system/settings/users.xml` | Usernames and plain-text passwords |
| `system/LoggedInUsers.xml` | The login token of everyone logged in (enough to act as them) |
| `system/settings/<username>.xml` | Each user's settings |
| `system/server/shortcuts.xml` | Shortcuts |

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._

_Updated after the `system/server` scripts were added to `siteroot`._
