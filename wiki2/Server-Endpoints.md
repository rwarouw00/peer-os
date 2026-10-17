# Server Endpoints

The browser code calls several files in `system/server/`, but **that folder isn't part of `siteroot`**. Without it you can't log in, and saving and shortcuts fail. This page describes what each file must do, based on how the browser code calls it, so it can be recreated.

All requests are form posts (`Content-Type: application/x-www-form-urlencoded`), but the browser code doesn't URL-encode the values except where noted. All calls are synchronous.

## auth.php

Called by the login screen.

| | |
| --- | --- |
| Request | `POST system/server/auth.php`, fields `username`, `password` |
| Success | JSON `{"error": "none"}` |
| Failure | Any other JSON, for example `{"error": "bad password"}` (the text isn't shown) |

The reply **must be JSON**: anything else, such as a PHP error page or a 404, throws a JavaScript error and the login screen is left showing a spinning progress icon with no message. A complete `auth.php` should probably also add the user to `system/LoggedInUsers.xml`, since logout expects to find them there.

## saveFile.php

Called by [App.saveFile](App.saveFile.md), the Settings app, and logout.

| | |
| --- | --- |
| Request | `POST system/server/saveFile.php`, fields `file`, `contents`, `user` |
| Reply | Any text; it is returned to the caller unchanged |

`file` is relative to the site root (for example `system/settings/admin.xml`). `user` is whatever the caller claims, so the script must check permissions itself and must not trust the path: without checks, any app could overwrite any file, including `system/settings/users.xml` or the PHP scripts.

## shortcuts.php

Called by [App.addShortcut](App.addShortcut.md) and when a shortcut is clicked or dropped.

| | |
| --- | --- |
| Request | `POST system/server/shortcuts.php`, field `contents` (URL-encoded JSON) |
| Success | The text `NO ERROR` |
| Failure | Any other text; it is shown in a dialog |

For a new shortcut, `contents` is `{"action":"add","path":"apps/default/web browser/","top":"30px","left":"0px"}`. Clicking or dropping a shortcut sends `[object Object]` instead, because of a bug (see [Known Issues](Known-Issues.md)). Nothing reads saved shortcuts back at login.

## Used by the Save File dialog

The [Save File Dialog](Save-File-Dialog.md)'s `index.php` also needs:

| File | Used for |
| --- | --- |
| `functions.php` | Must define `get_mime_type($file, $mimeTypesPath)`, returning a MIME type such as `text/plain` |
| `mime-types.xml` | `<mime value="text/plain" icon="text.png"/>` elements mapping a MIME type to an icon in `system/images/mime/` |
| `mime.types` | A MIME type list (Apache's `mime.types` format, presumably), passed to `get_mime_type` |

## Running without the server scripts

To try the desktop locally you can serve `siteroot` with PHP's built-in server and a small router script that answers the three endpoints. This is for testing only; it accepts any login and saves nothing:

```
<?php
// router.php — run from siteroot with: php -S localhost:8000 /path/to/router.php
$uri = urldecode(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH));
if (str_starts_with($uri, '/system/server/')) {
    if (str_ends_with($uri, 'auth.php')) { echo json_encode(['error' => 'none']); return true; }
    echo 'NO ERROR'; return true;
}
return false;
```

Log in as `admin` (the only user with a settings file). The screenshots in this wiki were taken this way.

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
