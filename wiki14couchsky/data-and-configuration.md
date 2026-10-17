# Data and configuration

## The home menu

There's no list to edit: the home menu shows every app document whose manifest has a `menu` number, lowest first (see [App manifest](app-manifest.md) and [The app store](app-store.md#the-home-menu)). After pushing an app, reload the page.

## The skylight database

The design document `_design/skylight`, holding the desktop, and one document per app (see [The app store](app-store.md)). The design document:

```json
{
	"_id": "_design/skylight",
	"validate_doc_update": "function (newDoc, oldDoc, userCtx, secObj) { ... }",
	"rewrites": [ { "from": "", "to": "index.html" }, { "from": "system/apps.json", "to": "_view/apps" }, ... ],
	"views": { "apps": { "map": "function (doc) { ... }" } },
	"_attachments": {
		"index.html": { "content_type": "text/html; charset=utf-8", ... },
		"system/js/core.js": { "content_type": "application/javascript; charset=utf-8", ... },
		...
	}
}
```

An app document:

```json
{
	"_id": "terminal",
	"type": "app",
	"manifest": { "title": "Terminal", "menu": 1, "position": "centered", "width": 500, "height": 300, "icons": { ... } },
	"_attachments": {
		"index.html": { "content_type": "text/html; charset=utf-8", ... },
		"icons/16.png": { "content_type": "image/png", ... },
		...
	}
}
```

Anyone can read the database; only CouchDB administrators can change it.

## Users

Accounts are CouchDB users, in `_users`:

```json
{
	"_id": "org.couchdb.user:alice",
	"name": "alice",
	"type": "user",
	"roles": [],
	"password_scheme": "pbkdf2",
	"iterations": 10,
	"derived_key": "...",
	"salt": "..."
}
```

Add users, or change a password, with `tools/add-user.sh` or `tools\add-user.ps1`. To remove one, delete their `_users` document and their `userdb-…` database (in Fauxton, or with `DELETE` requests as an administrator); CouchDB keeps the database unless `[couch_peruser] delete_dbs` is `true`.

## A user's database

`userdb-<name in hex>`, for example `userdb-616c696365` for `alice`, holds:

![A user's documents in Fauxton](images/fauxton.png)

### Settings

```json
{
	"_id": "settings",
	"type": "settings",
	"background": "system/images/backgrounds/1.jpg",
	"notificationTime": 3000,
	"shortcuts": [ { "path": "apps/terminal/", "left": 10, "top": 60 } ]
}
```

| Field | Default | Rule |
| --- | --- | --- |
| `background` | `system/images/backgrounds/3.jpg` | A `.jpg`, `.jpeg`, `.png` or `.gif` path made of letters, digits, `_`, `-` and `/` |
| `notificationTime` | `7000` | Milliseconds, 1000 to 60000 |
| `shortcuts` | `[]` | Up to 50, each with an app `path` ending in `/`, and `left` and `top` in pixels |

A user without a `settings` document gets the defaults; it's created the first time they change something.

### Files

```json
{
	"_id": "file:notes.txt",
	"type": "file",
	"name": "notes.txt",
	"contents": "Buy milk.\nCall Sam about Saturday."
}
```

One document per file, with the text in `contents` (up to 1 MB). Names may use letters, digits, spaces, `-`, `_` and `.` (up to 100 characters, not starting with `.`).

### The checks

`_design/skylight` in each user's database holds the `validate_doc_update` function from [`userdb/validate_doc_update.js`](../site14couchsky/userdb/validate_doc_update.js). It accepts only the `settings` document and `file:` documents with valid fields, and only from that user or an administrator.

## CouchDB settings

| Setting | Value | Set by |
| --- | --- | --- |
| `[couch_peruser] enable` | `true` | `tools/push`: gives each user their own database |
| `[couch_peruser] delete_dbs` | `false` (default) | Set it to `true` to delete a user's database with their account |
| `[couch_httpd_auth] timeout` | `600` (default) | Seconds without a request before the login cookie stops working. Raise it to stay logged in longer, for example to 8 hours: `curl -X PUT http://admin:password@127.0.0.1:5984/_node/_local/_config/couch_httpd_auth/timeout -d '"28800"'` |
| `[couch_httpd_auth] iterations` | `10` (default) | PBKDF2 rounds for new password hashes; see [Security](security.md) |
| `[chttpd] bind_address` | `127.0.0.1` (default) | Set it to `0.0.0.0` to let other computers use Skylight |

Change these in Fauxton (**Configuration**), in CouchDB's `local.ini`, or with the `_config` API as in the example. In the API, CouchDB 2.3 accepts `_local` for the node; its full name is shown by `GET /_membership`.
