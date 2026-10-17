# Configuration Files

Skylight OS keeps its configuration in XML and JSON files under `system/`. The desktop reads them over HTTP, so the web server must publish them.

| File | Read by | Purpose |
| --- | --- | --- |
| [`system/apps.xml`](../siteroot/system/apps.xml) | desktop at login | Apps shown in the home menu |
| `system/settings/<username>.xml` | desktop at login, Settings app | Per-user background and notification time. See [Users and Login](Users-and-Login.md) |
| [`system/settings/users.xml`](../siteroot/system/settings/users.xml) | nothing in the browser | Legacy user list with plain-text passwords |
| [`system/LoggedInUsers.xml`](../siteroot/system/LoggedInUsers.xml) | logout | Users currently logged in |
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

## LoggedInUsers.xml

```
<?xml version="1.0" encoding="utf-8"?>
<users></users>
```

Logout removes the element named after the user (for example `<admin/>`). Nothing in `siteroot` adds that element, so the server's `auth.php` is expected to.

## Browser storage

The desktop also keeps a few values in the browser's `localStorage`:

| Key | Holds |
| --- | --- |
| `Skylight.user` | Username of the logged-in user. Cleared at logout. |
| `Skylight.dialog.message` | Message for the next [Dialog](Dialogs.md). |
| `Skylight.dialog.onOK` | Script for the next Dialog's OK button. |

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
