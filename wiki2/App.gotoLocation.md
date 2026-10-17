## App.gotoLocation

This function moves the location of the window.  It operates the same way as location.assign, but it preserves the query string as well.  NOTE: This <b>MUST</b> be used instead of location.assign, even if there is no query string.  Skylight OS creates a default query string with special information it must pass to the app.  Your app will not work correctly if you use location.assign.

```
App.gotoLocation(path, queryString);
```

> **In `siteroot`:** the new address is built as `path + location.search + '&' + queryString`.
>
> * `path` is relative to the current page, like a normal link.
> * The current query string (including `app-id` and `username`) is kept, and `queryString` is added to the end. Arguments are never removed, so navigating repeatedly makes the address longer each time.
> * If you leave out `queryString`, the text `&undefined` is added. Pass an empty string if you have nothing to add.
>
> The [Save File Dialog](Save-File-Dialog.md) uses it to move between folders: `App.gotoLocation('index.php', 'path=../../../apps/')`.
>
> The Settings app does **not** follow this rule: its two pages switch with `location.assign`, so after visiting "Add User" the page loses its AppID and username.

---

_Source: [App.gotoLocation](https://sourceforge.net/p/skylightos/wiki/App.gotoLocation/) on the SourceForge wiki · Labels: app object · Last modified 2014-01-22_

_Updated after reviewing the `siteroot` code._
