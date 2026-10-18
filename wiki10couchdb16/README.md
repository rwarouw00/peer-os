# Skylight OS as a CouchDB 1.6 couchapp

This is Skylight OS running entirely inside **Apache CouchDB 1.6** (1.6.1), as a *couchapp*: the web pages are attachments of a CouchDB design document, and CouchDB itself is the server. There's no PHP and no web server. The code is in [`../site09couchdb`](../site09couchdb).

The pages are the browser version's ([`../site03`](../site03), documented in [`../wiki03`](../wiki03/README.md)): the same desktop, windows, apps and App API, in plain ES5 JavaScript. What changed is everything behind them:

| site03 (PHP) | site10couchdb16 (CouchDB 1.6) |
| --- | --- |
| `api/login.php`, `api/logout.php` | CouchDB's `/_session` (cookie login) |
| `data/users.json`, `tools/add-user.php` | CouchDB's `_users` database, `tools/add-user` |
| `data/users/<name>/settings.json` | A `settings` document in the user's own database |
| `data/users/<name>/files/<file>` | A `file:<file>` document in the user's own database |
| PHP checks on every save | `validate_doc_update` functions run by CouchDB |
| Files under `public/`, served by the web server | Attachments of `_design/skylight`, served by CouchDB |

Each user's database (`userdb-<name in hex>`) is created by `tools/add-user`, because CouchDB 1.6 has no couch_peruser feature, and can only be read by that user.

It's the same couchapp as the [CouchDB 2.3.1 version](../wiki09couchdb/README.md): the same pages, documents and checks. The differences are in how users and their databases are set up, and in one serious CouchDB 1.6.1 bug that the `_users` check has to block (see [Security](security.md)).

![Skylight OS served by CouchDB 1.6.1](images/terminal-desktop.png)

## Contents

**Using Skylight OS**

* [Getting started](getting-started.md): install CouchDB, push the couchapp, add users
* [User guide](user-guide.md): the desktop, windows, notifications and the built-in apps

**Building apps**

* [Building your first app](building-apps.md): a step-by-step tutorial
* [App manifest](app-manifest.md): the `app.json` file
* [App API](app-api.md): the `App` object apps use
* [UI components](ui-components.md): the shared styles

**How it works**

* [Architecture](architecture.md): the browser, the design document and the databases
* [CouchDB primer](couchdb-primer.md): the CouchDB features Skylight uses
* [Desktop internals](desktop-internals.md): what changed from the PHP version, and `core.js`
* [Data and configuration](data-and-configuration.md): documents, databases, tools and settings

**Background**

* [Compared with the other versions](comparison.md)
* [Security](security.md)
* [Limitations](limitations.md)

## At a glance

| | |
| --- | --- |
| Runs on | Apache CouchDB 1.6.1 (1.6 and 1.7 in general); any modern browser |
| Languages | ES5 JavaScript, HTML and CSS; JavaScript `validate_doc_update` functions in CouchDB |
| Server code | None: three validation functions and two rewrite rules |
| Install tools | `tools/push.sh` and `tools/add-user.sh` (bash and curl), or `tools\push.ps1` and `tools\add-user.ps1` (PowerShell) |
| Data | CouchDB documents, one private database per user |
| Tested | On a CouchDB 1.6.1 built from Apache's source release with Erlang 17.3, with Chromium (see [Limitations](limitations.md#how-it-was-tested)) |
