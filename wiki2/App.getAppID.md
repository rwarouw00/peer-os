## App.getAppID

This function returns the [AppID](AppID.md) of a window.  It accepts no arguments;

```
var AppID = App.getAppID();
```

> **In `siteroot`:** the AppID is read from the `app-id` value in the page's query string, so it is returned as a **string** (for example `"0.8566454929483042"`), not a number. If your app navigates to another page with `location.assign` instead of [App.gotoLocation](App.gotoLocation.md), the query string is lost and this returns `undefined`.

---

_Source: [App.getAppID](https://sourceforge.net/p/skylightos/wiki/App.getAppID/) on the SourceForge wiki · Labels: AppID, app object · Last modified 2014-08-13_

_Updated after reviewing the `siteroot` code._
