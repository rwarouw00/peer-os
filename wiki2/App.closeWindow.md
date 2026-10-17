## App.closeWindow

App.closeWindow is used to close an app's window.  You can only close your own window.  Even so, you still need to supply it with your current [AppID](AppID.md).

```
App.closeWindow(App.getAppID());
```

> **In `siteroot`:** any window can be closed, not just your own. The desktop removes whichever window has the AppID you pass, along with its taskbar icon, and doesn't check who asked. (The parameter is called `title` in [`app.js`](../siteroot/system/js/app.js), but it is used as an AppID.) Passing an AppID that doesn't exist causes a JavaScript error on the desktop.
>
> The [Dialog](Dialogs.md) uses this to close itself when OK or Cancel is clicked.

---

_Source: [App.closeWindow](https://sourceforge.net/p/skylightos/wiki/App.closeWindow/) on the SourceForge wiki · Labels: window, app object · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
