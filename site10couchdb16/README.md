# Skylight OS as a CouchDB 1.6 couchapp

Skylight OS running inside Apache CouchDB 1.6 (1.6.1), with no other server. It's the [CouchDB 2.3.1 version](../site09couchdb) adapted to 1.6: the same pages (the browser version's, from [`../site03`](../site03)), documents and checks, with tools that create each user's database themselves (1.6 has no couch_peruser) and a `_users` check that blocks CouchDB 1.6.1's admin-escalation bug, CVE-2017-12635. Documentation: [`../wiki10couchdb16`](../wiki10couchdb16/README.md).

## Quick start

With CouchDB 1.6 installed and an administrator created (in Futon, <http://127.0.0.1:5984/_utils/>, with **Fix this**; until then everyone is an administrator):

```
tools/push.sh http://couchadmin:password@127.0.0.1:5984
tools/add-user.sh http://couchadmin:password@127.0.0.1:5984 alice choose-a-password
```

On Windows, use `powershell -ExecutionPolicy Bypass -File tools\push.ps1 ...` and `tools\add-user.ps1` the same way. Then open <http://127.0.0.1:5984/skylight/_design/skylight/_rewrite/> and log in as `alice`.

## Layout

```
site09couchdb/
├── attachments/     the pages: uploaded as attachments of _design/skylight
│   ├── index.html
│   ├── apps/        terminal/, settings/, web-browser/
│   └── system/      js/ (core.js talks to CouchDB), css/, dialog/, images/, apps.json
├── design/          validate_doc_update.js and rewrites.json for _design/skylight
├── userdb/          validate_doc_update.js for each user's database
├── users/           validate_doc_update.js for _users (only administrators may write)
└── tools/           push and add-user, as bash (.sh) and PowerShell (.ps1) scripts
```
