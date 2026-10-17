## App.getQueryStringArgs

This function returns an object containing the query string that is passed to the app.  It accepts no arguments.

```
var qs = App.getQueryStringArgs();
```

> **In `siteroot`:** the result is an **object** keyed by name (the original page said "array"). Names and values are URL-decoded. A window opened by the desktop always gets `app-id` and `username`, so a freshly opened Terminal returns:
>
> ```
> { "app-id": "0.8566454929483042", "username": "admin", "undefined": "undefined" }
> ```
>
> The `"undefined"` entry is a side effect of a bug in `System.openWindow`, which appends its unused `args` parameter to the URL. See [Known Issues](Known-Issues.md).

---

_Source: [App.getQueryStringArgs](https://sourceforge.net/p/skylightos/wiki/App.getQueryStringArgs/) on the SourceForge wiki · Labels: app object · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
