# Skylight OS as a CouchDB app store

The [CouchDB 2.3.1 couchapp](../site09couchdb) refactored so that each app is its own document in CouchDB, with its `app.json` as the document's `manifest` and its files as attachments. The desktop stays in the design document `_design/skylight`; rewrite rules make the app documents look like the old `apps/<name>/` folders, and a view builds the home menu from the apps' `menu` numbers. Documentation: [`../wiki14couchsky`](../wiki14couchsky/README.md).

## Quick start

With CouchDB 2.3.1 installed and set up as a single node (in Fauxton, <http://127.0.0.1:5984/_utils/>):

```
tools/push.sh http://couchadmin:password@127.0.0.1:5984
tools/add-user.sh http://couchadmin:password@127.0.0.1:5984 alice choose-a-password
```

Then open <http://127.0.0.1:5984/skylight/_design/skylight/_rewrite/> and log in as `alice`. To install or update one app:

```
tools/push-app.sh http://couchadmin:password@127.0.0.1:5984 notes
```

On Windows, use `powershell -ExecutionPolicy Bypass -File tools\push.ps1 ...`, `tools\push-app.ps1` and `tools\add-user.ps1` the same way.

## Layout

```
site14couchsky/
├── attachments/     the desktop: uploaded as attachments of _design/skylight
│   ├── index.html
│   └── system/      js/ (core.js talks to CouchDB), css/, dialog/, images/
├── apps/            terminal/, settings/, web-browser/: each pushed as its own document
├── design/          validate_doc_update.js, rewrites.json and views/apps/map.js for _design/skylight
├── userdb/          validate_doc_update.js for each user's database
├── users/           validate_doc_update.js for _users (no sign-ups, no attachments)
└── tools/           push, push-app and add-user, as bash (.sh) and PowerShell (.ps1) scripts
```
