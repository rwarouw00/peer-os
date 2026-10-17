## App Object

The App object is the main object whereby an app interacts with Skylight OS and the server filesystem.  Many of these functions are affected by [App Permissions](Permissions.md)

To use it, load `system/js/app.js` in your app's page (see [How to build a Skylight OS app](How-to-build-a-Skylight-OS-app.md)). For how the calls reach the desktop, see [Architecture](Architecture.md).

> **In `siteroot`:** there are no permissions; every app can call every function. The table below shows which functions exist in the code.

| Function | Description | In `siteroot` |
| --- | --- | --- |
| [App.openDialog](App.openDialog.md) | Opens a [Dialog](Dialogs.md) | yes |
| [App.openWindow](App.openWindow.md) | Opens a window | yes |
| [App.closeWindow](App.closeWindow.md) | Closes a window | yes |
| [App.postNotification](App.postNotification.md) | Sends a notification to the user | yes |
| [App.getQueryStringArgs](App.getQueryStringArgs.md) | Gets the query string arguments and returns them as an object | yes |
| [App.getAppID](App.getAppID.md) | Gets the app id | yes |
| [App.getUsername](App.getUsername.md) | Retrieves the username of the current user | yes |
| [App.openFile](App.openFile.md) | Opens a file | yes |
| [App.saveFile](App.saveFile.md) | Saves a file | yes (needs the server's `saveFile.php`) |
| [App.parseXml](App.parseXml.md) | Parses an XML string into a DOMDocument | yes |
| [App.serializeXml](App.serializeXml.md) | Serializes a DOMDocument into an XML string | yes |
| [App.gotoLocation](App.gotoLocation.md) | Moves the window location, while preserving the query string.  This is required to be used instead of location.assign. | yes |
| [App.setBackground](App.setBackground.md) | Sets the desktop background. | yes |
| [App.addShortcut](App.addShortcut.md) | Adds a shortcut icon for an app to the desktop. | yes (not on the original wiki) |
| [App.registerError](App.registerError.md) | Registers a System Error.  This needs only to be used when a serious error occurs. | no |
| [App.speak](App.speak.md) | Allows apps to use Google's Text-to-Speech service. | no |
| [App.selectFileForOpen](https://sourceforge.net/p/skylightos/wiki/App.selectFileForOpen/) | Opens the [Open File Dialog](Open-File-Dialog.md) and returns the selected value. | no |
| [App.loadTheme](https://sourceforge.net/p/skylightos/wiki/App.loadTheme/) | Loads a system theme. | no |

The links for App.selectFileForOpen and App.loadTheme point at SourceForge pages that were never written.

> **In `siteroot`:** two other copies of the App object exist, in `apps/default/terminal/js/app.js` and `system/components/dialog/js/app.js`. Neither is loaded by anything, and the Terminal copy has a different `postNotification` that takes a JSON string. Always load `system/js/app.js`.

---

_Source: [App Object](https://sourceforge.net/p/skylightos/wiki/App%20Object/) on the SourceForge wiki · Labels: app object · Last modified 2014-08-26_

_Updated after reviewing the `siteroot` code._
