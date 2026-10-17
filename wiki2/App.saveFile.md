## App.saveFile

This function saves a file to the server.  NOTE: This should only be used after calling the [save file dialog](Save-File-Dialog.md).  It accepts two arguments.

```
App.saveFile('users/admin/Documents/test.txt', 'this is the contents of the file.');
```

> **In `siteroot`:** `saveFile` sends the file to the server script `system/server/saveFile.php` and returns whatever text the script replies with. That script **isn't included in `siteroot`**, so saving only works on a server that provides it (see [Server Endpoints](Server-Endpoints.md)).
>
> * The request sends three form fields: `file`, `contents`, and `user` (from [App.getUsername](App.getUsername.md)).
> * The Settings app passes a path **relative to the site root**, for example `'system/settings/admin.xml'`. Note that this differs from [App.openFile](App.openFile.md), whose paths are relative to your app.
> * The script's address is hard-coded as `../../../system/server/saveFile.php`, so it only works for apps three folders below the site root, such as `apps/default/myapp/`.
> * The values are **not encoded**. Contents containing `&`, `+` or `%` will be cut off or changed. Encode the contents yourself:
>
> ```
> App.saveFile('system/notes/' + App.getUsername() + '.txt', encodeURIComponent(text));
> ```
>
> The save file dialog exists in `siteroot` but can't hand a file name back to your app yet.

---

_Source: [App.saveFile](https://sourceforge.net/p/skylightos/wiki/App.saveFile/) on the SourceForge wiki · Labels: file I/O, app object · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
