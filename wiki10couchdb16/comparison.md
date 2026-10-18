# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names:

* **site03**: browser version: ES5 JavaScript, HTML and CSS with a PHP server ([`../wiki03`](../wiki03/README.md))
* **site04phpgtk**: desktop program in PHP 5.2 with PHP-GTK2 ([`../wiki04phpgtk`](../wiki04phpgtk/README.md))
* **site05jfx**: desktop program in Java 8 with JavaFX 8 ([`../wiki05jfx`](../wiki05jfx/README.md))
* **site06cs**: desktop program in C# 5 with Windows Forms ([`../wiki06cs`](../wiki06cs/README.md))
* **site07java7**: desktop program in Java 7 with JavaFX 2.2 ([`../wiki07java7`](../wiki07java7/README.md))
* **site08py27tk**: desktop program in Python 2.7 with Tkinter ([`../wiki08py27tk`](../wiki08py27tk/README.md))
* **site09couchdb**: the browser version as a CouchDB 2.3.1 couchapp ([`../wiki09couchdb`](../wiki09couchdb/README.md))
* **site10couchdb16**: the same couchapp for CouchDB 1.6 (this one)

## Next to the CouchDB 2.3.1 version

The pages (`attachments/`), the documents, and the checks in the `skylight` database and in each user's database are identical: the browser tests and the Notes tutorial passed unchanged on both. The differences are in the set-up:

| | site09couchdb (CouchDB 2.3.1) | site10couchdb16 (CouchDB 1.6) |
| --- | --- | --- |
| Admin pages | Fauxton | Futon |
| First administrator | Created by Fauxton's single-node set-up | Created with Futon's **Fix this**; until then, "admin party" |
| A user's database | Created by couch_peruser, which `tools/push` turns on | Created by `tools/add-user` |
| User's rights on it | couch_peruser makes them admin; `tools/add-user` reduces that to member | Member from the start |
| `_users` check | Refuses sign-ups and attachments; users may change their own password | Refuses every change by non-administrators, because of CVE-2017-12635 |
| Changing a password | The user (through CouchDB's API) or `tools/add-user` | `tools/add-user` only |
| Configuration API | `/_node/<node>/_config/...` | `/_config/...` |
| Clustering | Built in (single node used here) | None: one server |
| Attachments and CSP | No CSP in 2.3.1; CouchDB 3.1.2+ adds one that must be turned off | No CSP |

The tools are the only code that differs: about 20 lines in `push` and `add-user` (no couch_peruser to turn on or wait for), and the `_users` check.

## Next to the PHP browser version

Everything said in the [CouchDB 2.3.1 version's comparison](../wiki09couchdb/comparison.md#next-to-the-php-browser-version) applies: the pages are site03's, CouchDB replaces the PHP API, logins are CouchDB sessions, each user's data is a private database of JSON documents, and validation functions replace the PHP checks.

## All versions side by side

| | site03 | site04phpgtk | site05jfx | site06cs | site07java7 | site08py27tk | site09couchdb | site10couchdb16 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Runs in | Browser + PHP | PHP-GTK2 | Java 8 | .NET (WinForms) | Java 7 | Python 2.7 | Browser + CouchDB 2.3 | Browser + CouchDB 1.6 |
| Apps are | Pages in iframes | PHP classes | Java classes | C# classes | Java classes | Python classes | Pages in iframes | Pages in iframes |
| Users | Many per server | One per program | One per program | One per program | One per program | One per program | Many per server | Many per server |
| Data | JSON files | JSON files | `.properties` | JSON files | `.properties` | JSON files | CouchDB documents | CouchDB documents |

## Which CouchDB?

If you have the choice, use the 2.3.1 version, or better, CouchDB 3 with the notes in its [limitations](../wiki09couchdb/limitations.md#couchdb-3). CouchDB 1.6 is useful where it's already installed and can't be changed; even then, **1.7.2**, the last 1.x release, fixes the worst of 1.6.1's security holes and has the same API (it wasn't tested here).

## Notes in every version

The same Notes app, written for all fourteen versions, is compared side by side in [Notes, in every version](../wikicompare/README.md).
