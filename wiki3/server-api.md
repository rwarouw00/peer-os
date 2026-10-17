# Server API

The browser code calls PHP scripts in `system/server/`. **That folder isn't included in `siteroot`**, so this page describes each script as the browser code uses it: what it sends and what it expects back. Use it to write or restore the scripts.

Every request is an HTTP `POST` with `Content-Type: application/x-www-form-urlencoded`, made synchronously. Except where noted, the browser doesn't URL-encode the values, so the scripts receive whatever survives form decoding.

## auth.php

Checks a login.

| Request field | Value |
| --- | --- |
| `username` | As typed |
| `password` | As typed |

**Response:** JSON. `{"error": "none"}` means success; any other JSON means failure (the user sees a fixed "incorrect" message). A response that isn't JSON leaves the login screen stuck.

The script should also add an element for the user to `system/LoggedInUsers.xml`, because logout expects one. Nothing in the browser proves later requests come from the logged-in user; to protect `saveFile.php`, start a PHP session here and check it there.

## saveFile.php

Writes a file. Used by `App.saveFile`, the Settings app and logout.

| Request field | Value |
| --- | --- |
| `file` | Path relative to the site root, for example `system/settings/admin.xml` |
| `contents` | The new contents. Apps are told to `encodeURIComponent` it; the system's own calls don't. |
| `user` | The username the caller claims |

**Response:** any text; it is passed back to the caller unchanged.

This script decides what can be overwritten, so it must check that the caller is logged in (session, not the `user` field) and that `file` stays inside the folders that user may write. See [Security](security.md).

## shortcuts.php

Records desktop shortcuts.

| Request field | Value |
| --- | --- |
| `contents` | URL-encoded JSON |

For a new shortcut the JSON is:

```json
{"action": "add", "path": "apps/default/web browser/", "top": "30px", "left": "0px"}
```

An `edit` action was intended for moved shortcuts, but because of a bug the browser sends `[object Object]` instead. Nothing reads shortcuts back at login.

**Response:** the exact text `NO ERROR` on success. Anything else is shown to the user in a dialog.

## Files used by the Save File dialog

`system/components/save dialog/index.php` includes these:

| File | Contents |
| --- | --- |
| `functions.php` | Defines `get_mime_type($file, $mimeTypesPath)`, returning a MIME type string |
| `mime-types.xml` | `<mime value="..." icon="..."/>` elements mapping MIME types to icons in `system/images/mime/` |
| `mime.types` | A MIME type table, passed to `get_mime_type` |

## Test stand-in

[Getting started](getting-started.md#run-it-locally) has a ten-line router script that answers `auth.php`, `saveFile.php` and `shortcuts.php` so the desktop can be tried without the real scripts.
