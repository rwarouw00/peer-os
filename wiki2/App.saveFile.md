## App.saveFile

This function saves a file to the server.  NOTE: This should only be used after calling the [save file dialog](Save-File-Dialog.md).  It accepts two arguments.

```
App.saveFile('users/admin/Documents/test.txt', 'this is the contents of the file.');
```

> **In `siteroot`:** `saveFile` sends the file to the server script [`system/server/saveFile.php`](Server-Endpoints.md#savefilephp) and returns its reply: `{"error": "none"}` on success, or `{"error": "Operation not performed by a logged in user"}` if the login cookie doesn't match. Check the reply; it is the only way to know the file was saved.
>
> ```
> var reply = JSON.parse(App.saveFile(path, encodeURIComponent(text)));
> if (reply.error != 'none') { App.openDialog('Could not save: ' + reply.error); }
> ```
>
> Saving only works when the browser holds the login cookie from `auth.php`. On a site with a port number in its address (such as `http://localhost:8000`) the browser rejects that cookie, so every save is refused; see [Known Issues](Known-Issues.md#server).
>
> * The request sends three form fields: `file`, `contents`, and `user` (from [App.getUsername](App.getUsername.md)).
> * The path is **relative to the site root**, for example `'system/settings/admin.xml'` (the script writes to `../../` + `file`). Note that this differs from [App.openFile](App.openFile.md), whose paths are relative to your app. Folders must already exist.
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

_Updated after the `system/server` scripts were added to `siteroot`._
