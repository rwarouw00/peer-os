# CouchDB primer

A short guide to the CouchDB 2.3 features Skylight uses. CouchDB's own documentation (version 2.3) covers the rest; its admin pages, Fauxton, at `/_utils/`, show every database and document described here.

## Documents and revisions

CouchDB stores **JSON documents** in **databases**, and everything is reached over HTTP:

```
GET    /userdb-616c696365/settings          read a document
PUT    /userdb-616c696365/settings          create it, or replace it (with its current _rev)
DELETE /userdb-616c696365/settings?rev=...  delete it
GET    /userdb-616c696365/_all_docs         list the documents
```

Every document has an `_id` and a revision, `_rev`. To replace a document you must send the revision you read; if it changed in the meantime, CouchDB answers **409 Conflict** instead of losing someone's change. Skylight reads, then writes, and tries again on a conflict.

`_all_docs` lists documents in `_id` order, so `startkey="file:"` and `endkey="file:￰"` pick out every `file:...` document: that's `App.listFiles()`.

## Design documents and attachments

A **design document** is a document whose `_id` starts with `_design/`. It holds code and settings for its database, and, like any document, it can have **attachments**: files stored with it and served as they are:

```
GET /skylight/_design/skylight/system/js/core.js
```

That's what makes a *couchapp*: the whole web application lives in one design document, and CouchDB serves it. `tools/push.sh` builds the document from the files under `attachments/` (sending each one base64-encoded in `_attachments`) and uploads it in one request.

## Rewrites

A design document's `rewrites` field maps addresses under `.../_design/<name>/_rewrite/` to other paths. Skylight's rules show `index.html` for the bare address and pass everything else through:

```json
[
	{ "from": "", "to": "index.html" },
	{ "from": "*", "to": "*" }
]
```

CouchDB serves attachments by name only: it has no "folder index" like a web server. So the desktop opens `apps/terminal/index.html`, not `apps/terminal/`.

## Users and sessions

User accounts are documents in the **`_users`** database, named `org.couchdb.user:<name>`, with `"type": "user"`. You give CouchDB the password in a `password` field; it stores a salted hash instead (`password_scheme`, `derived_key`, `salt`, `iterations`) and never keeps the password.

`POST /_session` with a name and password logs in and sets a cookie, `AuthSession`. The browser sends it with every later request, and `GET /_session` says who's logged in (`userCtx.name`). `DELETE /_session` logs out. The cookie stops working after 10 minutes without a request, by default.

**Server administrators** are listed in CouchDB's configuration (`[admins]`), not in `_users`. They can do anything.

## Per-user databases

With **couch_peruser** turned on (`[couch_peruser] enable = true`), CouchDB creates a database for each new user, named `userdb-` followed by the user's name in hexadecimal UTF-8: `alice` becomes `userdb-616c696365`. Only that user (and administrators) can use it. Skylight keeps everything a user owns there.

## Security objects

Each database has a **security object**, `/<db>/_security`, listing its **admins** and **members** by name or role. If it has members, only they (and admins) can read or write it; if not, anyone can read it. A database admin can also change design documents.

| Database | Security object | Effect |
| --- | --- | --- |
| `skylight` | Empty | Anyone can read it, so the login page loads before anyone has logged in |
| `userdb-616c696365` | Members: `alice` | Only Alice (and server administrators) can read or write it |

couch_peruser makes the user both an admin and a member of their database. `tools/add-user` leaves them a member only, so they can't remove the checks described next.

## Validation functions

A design document's **`validate_doc_update`** function runs inside CouchDB (in its JavaScript engine, SpiderMonkey 1.8.5) before every write to that database, and can refuse it:

```javascript
function (newDoc, oldDoc, userCtx, secObj) {
	if (userCtx.roles.indexOf('_admin') < 0) {
		throw({ unauthorized: 'Only CouchDB administrators can change Skylight OS itself.' });
	}
}
```

`throw({ forbidden: message })` refuses with 403, `throw({ unauthorized: message })` with 401, and the message reaches the browser as the reply's `reason`. Every validation function in a database must agree. Skylight has three: one each for the `skylight` database, for `_users`, and for every user database. They are written in ES5, like the pages.
