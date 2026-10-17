## App.addShortcut

Adds a shortcut for an app to the desktop. It accepts one argument: the app's folder **relative to the site root**, ending in `/`, the same form [App.openWindow](App.openWindow.md) uses.

```
App.addShortcut('apps/default/web browser/');
```

The shortcut shows the app's `Xlarge` icon with its title underneath (see [appData.json](appData.json.md)). Double-clicking it opens the app. Shortcuts can be dragged around the desktop.

![A desktop shortcut for the Web Browser](images/siteroot/shortcut.png)

The desktop also sends the shortcut to the server script `system/server/shortcuts.php` (see [Server Endpoints](Server-Endpoints.md)). If the script doesn't reply `NO ERROR`, a dialog reports the problem.

Limitations in `siteroot`:

* Shortcuts aren't loaded again at login, so they disappear when the page is reloaded.
* Every shortcut is placed at the same spot, so a second shortcut covers the first until it is dragged away.
* Two apps with the same title can't both have a shortcut, because the shortcut's element id is built from the title.
* Releasing the mouse over a shortcut (a click, or the end of a drag) sends broken data to `shortcuts.php`; if the server rejects it, an error dialog appears. See [Known Issues](Known-Issues.md).

This function isn't mentioned on the original wiki.

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
