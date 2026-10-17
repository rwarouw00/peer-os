# Configuration Files

Skylight OS keeps its configuration in XML and JSON files under `system/`. The desktop reads them over HTTP, so the web server must publish them.

| File | Read by | Purpose |
| --- | --- | --- |
| [`system/apps.xml`](../siteroot/system/apps.xml) | desktop at login | Apps shown in the home menu |
| `system/settings/<username>.xml` | desktop at login, Settings app | Per-user background and notification time. See [Users and Login](Users-and-Login.md) |
| [`system/settings/users.xml`](../siteroot/system/settings/users.xml) | `auth.php` | Accounts, with plain-text passwords |
| [`system/LoggedInUsers.xml`](../siteroot/system/LoggedInUsers.xml) | `auth.php`, `saveFile.php`, logout | Login tokens of users currently logged in |
| [`system/server/shortcuts.xml`](../siteroot/system/server/shortcuts.xml) | `shortcuts.php` | Desktop shortcuts (written, never read back) |
| [`system/server/mime-types.xml`](../siteroot/system/server/mime-types.xml), [`mime.types`](../siteroot/system/server/mime.types) | Save File dialog | File-type icons. See [Server Endpoints](Server-Endpoints.md#save-file-dialog-helpers) |
| `system/components/context menus/*.xml` | desktop on right-click | Menu items. See [Context Menus](Context-Menus.md) |
| `<app folder>/appData.json` | desktop when a window opens | Window title, size and icons. See [appData.json](appData.json.md) |

## apps.xml

One `<app>` element per app, in the order they appear in the home menu. `path` is the app's folder relative to the site root, ending in `/`.

```
<?xml version="1.0" encoding="utf-8" ?>

<apps>
	<app path="apps/default/terminal/"/>
	<app path="system/components/dialog/"/>
	<app path="apps/default/settings/"/>
	<app path="system/components/save dialog/"/>
	<app path="apps/default/web browser/"/>
</apps>
```

The shipped file lists the Dialog and Save File components as well as the three apps, so they appear in the home menu too. Remove those two lines if you don't want users opening them directly. The list must contain at least one app.

Changes take effect at the next login.

## users.xml

```
<users>
	<user username="admin" password=""/>
</users>
```

One `<user>` per account, read by `auth.php`. Passwords are plain text, and the shipped `admin` account has an empty password. To add a user, add a line here and create their settings file (see [Users and Login](Users-and-Login.md)). Usernames must be valid XML element names (letters, digits, `-`, `_`, `.`; not starting with a digit), because they are also used as element names in `LoggedInUsers.xml`.

## LoggedInUsers.xml

```
<?xml version="1.0" encoding="utf-8"?>
<users><admin>6e91a3f72bec281fcb2fdfe9b26b01acf0aa5cb8</admin></users>
```

`auth.php` appends one element per login, named after the user and holding the login token that is also in the browser's `Skylight_OS_Auth` cookie. `saveFile.php` compares the cookie with the first element for the user. Logout removes that first element. The file must be writable by the web server, and because it is inside the website, anyone can read the tokens in it (see [Known Issues](Known-Issues.md#security)).

## shortcuts.xml

```
<shortcuts><shortcut top="30px" left="0px">apps/default/settings/</shortcut></shortcuts>
```

Written by `shortcuts.php`, shared by all users, and never read by the desktop. The shipped copy contains three identical test entries.

## Browser storage

The desktop also keeps a few values in the browser's `localStorage`:

| Key | Holds |
| --- | --- |
| `Skylight.user` | Username of the logged-in user. Cleared at logout. |
| `Skylight.dialog.message` | Message for the next [Dialog](Dialogs.md). |
| `Skylight.dialog.onOK` | Script for the next Dialog's OK button. |

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._

_Updated after the `system/server` scripts were added to `siteroot`._
