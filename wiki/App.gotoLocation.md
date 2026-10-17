## App.gotoLocation

This function moves the location of the window.  It operates the same way as location.assign, but it preserves the query string as well.  NOTE: This <b>MUST</b> be used instead of location.assign, even if there is no query string.  Skylight OS creates a default query string with special information it must pass to the app.  Your app will not work correctly if you use location.assign.

```
App.gotoLocation(path, queryString);
```

---

_Source: [App.gotoLocation](https://sourceforge.net/p/skylightos/wiki/App.gotoLocation/) on the SourceForge wiki · Labels: app object · Last modified 2014-01-22_
