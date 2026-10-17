## App Object

The App object is the main object whereby an app interacts with Skylight OS and the server filesystem.  Many of these functions are affected by [App Permissions](Permissions.md)

* [App.openDialog](App.openDialog.md) Opens a Dialog
* [App.openWindow](App.openWindow.md) Opens a window
* [App.closeWindow](App.closeWindow.md) Closes the current window
* [App.postNotification](App.postNotification.md) Sends a notification to the user
* [App.getQueryStringArgs](App.getQueryStringArgs.md) Gets the query string arguments and returns them as an array
* [App.getAppID](App.getAppID.md) Gets the app id
* [App.getUsername](App.getUsername.md) Retrieves the username of the current user
* [App.openFile](App.openFile.md) Opens a file
* [App.saveFile](App.saveFile.md) Saves a file
* [App.parseXml](App.parseXml.md) Parses an XML string into an DOMDocument
* [App.serializeXml](App.serializeXml.md) Serializes an DOMDocument into an XML string
* [App.gotoLocation](App.gotoLocation.md) Moves the window location, while preserving the query string.  This is required to be used instead of location.assign.
* [App.registerError](App.registerError.md) Registers a System Error.  This needs only to be used when a serious error occurs.
* [App.speak](App.speak.md) Allows apps to use Google's Text-to-Speech service.
* [App.setBackground](https://sourceforge.net/p/skylightos/wiki/App.setBackground/) Sets the desktop background.
* [App.selectFileForOpen](https://sourceforge.net/p/skylightos/wiki/App.selectFileForOpen/) Opens the [Open File Dialog](Open-File-Dialog.md) and returns the selected value.
* [App.loadTheme](https://sourceforge.net/p/skylightos/wiki/App.loadTheme/) Loads a system theme.

---

_Source: [App Object](https://sourceforge.net/p/skylightos/wiki/App%20Object/) on the SourceForge wiki · Labels: app object · Last modified 2014-08-26_
