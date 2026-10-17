# Server API

The server is four small PHP scripts in [`public/api/`](../site3/public/api/) and one shared library, [`lib/skylight.php`](../site3/lib/skylight.php), which sits outside the web root. The code avoids PHP features newer than 5.3 (no short array syntax, no `??`), and needs 5.3.7 for bcrypt password hashes.

## Conventions

* **Replies** are always JSON objects with an `ok` field: `{"ok": true, ...}` or `{"ok": false, "error": "A sentence for the user."}`. They are built with `json_encode`.
* **Changes** must be `POST` requests carrying the header `X-Requested-With: Skylight`. Browsers won't add a custom header to a request from another site without asking the server first, so a malicious page can't make the user's browser change anything. `Skylight.post` in `core.js` adds the header.
* **Logins** are PHP sessions. The session cookie is called `skylight` and is `HttpOnly`.
* **Input** is read with `input($name)`, which only ever returns a string and undoes magic quotes on old PHP 5 installs.

From the browser, call these through `Skylight.get(name, fields)` and `Skylight.post(name, fields)`, or through the [App API](app-api.md).

## login.php

`POST username, password` → `{ ok, user, settings }`

Checks the password against the bcrypt hash in `data/users.json`, starts a new session (`session_regenerate_id`) and returns the user's settings. On failure: `"Either the username or password is incorrect."` (the same message whether the user exists or not).

## logout.php

`POST` → `{ ok }`

Ends the session. The desktop then reloads, which brings back the login screen.

## settings.php

`GET` → `{ ok, user, settings }`

Returns the logged-in user and their settings, or `{ ok: false }` if nobody is logged in. The desktop calls it on page load to skip the login screen when a session is still active.

`POST changes` → `{ ok, user, settings }`

`changes` is a JSON object of settings to change. Each one is checked by `clean_setting()`, and an unknown name or a bad value rejects the whole request:

| Setting | Rule |
| --- | --- |
| `background` | Image path relative to the site root: letters, digits, `_`, `-`, `/`, ending in `.jpg`, `.jpeg`, `.png` or `.gif` |
| `notificationTime` | Milliseconds, 1000 to 60000 |
| `shortcuts` | Up to 50 `{ path, left, top }` objects; `path` must look like `apps/name/` |

## files.php

The logged-in user's own files, stored in `data/users/<name>/files/`.

| Request | Reply |
| --- | --- |
| `GET` | `{ ok, files: ["notes.txt", ...] }` |
| `GET name` | `{ ok, contents }`, or `ok: false` if there's no such file |
| `POST name, contents` | `{ ok }` |

Names may use letters, digits, spaces, `-`, `_` and `.`, up to 100 characters, and may not start with `.`. There are no folders, so a name can't point outside the user's own folder. Files are limited to 1 MB.

## lib/skylight.php

| Function | Purpose |
| --- | --- |
| `reply($data)`, `fail($message)` | Send a JSON reply and stop |
| `input($name)`, `is_post()`, `require_post()` | Read requests safely |
| `start_session()`, `current_user()`, `require_user()` | Sessions; `require_user` replies with an error if nobody is logged in |
| `read_json($path, $default)`, `write_json($path, $data)` | Data files; writes are locked and folders are created as needed |
| `is_valid_username()`, `load_users()`, `user_folder()` | Users |
| `hash_password()`, `check_password()` | bcrypt through `crypt()` with a random salt |
| `default_settings()`, `load_settings()`, `clean_setting()` | Settings |
| `user_file($user, $name)` | Checks a file name and returns its path |

A new endpoint is a few lines:

```php
<?php
require dirname(__FILE__) . '/../../lib/skylight.php';

$user = require_user();
reply(array('ok' => true, 'message' => 'Hello, ' . $user));
```

## tools/add-user.php

```
php tools/add-user.php <username> <password>
```

Adds a user to `data/users.json`, or changes the password of an existing one. It refuses to run from the web.
