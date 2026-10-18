# Migrating apps from Arctic Eagle

Apps written for the original code ([`../siteroot`](../siteroot), documented in [`../wiki2`](../wiki2/README.md)) need a few changes to run on site03. The `App` function names are the same; what changed is mostly where files live and how dialogs and saving work.

## Checklist

1. **Move the folder** from `apps/default/<name>/` to `apps/<name>/`, using lowercase and hyphens instead of spaces (`web browser` → `web-browser`).
2. **Rename `appData.json` to `app.json`** and change it:
   * icon keys `small`/`medium`/`large`/`Xlarge` → `16`/`32`/`48`/`64`;
   * icon paths relative to the app's folder (`icons/16.png`), not the site root;
   * `width` and `height` may now be numbers;
   * remove `UI` and `permissions` if present (they were never used).
3. **Update the script tags.** Load `core.js` before `app.js`:

   ```html
   <link href="../../system/css/ui.css" rel="stylesheet"/>
   <script src="../../system/js/core.js"></script>
   <script src="../../system/js/app.js"></script>
   ```

4. **Add the app to `system/apps.json`** (it replaces `system/apps.xml`).
5. Go through the function changes below.

## Function changes

| Function | Arctic Eagle | site03 |
| --- | --- | --- |
| `App.openDialog(message, onOK)` | `onOK` was a **string of code**, run inside the dialog's window; `message` was HTML | `onOK` is a **function**, run inside your app; `message` is plain text |
| `App.saveFile(path, contents)` | Wrote any path under the site root, through `saveFile.php`; contents had to be URL-encoded | Same as `App.writeFile(name, contents)`: a plain file name in the user's private storage; no encoding needed; returns `{ ok, error }` |
| `App.openFile(url)` | Returned the server's error page for a missing file | Returns `null` for a missing file. For files the app saved, use `App.readFile(name)` |
| `App.postNotification(header, body, action)` | `body` was required; HTML allowed; `action` was ignored | `body` optional; plain text; no `action` |
| `App.closeWindow(appID)` | `appID` required | Optional: leave it out to close your own window |
| `App.setBackground(path)` | Changed the background until the next login; the app had to update the settings file itself | Saves the setting too. Background paths are now `system/images/backgrounds/1.jpg` to `6.jpg` |
| `App.gotoLocation(path, query)` | Appended to the query string every time; `&undefined` if `query` was missing | Merges values; `query` optional |
| `App.parseXml(text)` | Showed an error dialog and returned a document with `<parsererror>` | Returns `null` for invalid XML |
| `App.getQueryStringArgs()` | Included a stray `"undefined"` key | Only real values |

New functions: `App.readFile`, `App.writeFile`, `App.listFiles`, `App.getSettings`, `App.saveSettings`.

## Example: saving settings

Arctic Eagle (the Settings app):

```javascript
var settings = App.openFile('../../../system/settings/' + App.getUsername() + '.xml');
settings = App.parseXml(settings);
settings.getElementsByTagName('background')[0].setAttribute('src', path);
App.saveFile('system/settings/' + App.getUsername() + '.xml', App.serializeXml(settings));
App.setBackground(path);
```

site03:

```javascript
App.setBackground(path);
```

## Example: a confirmation dialog

Arctic Eagle:

```javascript
App.openDialog('Delete?', "parent.System.postNotification('Deleted', 'Done')");
```

site03:

```javascript
App.openDialog('Delete?', function () {
	App.postNotification('Deleted', 'Done');
});
```

## Things that no longer work

* Calling `parent.System`. The desktop object is now called `Desktop`, and reaching into it from an app isn't supported; send a message through `App` instead.
* Reading server data files over HTTP (`system/settings/*.xml`, `system/LoggedInUsers.xml`): they no longer exist. Use `App.getSettings()`.
* The Save File dialog (`system/components/save dialog/`) was removed.
