## App.openFile

This function opens a file on the Skylight OS server, and returns it's content.  NOTE: The file to be opened should be determined by using the [open file dialog](Open-File-Dialog.md).

```
var file = App.openFile('users/admin/Documents/test.txt');
```

> **In `siteroot`:** `openFile` downloads the file with a plain HTTP GET request and returns its text. Some details matter:
>
> * **The path is relative to your app's page**, like a link, not to the site root. An app in `apps/default/myapp/` that wants `system/settings/admin.xml` must ask for `'../../../system/settings/admin.xml'`. The example above would look for `apps/default/myapp/users/admin/Documents/test.txt`.
> * Only files the web server publishes can be opened, and there is no permission check beyond that.
> * The request is synchronous, so the app freezes until the file arrives.
> * Errors aren't reported: a missing file returns the server's error page text.
> * The open file dialog doesn't exist in `siteroot`.
>
> The Settings app reads the current user's settings like this:
>
> ```
> var settings = App.openFile('../../../system/settings/' + App.getUsername() + '.xml');
> settings = App.parseXml(settings);
> ```

---

_Source: [App.openFile](https://sourceforge.net/p/skylightos/wiki/App.openFile/) on the SourceForge wiki · Labels: file I/O, app object · Last modified 2014-01-22_

_Updated after reviewing the `siteroot` code._
