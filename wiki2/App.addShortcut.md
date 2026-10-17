## App.addShortcut

Adds a shortcut for an app to the desktop. It accepts one argument: the app's folder **relative to the site root**, ending in `/`, the same form [App.openWindow](App.openWindow.md) uses.

```
App.addShortcut('apps/default/web browser/');
```

The shortcut shows the app's `Xlarge` icon with its title underneath (see [appData.json](appData.json.md)). Double-clicking it opens the app. Shortcuts can be dragged around the desktop.

![A desktop shortcut for the Web Browser](images/siteroot/shortcut.png)

The desktop also sends the shortcut to the server script `system/server/shortcuts.php`, which appends it to `system/server/shortcuts.xml` (see [Server Endpoints](Server-Endpoints.md#shortcutsphp)). If the script doesn't reply `NO ERROR`, a dialog reports the problem.

Limitations in `siteroot`:

* The server saves shortcuts, but the desktop never loads them again, so they disappear when the page is reloaded. The saved list is shared by all users.
* Every shortcut is placed at the same spot, so a second shortcut covers the first until it is dragged away.
* Two apps with the same title can't both have a shortcut, because the shortcut's element id is built from the title.
* Clicking a shortcut (or finishing a drag) sends broken data to `shortcuts.php`, so an error dialog, "There was an error creating the shortcut", appears every time (tested). See [Known Issues](Known-Issues.md).

This function isn't mentioned on the original wiki.

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._

_Updated after the `system/server` scripts were added to `siteroot`._
