# App API

Every page of an app loads two scripts, in this order:

```html
<script src="../../system/js/core.js"></script>
<script src="../../system/js/app.js"></script>
```

(The paths assume the app is in `apps/<name>/`; adjust them if it lives elsewhere. Nothing else depends on the folder depth.)

## Summary

| Function | Returns |
| --- | --- |
| [`App.openWindow(path)`](#appopenwindow) | nothing |
| [`App.closeWindow([appID])`](#appclosewindow) | nothing |
| [`App.openDialog(message[, onOK])`](#appopendialog) | nothing |
| [`App.postNotification(header[, body])`](#apppostnotification) | nothing |
| [`App.getSettings()`](#appgetsettings) | object |
| [`App.saveSettings(changes)`](#appsavesettings) | nothing |
| [`App.setBackground(path)`](#appsetbackground) | nothing |
| [`App.addShortcut(path)`](#appaddshortcut) | nothing |
| [`App.getAppID()`](#appgetappid) | string |
| [`App.getUsername()`](#appgetusername) | string |
| [`App.getQueryStringArgs()`](#appgetquerystringargs) | object |
| [`App.gotoLocation(path[, queryString])`](#appgotolocation) | nothing |
| [`App.openFile(url)`](#appopenfile) | string or `null` |
| [`App.readFile(name)`](#appreadfile) | string or `null` |
| [`App.writeFile(name, contents)`](#appwritefile) | `{ ok, error }` |
| [`App.listFiles()`](#applistfiles) | array or `null` |
| [`App.parseXml(text)`](#appparsexml) | document or `null` |
| [`App.serializeXml(doc)`](#appserializexml) | string |

Functions that return nothing send a message to the desktop and return at once (see [Architecture](architecture.md#messages-between-apps-and-the-desktop)). Their arguments must be plain data, except `onOK`. Functions that return a value call the server or the browser directly and wait for the answer.

**Paths:** app and image paths (`openWindow`, `addShortcut`, `setBackground`) are relative to the site root. `openFile` and `gotoLocation` take ordinary links, relative to your page. `readFile`, `writeFile` and `listFiles` use plain file names in the user's private storage.

## Windows

### App.openWindow

```javascript
App.openWindow('apps/web-browser/');
```

Opens the app in the given folder (site root relative, ending in `/`) in a new window. Each call opens a new window. If the folder isn't an app, the user gets a notification.

### App.closeWindow

```javascript
App.closeWindow();           // close this app's own window
App.closeWindow(someAppID);  // close another window
```

## Dialogs and notifications

### App.openDialog

```javascript
App.openDialog('Delete this file?', function () {
	App.writeFile('notes.txt', '');
});
```

Shows a dialog with the message and **OK** and **Cancel** buttons. `onOK`, if given, is a function in your app that runs only when OK is clicked; it runs in your app with all your variables available. The message is plain text: HTML tags are shown as typed.

![A dialog](images/dialog.png)

### App.postNotification

```javascript
App.postNotification('Download complete', 'report.pdf was saved.');
```

Shows a toast and adds the notification to the home menu's Notifications tab. `body` is optional. Both are plain text. Long headers are cut short with "…" in the toast; the Notifications tab shows everything.

## Settings and the desktop

### App.getSettings

```javascript
var settings = App.getSettings();
// { background: "system/images/backgrounds/3.jpg", notificationTime: 7000, shortcuts: [...] }
```

Returns the user's settings, or `null` if the session has ended. See [Configuration and data](configuration.md#settings) for the fields.

### App.saveSettings

```javascript
App.saveSettings({ notificationTime: 3000 });
```

Asks the desktop to save and apply settings. Only the known settings with valid values are accepted; if the server refuses, the user gets a notification.

### App.setBackground

```javascript
App.setBackground('system/images/backgrounds/2.jpg');
```

Shorthand for `App.saveSettings({ background: path })`. The change is saved, so it lasts.

### App.addShortcut

```javascript
App.addShortcut('apps/settings/');
```

Puts a shortcut to the app on the desktop, using its 64px icon. Shortcuts are saved in the user's settings; adding one that already exists does nothing.

## Identity and navigation

### App.getAppID

Returns this window's AppID (a string of digits). Rarely needed: `App.closeWindow()` closes your own window without it.

### App.getUsername

Returns the logged-in user's name, as passed in the window's address. Use it for display. The server never trusts it: every server call checks the session instead.

### App.getQueryStringArgs

```javascript
App.getQueryStringArgs();  // { "app-id": "7453540744585879", "username": "admin" }
```

Returns the page's query string as an object of decoded values.

### App.gotoLocation

```javascript
App.gotoLocation('details.html', 'item=42');
```

Moves your window to another page, keeping `app-id` and `username` (and any other values) in the address. Values in `queryString` replace existing ones with the same name. Use it instead of plain links when moving between your app's pages.

## Files

There are two kinds of file:

* **Site files**: anything under `public/`, readable by anyone, never writable by apps. Use `openFile`.
* **User files**: private to the logged-in user, in `data/users/<name>/files/`. Use `readFile`, `writeFile` and `listFiles`.

### App.openFile

```javascript
var list = JSON.parse(App.openFile('../../system/apps.json'));
```

Downloads a site file and returns its text, or `null` if it can't be loaded. `url` is relative to your page.

### App.readFile

```javascript
var text = App.readFile('notes.txt');  // null if there is no such file
```

### App.writeFile

```javascript
var reply = App.writeFile('notes.txt', text);
if (!reply.ok) {
	App.openDialog('Could not save: ' + reply.error);
}
```

Saves text to the user's storage, replacing any file with that name. Names may use letters, digits, spaces, `-`, `_` and `.` (up to 100 characters, not starting with `.`); files are limited to 1 MB. Any text can be saved; there's no need to encode it.

`App.saveFile` is another name for `writeFile`, kept for apps written for earlier releases.

### App.listFiles

```javascript
App.listFiles();  // ["notes.txt", "todo.txt"]
```

## XML

### App.parseXml

```javascript
var doc = App.parseXml('<list><item>One</item></list>');
if (doc) { doc.getElementsByTagName('item').length; }
```

Returns an XML document, or `null` if the text isn't valid XML.

### App.serializeXml

Turns an XML document back into text.
