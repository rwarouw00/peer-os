# The app store

In [site9couchdb](../wiki9couchdb/README.md), Skylight and all its apps were one design document: changing any app meant pushing everything again, and the home menu was a file, `apps.json`, edited by hand. Here, **each app is its own document** in the `skylight` database, and CouchDB itself works out the home menu:

* **Install or update one app** with `tools/push-app.sh <address> <app>`, without touching the desktop or the other apps.
* **No list to keep up to date.** An app's `app.json` says where it goes in the home menu (`"menu": 4`), and a CouchDB view collects them.
* **Everyone gets it at once.** All users load apps from the same database, so a pushed app is in everyone's menu at their next login, and an update shows the next time they open the app.
* **CouchDB's tools work on apps**: each app has a revision number, Fauxton shows and edits it, and replication copies Skylight and its apps to another CouchDB in one go.

## An app's document

`tools/push-app.sh` turns the folder `apps/notes/` into the document `notes`:

```json
{
	"_id": "notes",
	"_rev": "3-35a75592f5ac063dda2ede634d691145",
	"type": "app",
	"manifest": { "title": "Notes", "menu": 4, "position": "centered", "width": 420, "height": 320, "icons": { ... } },
	"_attachments": {
		"index.html":   { "content_type": "text/html; charset=utf-8", "length": 517, ... },
		"notes.js":     { "content_type": "application/javascript; charset=utf-8", ... },
		"style.css":    { "content_type": "text/css; charset=utf-8", ... },
		"icons/16.png": { "content_type": "image/png", ... },
		...
	}
}
```

* `manifest` is the folder's `app.json`, as JSON, so CouchDB's view can read it (views can't read attachments).
* Every other file is an attachment, under its path in the folder.

![The notes document in Fauxton](images/fauxton-app.png)

The `skylight` database then holds the desktop and the apps side by side:

![The skylight database in Fauxton](images/fauxton-apps.png)

## Addresses

The app pages haven't changed since site3: they load `../../system/js/core.js`, and the desktop opens `apps/notes/index.html` and reads `apps/notes/app.json`. The design document's rewrite rules keep all those addresses working, so the pages don't know their files now live in different documents:

| Address under `.../_design/skylight/_rewrite/` | Rule | Served from |
| --- | --- | --- |
| (nothing) | `"" → index.html` | The desktop page, an attachment of `_design/skylight` |
| `system/apps.json` | `system/apps.json → _view/apps` | The `apps` view: the home menu |
| `apps/notes/app.json` | `apps/:app/app.json → ../../:app` | The whole `notes` document; the desktop takes its `manifest` |
| `apps/notes/index.html`, `apps/notes/icons/48.png` | `apps/:app/* → ../../:app/*` | Attachments of the `notes` document |
| `system/js/core.js`, `system/dialog/index.html`, ... | `* → *` | Attachments of `_design/skylight` |

Rules are tried in order, so the specific ones come first. `../../` goes from the design document up to the database, where the app documents are.

## The home menu

`design/views/apps/map.js` is a CouchDB view:

```javascript
function (doc) {
	if (doc.type === 'app' && doc.manifest && typeof doc.manifest.menu === 'number') {
		emit(doc.manifest.menu, doc.manifest.title);
	}
}
```

CouchDB runs it over every document as it's saved and keeps the result sorted by key, so `GET .../_rewrite/system/apps.json` returns the menu, in order:

```json
{"total_rows":4,"offset":0,"rows":[
{"id":"terminal","key":1,"value":"Terminal"},
{"id":"settings","key":2,"value":"Settings"},
{"id":"web-browser","key":3,"value":"Web Browser"},
{"id":"notes","key":4,"value":"Notes"}
]}
```

An app without a `menu` number is installed but not in the menu: it opens by path, with `App.openWindow('apps/<name>/')` or a shortcut. Two apps with the same number are listed in name order.

## Pushing apps

| Tool | Does |
| --- | --- |
| `tools/push.sh <address>` | The whole of Skylight: settings, `_users` check, the design document, then every app in `apps/` |
| `tools/push-app.sh <address> <app> [app ...]` | Only the apps named: one document each, created or replaced |

The PowerShell versions, `tools\push.ps1` and `tools\push-app.ps1`, take the same arguments. `<address>` is a CouchDB administrator's, such as `http://couchadmin:password@127.0.0.1:5984`.

`push-app` reads the document's current revision and sends the new version with it in one request, so the app is replaced all at once: a user never gets a mix of old and new files. CouchDB answers with a conflict if someone else changed the app in between; run it again.

## Removing an app

Delete its document, in Fauxton or with curl (the revision is in Fauxton, or in `GET /skylight/<app>`):

```
curl -X DELETE 'http://couchadmin:password@127.0.0.1:5984/skylight/notes?rev=3-35a7...'
```

It leaves the menu at the next login. Desktop shortcuts to it stay in users' settings, but are no longer shown, because their app can't be read. To keep an app but hide it from the menu, remove its `menu` number and push it again.

## Who can change apps

The `skylight` database's `validate_doc_update` function refuses every change from anyone who isn't a CouchDB administrator, as in site9couchdb, and now also checks app documents, even from administrators:

* the name (`_id`) may use only lowercase letters, digits and `-`, so it works as a folder name in `apps/<name>/`;
* `type` must be `"app"`, and `manifest` must have a `title`.

Everyone may read the database, because the login page loads before anyone has logged in. Apps are pages that run with the logged-in user's rights in CouchDB (see [Security](security.md)), so pushing an app is installing software for every user.

## Copies

Replicating the `skylight` database copies the desktop and all the apps together; the rewrite rules only use addresses inside the database, so the copy works at its own address:

```
curl -X POST http://couchadmin:password@127.0.0.1:5984/_replicate -H 'Content-Type: application/json' \
     -d '{"source": "http://couchadmin:password@127.0.0.1:5984/skylight", "target": "http://couchadmin:password@127.0.0.1:5984/skylight-copy", "create_target": true}'
```

`.../skylight-copy/_design/skylight/_rewrite/` is then a second Skylight with the same apps (and the same users, since they're in `_users`). Use a copy to try out new apps before pushing them to the real one, or replicate to another server.
