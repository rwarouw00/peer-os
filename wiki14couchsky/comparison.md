# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names:

* **site03**: browser version: ES5 JavaScript, HTML and CSS with a PHP server ([`../wiki03`](../wiki03/README.md))
* **site04phpgtk**: desktop program in PHP 5.2 with PHP-GTK2 ([`../wiki04phpgtk`](../wiki04phpgtk/README.md))
* **site05jfx**: desktop program in Java 8 with JavaFX 8 ([`../wiki05jfx`](../wiki05jfx/README.md))
* **site06cs**: desktop program in C# 5 with Windows Forms ([`../wiki06cs`](../wiki06cs/README.md))
* **site07java7**: desktop program in Java 7 with JavaFX 2.2 ([`../wiki07java7`](../wiki07java7/README.md))
* **site08py27tk**: desktop program in Python 2.7 with Tkinter ([`../wiki08py27tk`](../wiki08py27tk/README.md))
* **site09couchdb**: the browser version as a CouchDB 2.3.1 couchapp ([`../wiki09couchdb`](../wiki09couchdb/README.md)), and **site10couchdb16** for CouchDB 1.6 ([`../wiki10couchdb16`](../wiki10couchdb16/README.md))
* **site11fxml** and **site12fxmlcouch**: the Java 8 version with FXML apps, read from files or from a CouchDB app store ([`../wiki11fxml`](../wiki11fxml/README.md), [`../wiki12fxmlcouch`](../wiki12fxmlcouch/README.md))
* **site14couchsky**: site09couchdb with each app in its own CouchDB document (this one)

## Next to site09couchdb

The same pages, users and data; only where the apps live changed.

| | site09couchdb | site14couchsky |
| --- | --- | --- |
| Apps are | Folders of attachments in `_design/skylight`, with the desktop | One document each, named after the app |
| The home menu | `system/apps.json`, a list edited by hand | A view of the app documents, ordered by `menu` in each `app.json` |
| Installing or updating an app | Edit `apps.json`, push all of Skylight | `tools/push-app <address> <app>` |
| An app's manifest | The `app.json` attachment | The document's `manifest` field |
| Addresses | `.../_rewrite/apps/notes/...` or `.../_design/skylight/apps/notes/...` | `.../_rewrite/apps/notes/...` only (the rewrites find the app's document) |
| Checks | Only administrators may write | The same, plus every app document needs a name, a type and a manifest with a title |
| Code changed | — | About 10 lines of `desktop.js`, three rewrite rules, one view, the validation function, and the push tools |

## Next to site12fxmlcouch

Both keep each app in a CouchDB document with its files attached, and both read apps without logging in:

| | site12fxmlcouch | site14couchsky |
| --- | --- | --- |
| The desktop runs | As a Java program on each computer | In a web browser, served by the same CouchDB |
| An app is | FXML and JavaScript (Nashorn) | An HTML page with ES5 JavaScript |
| The home menu | `apps.txt`, attached to `_design/store` | A CouchDB view |
| Users' data | `.properties` files on each computer | A private CouchDB database per user |

## site09couchdb next to the PHP browser version

The pages are site03's, so for users and app developers the two are the same: the same desktop, apps, `App` API and tutorial. The difference is the server.

| | site03 (PHP) | site09couchdb (CouchDB) |
| --- | --- | --- |
| Server | Any web server with PHP 5.3.7+ | CouchDB 2.3.1, nothing else |
| Server code | ~210 lines of PHP in 5 files | Three `validate_doc_update` functions and two rewrite rules (~90 lines); `core.js` grew by ~180 lines instead |
| Installing | Copy files; point the web server at `public/` | `tools/push` uploads one design document |
| Logins | PHP sessions | CouchDB's `/_session` cookie |
| Passwords | bcrypt (cost 10) | CouchDB's PBKDF2-SHA1 (10 rounds by default; raise it, see [Security](security.md)) |
| Accounts | `data/users.json` | CouchDB users in `_users` |
| A user's data | Files in `data/users/<name>/` | Documents in a private database, `userdb-<hex>` |
| Checks | PHP, before writing files | `core.js` before sending, and CouchDB's validation functions before storing |
| Concurrent saves | Last write wins | CouchDB's revisions: a conflict is detected and retried |
| Getting at the data | Files on the server | CouchDB's HTTP API and Fauxton; replication to other CouchDBs |
| Size (non-blank lines) | ~1,360 | ~1,300 in the pages, ~90 of validation and rewrites, ~290 in the install tools |

## All versions side by side

| | site03 | site04phpgtk | site05jfx | site06cs | site07java7 | site08py27tk | site09couchdb |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Runs in | Browser + PHP | PHP-GTK2 | Java 8 | .NET (WinForms) | Java 7 | Python 2.7 | Browser + CouchDB |
| Apps are | Pages in iframes | PHP classes | Java classes | C# classes | Java classes | Python classes | Pages in iframes |
| Terminal runs | JavaScript | PHP | JavaScript | C# | JavaScript | Python | JavaScript |
| Users | One server, many users | One user per program | One user per program | One user per program | One user per program | One user per program | One server, many users |
| Data | JSON files | JSON files | `.properties` | JSON files | `.properties` | JSON files | CouchDB documents |

## What CouchDB brought

* **No server code to look after.** The login, the storage, the access rules and the web server are CouchDB's; Skylight adds rules, not programs.
* **Per-user privacy enforced by the database.** A user's data is in a database only they can open, rather than relying on every PHP script to check the session.
* **Your data over HTTP.** Every setting and file is a JSON document that tools, scripts and Fauxton can read, back up, or replicate to another CouchDB.
* **Safe concurrent saves**, through revisions.

## What it costs

* **CouchDB 2.3.1 itself**, which is old (see [Security](security.md)) and, on Linux, now has to be built or found as an old package.
* **An administrator step for every user**, since `tools/add-user` creates the account and its database checks.
* **Weaker password hashing by default** than the PHP version's bcrypt, until you raise CouchDB's PBKDF2 iterations.
* **Couchapps are out of fashion.** CouchDB 3 still serves attachments and rewrites, but marks rewrites as deprecated and, by default, sends attachments with a restrictive `Content-Security-Policy` that stops pages like these from running scripts; see [Limitations](limitations.md#couchdb-3).

## Notes in every version

The same Notes app, written for all fourteen versions, is compared side by side in [Notes, in every version](../wikicompare/README.md).
