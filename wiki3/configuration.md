# Configuration

Skylight OS keeps its configuration in files under `system/`. The browser reads them over HTTP, so they must be published by the web server; changes go through the [server scripts](server-api.md), so the web server must be able to write them.

| File | Purpose | Read |
| --- | --- | --- |
| [`system/apps.xml`](#appsxml) | Apps in the home menu | At login |
| [`system/settings/<username>.xml`](#user-settings) | Each user's background and toast time | At login; by the Settings app |
| [`system/settings/users.xml`](#usersxml) | Accounts and passwords | By `auth.php` at login |
| [`system/LoggedInUsers.xml`](#loggedinusersxml) | Login tokens | By `auth.php`, `saveFile.php` and logout |
| [`system/server/shortcuts.xml`](#shortcutsxml) | Desktop shortcuts | Written by `shortcuts.php`; never read |
| `system/server/mime.types`, `mime-types.xml` | File-type icons | By the Save File dialog ([Server API](server-api.md#save-file-dialog-helpers)) |
| [`system/components/context menus/*.xml`](#context-menus) | Right-click menus | On right-click |
| `<app>/appData.json` | Each app's window | See [App manifest](app-manifest.md) |

## apps.xml

Lists the apps in the home menu, in order. `path` is the app's folder relative to the site root, ending in `/`.

```xml
<?xml version="1.0" encoding="utf-8" ?>

<apps>
	<app path="apps/default/terminal/"/>
	<app path="apps/default/settings/"/>
	<app path="apps/default/web browser/"/>
</apps>
```

The shipped file also lists `system/components/dialog/` and `system/components/save dialog/`, which makes those system windows appear in the menu; removing them is recommended. The list must not be empty. Changes apply at the next login.

## User settings

Each user needs `system/settings/<username>.xml`. Without it, the desktop doesn't finish loading after login.

```xml
<?xml version="1.0" encoding="utf-8" ?>

<settings>
	<background src="system/images/desktop/desktop_3.jpg"/>
	<displayNotificationsFor value="7000"/>
</settings>
```

| Element | Attribute | Meaning |
| --- | --- | --- |
| `background` | `src` | Background image, relative to the site root. The Settings app updates it. |
| `displayNotificationsFor` | `value` | How long toasts show, in milliseconds. |

Both elements are required. To add a user, see [users.xml](#usersxml).

Backgrounds supplied with the system are `system/images/desktop/desktop_1.jpg` to `desktop_6.jpg`. (`desktop_4` is named `desktop_4.JPG` on disk; rename it to `.jpg` for case-sensitive servers.)

## users.xml

```xml
<users>
	<user username="admin" password=""/>
</users>
```

The accounts `auth.php` checks at login. Passwords are **plain text**, the shipped `admin` password is empty, and anyone can download the file; see [Security](security.md).

To add a user:

1. Add `<user username="alice" password="..."/>`. The username must be a valid XML element name (letters, digits, `-`, `_`, `.`; not starting with a digit), because it is also used as an element name in `LoggedInUsers.xml`.
2. Copy `system/settings/admin.xml` to `system/settings/alice.xml`.

## LoggedInUsers.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<users><admin>6e91a3f72bec281fcb2fdfe9b26b01acf0aa5cb8</admin></users>
```

One element per login, named after the user and holding their token (the same value as their `Skylight_OS_Auth` cookie). `auth.php` appends; `saveFile.php` checks the user's first element; logout removes the first element. Ship it empty: `<users></users>`.

## shortcuts.xml

```xml
<shortcuts><shortcut top="30px" left="0px">apps/default/settings/</shortcut></shortcuts>
```

In `system/server/`. Shared by all users and never read by the desktop. The shipped copy contains three identical test entries.

## Context menus

Each right-click menu is an XML file in `system/components/context menus/`:

| File | Shown when right-clicking |
| --- | --- |
| `taskbarItem.xml` | A taskbar icon |
| `appItem.xml` | An icon in the home menu |
| `panelTitleBar.xml` | A panel's title bar (panels are unfinished) |

```xml
<contextmenu>
	<item value="Close">
		System.closeWindow(System.selected.parentNode.getAttribute('data-appID'));
	</item>
</contextmenu>
```

`value` is the label. The element's text is JavaScript that runs on the desktop page when the item is clicked; `System.selected` is the element that was right-clicked. The code is placed in an HTML `onclick` attribute, so it can't contain double quotes. Which file goes with which element is fixed in `index.html`.

## Browser storage

| `localStorage` key | Holds |
| --- | --- |
| `Skylight.user` | The logged-in username |
| `Skylight.dialog.message` | Message for the Dialog window being opened |
| `Skylight.dialog.onOK` | OK script for the Dialog window being opened |
