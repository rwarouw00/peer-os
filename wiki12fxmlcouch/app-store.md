# The app store

In the [FXML version](../wiki11fxml/README.md) an app is already just files, read when the app opens. This version reads those same files from **CouchDB** instead of from `apps/`, so one store can serve every desktop:

* **One copy of each app.** Install or update an app once, in the store, and every desktop that uses the store has it.
* **Updates without restarting.** An app's FXML and scripts are fetched each time it opens, so a new version reaches users the next time they open the app.
* **Nothing to compile or install on the desktops.** Skylight itself (`skylight.jar`, `ui/`, `images/`) is installed once; apps come and go in the store.
* **CouchDB's own features come free**: every document has a revision, the store can be replicated to another server or a laptop, and reads need no login while writes are limited to administrators.

Users' data doesn't move: settings and files stay in `data/` on each computer, as before.

## What's in the store

The store is one CouchDB database (the examples call it `skylight-apps`). Each app is **one document**, named after its folder, with each of its files as an **attachment**:

```
skylight-apps
├── _design/store            validate_doc_update (the store's one rule)
│   └── apps.txt             the home menu: apps/terminal/, apps/settings/, ...
├── terminal                 { "type": "app", "title": "Terminal" }
│   ├── app.properties
│   ├── app.fxml
│   ├── terminal.js
│   └── icons/16.png, icons/32.png, icons/48.png, icons/64.png
├── settings
├── web-browser
└── notes
```

CouchDB serves each attachment at its own address, so the store looks like a folder of files to anything that reads URLs:

```
http://127.0.0.1:5984/skylight-apps/terminal/app.fxml
http://127.0.0.1:5984/skylight-apps/terminal/icons/48.png
http://127.0.0.1:5984/skylight-apps/_design/store/apps.txt
```

## How Skylight uses it

`skylight.properties`, beside `skylight.jar`, names the store:

```properties
appStore=http://127.0.0.1:5984/skylight-apps/
```

(or start Java with `-Dskylight.appStore=...`, which wins). Skylight then turns every app file it needs into a URL in the store, in one method, `Storage.appResource`:

| Skylight needs | From the store | Without a store |
| --- | --- | --- |
| The home menu's list | `<store>_design/store/apps.txt` | `apps/apps.txt` |
| An app's manifest | `<store>notes/app.properties` | `apps/notes/app.properties` |
| Its window | `<store>notes/app.fxml` | `apps/notes/app.fxml` |
| Its icons | `<store>notes/icons/48.png` | `apps/notes/icons/48.png` |
| A Java app's classes | `<store>jnotes/app.jar` | `apps/jnotes/app.jar` |

The rest comes free from Java and JavaFX, which work with URLs rather than files:

* **`FXMLLoader`** reads `app.fxml` over HTTP, and resolves `<fx:script source="notes.js"/>` and `@icons/48.png` against the FXML's own address, so they're fetched from the store too. App files don't change at all between a folder and the store.
* **`Image`** loads icons from the store.
* **`URLClassLoader`** loads a Java app's `app.jar` from the store, so `class=` apps work too.

App paths stay as they were: an app is still `apps/notes/` to `openWindow`, shortcuts and settings, so users' saved shortcuts work with or without a store.

```mermaid
sequenceDiagram
    participant U as User
    participant D as Skylight (desktop)
    participant C as CouchDB (skylight-apps)
    D->>C: GET _design/store/apps.txt (at login)
    D->>C: GET terminal/app.properties, icons/48.png, ... (each app in the menu)
    U->>D: opens Notes
    D->>C: GET notes/app.fxml
    D->>C: GET notes/notes.js (fx:script source=)
    D->>U: the Notes window
    U->>D: Save
    D->>D: writes data/users/admin/files/notes.txt (local)
```

## Filling the store

`tools/push-apps.sh` (Linux, macOS) and `tools/push-apps.ps1` (Windows PowerShell) upload the folders in `apps/`:

