# Limitations

## Behaviour

The pages are the PHP version's, so its [limitations](../wiki3/limitations.md) about windows, the Terminal and apps apply here too. Because of CouchDB:

* **The login lasts 10 minutes without activity**, CouchDB's default cookie timeout. Skylight only talks to CouchDB when you log in, save or open something, so after a quiet spell the next save says you're not logged in. Raise `[couch_httpd_auth] timeout` (see [Data and configuration](data-and-configuration.md#couchdb-settings)).
* **Only administrators manage accounts**, with `tools/add-user`. Users can't sign up, and can't change their own password, because the `_users` check refuses all their writes (see [Security](security.md)).
* **Changing Skylight means pushing it again**: the pages live in the design document, not in a folder CouchDB reads.
* **Files are text**, up to 1 MB each, stored inside JSON documents.
* **Requests are synchronous**, as they always were in Skylight OS; a slow CouchDB freezes the page while it answers.
* **Notifications** are kept only until the page is reloaded.
* **One server.** CouchDB 1.6 has no clustering; replication can copy databases to other servers, but each user's database must be created there too.

## Moving to a newer CouchDB

The `attachments/`, `design/` and `userdb/` folders are the same as in the [CouchDB 2.3.1 version](../wiki9couchdb/README.md), so moving up means using that version's tools and `users/` check. Replicate the `userdb-…` databases and `_users` accounts across; the database names are the ones couch_peruser uses, so users keep their data. Then run that version's `tools/add-user` for each user, to set up their database as couch_peruser expects.

## How it was tested

CouchDB 1.6.1 was **built from Apache's source release** on Linux, with:

* **Erlang/OTP 17.3** (Ubuntu 15.04's packages, unpacked locally; 1.6.1's build accepts Erlang R14B to 17);
* **SpiderMonkey 1.8.5** (`libmozjs185`), which runs the validation functions;
* the system's ICU, through a small stand-in for the `icu-config` script that current ICU no longer ships, and with `-Werror` removed from the build, because current C headers warn where 2014's didn't.

It ran on `127.0.0.1:5984` with one administrator, set up as in [Getting started](getting-started.md), and Skylight was installed with `tools/push.sh` and `tools/add-user.sh`. The PowerShell tools were run with PowerShell 7.4 on Linux against the same server, and installed the same design document and user databases; Windows PowerShell 5.1 wasn't available.

The pages were tested in **Chromium** with the same browser test as the CouchDB 2.3.1 version, and every check passed unchanged: logging in (including a wrong password), staying logged in after a reload, the home menu, the Terminal, toasts, dialogs with OK and Cancel, files (including a refused `../` name and a missing file), shortcuts, windows, the Settings app, `App.closeWindow`, a missing app, a page in the Web Browser trying to send messages to the desktop, and logging out. The Notes tutorial was built from this documentation's code, pushed, and tested.

CouchDB's side was tested with `curl`: CVE-2017-12635 was first confirmed on the bare server (a sign-up and an edited account both gained the `_admin` role), then refused once Skylight was installed. Writes to the `skylight` database, bad settings, unknown documents, an oversized file, attachments in a user's database and on their account, another user and an anonymous visitor reading a user's database, sign-ups and deleting a database's checks were all refused, and a valid file was accepted. The screenshots come from these runs.

What wasn't tested:

* **The Windows and macOS builds of CouchDB 1.6.1**, CouchDB 1.7.2, and Windows PowerShell 5.1. The pages and tools only use CouchDB's HTTP API, which is the same on every platform.
* **Browsers other than Chromium**, although the pages use only ES5 and long-standing browser features.
