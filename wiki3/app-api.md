# App API

The `App` object is how an app works with Skylight OS. Load it in every page of your app:

```html
<script src="../../../system/js/app.js"></script>
```

## Summary

| Function | Returns | Goes through the desktop |
| --- | --- | --- |
| [`App.openWindow(path)`](#appopenwindow) | nothing | yes |
| [`App.closeWindow(appID)`](#appclosewindow) | nothing | yes |
| [`App.openDialog(message, onOK)`](#appopendialog) | nothing | yes |
| [`App.postNotification(header, body)`](#apppostnotification) | nothing | yes |
| [`App.setBackground(path)`](#appsetbackground) | nothing | yes |
| [`App.addShortcut(path)`](#appaddshortcut) | nothing | yes |
| [`App.getAppID()`](#appgetappid) | string | no |
| [`App.getUsername()`](#appgetusername) | string | no |
| [`App.getQueryStringArgs()`](#appgetquerystringargs) | object | no |
| [`App.gotoLocation(path, queryString)`](#appgotolocation) | nothing | no |
| [`App.openFile(path)`](#appopenfile) | string | no |
| [`App.saveFile(path, contents)`](#appsavefile) | string | no (server) |
| [`App.parseXml(xml)`](#appparsexml) | XML document | no |
| [`App.serializeXml(doc)`](#appserializexml) | string | no |

Functions that go through the desktop send it a message and return immediately; they can't report success or failure (see [Architecture](architecture.md#how-apps-talk-to-the-desktop)). Their arguments must be plain data: functions are dropped.

**Paths.** Window and image paths (`openWindow`, `setBackground`, `addShortcut`, `saveFile`) are relative to the **site root**. `openFile` and `gotoLocation` paths are relative to **your page**, like links.

## Windows

### App.openWindow

```javascript
App.openWindow(path)
```

Opens an app in a new window.

| Parameter | Description |
| --- | --- |
| `path` | The app's folder, relative to the site root, ending in `/`. The folder must contain `appData.json` and an `index.html`. |

```javascript
App.openWindow('apps/default/web browser/');
```

Each call opens a new window, even if the app is already open. The new window's AppID isn't returned.

### App.closeWindow

```javascript
App.closeWindow(appID)
```

Closes the window with the given AppID, and removes its taskbar icon. To close your own window:

```javascript
App.closeWindow(App.getAppID());
```

Any window can be closed if you know its AppID. Passing an AppID that doesn't exist causes an error on the desktop.

## Dialogs and notifications

### App.openDialog

```javascript
App.openDialog(message, onOK)
```

Shows a dialog window with a message and **OK** and **Cancel** buttons. Both buttons close it.

| Parameter | Description |
| --- | --- |
| `message` | Text to show. HTML is allowed. |
| `onOK` | Optional. JavaScript **source code, as a string**, to run when OK is clicked. |

![A dialog](images/dialog.png)

The `onOK` code runs with `eval` inside the dialog's own window, which has its own `App` object. It can't see your app's variables, and your app isn't told which button was clicked.

```javascript
App.openDialog('Switch to the space background?',
	"App.setBackground('system/images/desktop/desktop_3.jpg');");
```

Button labels can't be changed. See also [UI components](ui-components.md#dialogs).

### App.postNotification

```javascript
App.postNotification(header, body)
```

Shows a toast at the top right of the screen and adds the notification to the home menu's Notifications tab.

| Parameter | Description |
| --- | --- |
| `header` | Title. The toast shows the first 20 characters. |
| `body` | Message. The toast shows the first 110 characters. **Always pass a body**, even `''`: without one, no toast appears. |

Both are inserted as HTML. Never include text from untrusted sources. A third parameter, `action`, exists in the code but can't be used from apps.

```javascript
App.postNotification('Download complete', 'report.pdf was saved.');
```

## Desktop

### App.setBackground

```javascript
App.setBackground(path)
```

Changes the desktop background image until the next login. `path` is relative to the site root. To keep the change, also update the user's settings file, as the Settings app does:

```javascript
var file = 'system/settings/' + App.getUsername() + '.xml';
var settings = App.parseXml(App.openFile('../../../' + file));
settings.getElementsByTagName('background')[0].setAttribute('src', path);
App.saveFile(file, App.serializeXml(settings));
App.setBackground(path);
```

### App.addShortcut

```javascript
App.addShortcut(path)
```

Puts an icon for the app in folder `path` (site root relative, ending in `/`) on the desktop, using the manifest's `Xlarge` icon and title. Double-clicking it opens the app.

![A desktop shortcut](images/shortcut.png)

Shortcuts aren't restored after a reload, all appear at the same spot (drag them apart), and two apps with the same title can't both have one. The desktop also posts the shortcut to `shortcuts.php` ([Server API](server-api.md#shortcutsphp)) and shows a dialog if that fails.

## Identity and navigation

### App.getAppID

```javascript
App.getAppID()  // → "0.8566454929483042"
```

Returns this window's AppID, a random number between 0 and 1 assigned when the window opened, **as a string**. Use it to close your own window.

### App.getUsername

```javascript
App.getUsername()  // → "admin"
```

Returns the logged-in user's name. Like the AppID, it comes from the page's query string, so it is only correct on pages the desktop opened (or reached with `gotoLocation`), and it isn't proof of identity.

### App.getQueryStringArgs

```javascript
App.getQueryStringArgs()
// → { "app-id": "0.8566454929483042", "username": "admin", "undefined": "undefined" }
```

Returns the page's query string as an object of decoded names and values. The `"undefined"` entry is a desktop bug and can be ignored.

### App.gotoLocation

```javascript
App.gotoLocation(path, queryString)
```

Moves your window to another page while keeping the query string, so `getAppID` and `getUsername` keep working. Use it instead of links or `location.assign`.

| Parameter | Description |
| --- | --- |
| `path` | Page to load, relative to the current page. |
| `queryString` | Extra values to add, such as `'tab=2'`. Pass `''` if there are none, or `&undefined` is added. |

The new address is `path + current query string + '&' + queryString`. Values are added each time and never removed; when a name repeats, `getQueryStringArgs` returns the last value.

```javascript
App.gotoLocation('details.html', 'item=42');
```

## Files and XML

### App.openFile

```javascript
App.openFile(path)  // → file contents as a string
```

Downloads a file from the web server and returns its text. `path` is relative to **your page**, so files elsewhere on the site start with `../../../`:

```javascript
var xml = App.openFile('../../../system/apps.xml');
```

The call blocks until the file arrives. Errors aren't reported: a missing file returns the server's error page. Only files the web server publishes can be read.

### App.saveFile

```javascript
App.saveFile(path, contents)  // → the server's reply
```

Sends a file to the server script `system/server/saveFile.php` ([Server API](server-api.md#savefilephp)) and returns its reply. That script isn't included in `siteroot`.

| Parameter | Description |
| --- | --- |
| `path` | Where to save, relative to the **site root**, for example `'system/settings/admin.xml'`. |
| `contents` | The text to save. **Encode it with `encodeURIComponent` first**; the function doesn't, and `&`, `+` or `%` in the text would corrupt it. |

```javascript
App.saveFile('system/notes/' + App.getUsername() + '.txt', encodeURIComponent(text));
```

The current username is sent along with the file. Only works for apps three folders below the site root.

### App.parseXml

```javascript
App.parseXml(xmlString)  // → XML document
```

Parses XML text into a document you can query with `getElementsByTagName`, `getAttribute` and so on. If the XML is invalid, the user sees an error dialog and the document contains a `<parsererror>` element.

### App.serializeXml

```javascript
App.serializeXml(xmlDocument)  // → string
```

Turns an XML document back into text, ready for `saveFile`.

## Not in this release

Functions documented for later releases, such as `App.speak`, `App.registerError`, `App.selectFileForOpen` and `App.loadTheme`, don't exist here. See [Version notes](version-notes.md).