```
tools/push-apps.sh http://admin:password@127.0.0.1:5984/skylight-apps
powershell -ExecutionPolicy Bypass -File tools\push-apps.ps1 http://admin:password@127.0.0.1:5984/skylight-apps
```

They:

1. check that the account is a CouchDB administrator, and create the database if needed;
2. write `_design/store`, with the rule from [`store/validate_doc_update.js`](../site12fxmlcouch/store/validate_doc_update.js) and `apps/apps.txt` attached;
3. write one document per folder in `apps/`: `type` and `title` (from `app.properties`), and every file in the folder as an attachment (except `src/` and `classes/`, a Java app's source and build output).

Running them again updates everything: each document is replaced with a new revision. To update only some apps, name them:

```
tools/push-apps.sh http://admin:password@127.0.0.1:5984/skylight-apps notes
```

The bash version needs `bash`, `curl`, `base64`, `sed` and `awk`; the PowerShell one was tested with PowerShell 7 and written to work with Windows PowerShell 5.1 as well (not tested there). Both send each app as one JSON document with its files base64-encoded inside it, so an app is replaced all at once: a desktop never sees half an update.

## Changing an app

1. Edit the files in `apps/notes/` and try them locally: with `appStore=` empty (or `-Dskylight.appStore=`), Skylight uses the `apps/` folder, as site11fxml does.
2. Push the app: `tools/push-apps.sh http://... notes`.
3. The next time anyone opens Notes, they get the new version. Changes to `app.properties` (title, size, icons) and to the home menu show at the next login, when the manifests and `apps.txt` are read.

The store keeps a revision number for each app, but CouchDB only keeps the latest version's attachments after compaction, so keep apps' sources in version control as well (they're in `apps/`).

## Copies of the store

CouchDB replication copies a whole store to another database, on the same server or another one:

```
curl -X POST http://admin:password@127.0.0.1:5984/_replicate \
     -H 'Content-Type: application/json' \
     -d '{"source": "skylight-apps", "target": "skylight-apps-replica", "create_target": true}'
```

Uses:

* **A test store.** Push new versions to `skylight-apps-test`, try them with `-Dskylight.appStore=http://.../skylight-apps-test/`, then replicate to the real store.
* **A local copy for a laptop or a branch office.** A CouchDB on the laptop with a copy of the store keeps apps working with no network. Replicate again (or set up continuous replication) to pick up updates.

## Who can change the store

The store's one rule, in `_design/store`, refuses any change from a user who isn't a CouchDB administrator:

```javascript
function (newDoc, oldDoc, userCtx, secObj) {
	if (userCtx.roles.indexOf('_admin') < 0) {
		throw({ unauthorized: 'Only CouchDB administrators can change the app store.' });
	}
}
```

Reading needs no login, because Skylight downloads apps without one. Apps are programs that run with the full power of Java on every desktop that uses the store (see [Security](security.md)), so writing to the store is the same as installing software on all of them.

## When the store can't be reached

* At login, the home menu is empty if `apps.txt` can't be read, and apps whose manifests can't be read are left out of it and off the desktop.
* Opening an app from a shortcut or `openWindow` gives a "Cannot open app" notification saying the app isn't in the store or the store can't be reached.
* Apps already open keep working: their files were read when they opened. Users' files and settings are local, so nothing is lost.

Skylight doesn't keep its own copy of the apps; for working offline, replicate the store to a CouchDB on the same computer (above).

## Without CouchDB

Any web server can serve a store, since Skylight only reads URLs: a folder of app folders, with `apps.txt` at `_design/store/apps.txt`, served by nginx, Apache or `python3 -m http.server`, works too (tested with `python3 -m http.server`, including a Java app's `app.jar`). CouchDB adds what a plain web server doesn't: one document per app updated all at once, revisions, the write rule, and replication.
