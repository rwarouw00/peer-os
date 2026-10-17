## App.getUsername

App.getUsername returns the username of the current user.  It accepts no arguments.

```
var user = App.getUsername();
```

> **In `siteroot`:** the username comes from the `username` value in the page's query string, which the desktop adds when it opens the window. It isn't checked by anything, so don't rely on it for security: a page opened with a different query string will report a different user.

---

_Source: [App.getUsername](https://sourceforge.net/p/skylightos/wiki/App.getUsername/) on the SourceForge wiki · Labels: app object · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
