# Skylight OS as a CouchDB 2.3.1 couchapp

Skylight OS running inside Apache CouchDB 2.3.1, with no other server. The pages are the browser version's ([`../site3`](../site3)); CouchDB serves them as attachments of a design document, logs users in through `/_session`, and keeps each user's settings and files in their own database. Documentation: [`../wiki9couchdb`](../wiki9couchdb/README.md).

## Quick start

With CouchDB 2.3.1 installed and set up as a single node (in Fauxton, <http://127.0.0.1:5984/_utils/>):

```
tools/push.sh http://couchadmin:password@127.0.0.1:5984
tools/add-user.sh http://couchadmin:password@127.0.0.1:5984 alice choose-a-password
```

On Windows, use `powershell -ExecutionPolicy Bypass -File tools\push.ps1 ...` and `tools\add-user.ps1` the same way. Then open <http://127.0.0.1:5984/skylight/_design/skylight/_rewrite/> and log in as `alice`.

## Layout

```
site9couchdb/
├── attachments/     the pages: uploaded as attachments of _design/skylight
│   ├── index.html
│   ├── apps/        terminal/, settings/, web-browser/
│   └── system/      js/ (core.js talks to CouchDB), css/, dialog/, images/, apps.json
├── design/          validate_doc_update.js and rewrites.json for _design/skylight
├── userdb/          validate_doc_update.js for each user's database
├── users/           validate_doc_update.js for _users (no sign-ups, no attachments)
└── tools/           push and add-user, as bash (.sh) and PowerShell (.ps1) scripts
```
