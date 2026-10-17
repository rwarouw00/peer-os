## App.openWindow

Opens a [window](window.md).  Accepts a path.  This should be something like 'apps/default/terminal/', or for packaged apps, 'apps/default/terminal.sapp'.

NOTE: Apps can only open windows if they have requested the permission [OPEN_WINDOWS](Permissions.md).

```
App.openWindow(path);
```

> **In `siteroot`:**
>
> * `path` is a folder **relative to the site root** (not to your app), and it **must end with `/`**. The desktop loads `path + 'appData.json'` for the window settings and `path` itself as the window content, so the folder needs an `index.html` (or other default page).
> * Packaged `.sapp` apps aren't supported.
> * There are no permissions, so any app can open windows.
> * Nothing is returned. The desktop creates a new [AppID](AppID.md) for the window, but your app isn't told what it is.
> * Each call opens another window, even if the app is already open.
>
> ```
> App.openWindow('apps/default/web browser/');
> ```

---

_Source: [App.openWindow](https://sourceforge.net/p/skylightos/wiki/App.openWindow/) on the SourceForge wiki · Labels: window, app object · Last modified 2014-03-13_

_Updated after reviewing the `siteroot` code._
