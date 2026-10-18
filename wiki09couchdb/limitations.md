# Limitations

## Behaviour

The pages are the PHP version's, so its [limitations](../wiki03/limitations.md) about windows, the Terminal and apps apply here too. Because of CouchDB:

* **The login lasts 10 minutes without activity**, CouchDB's default cookie timeout. Skylight only talks to CouchDB when you log in, save or open something, so after a quiet spell the next save says you're not logged in. Raise `[couch_httpd_auth] timeout` (see [Data and configuration](data-and-configuration.md#couchdb-settings)).
* **Adding users needs an administrator**, with `tools/add-user`; there's no sign-up page, and no page for changing a password.
* **Changing Skylight means pushing it again**: the pages live in the design document, not in a folder CouchDB reads.
* **Files are text**, up to 1 MB each, as in the PHP version; they're stored inside JSON documents, not as attachments.
* **Requests are synchronous**, as they always were in Skylight OS. Browsers still allow synchronous `XMLHttpRequest` on pages, but warn about it in the console, and a slow CouchDB freezes the page while it answers.
* **Notifications** are kept only until the page is reloaded.

## CouchDB 3

Skylight is written for CouchDB 2.3.1, as requested. It mostly works on CouchDB 3, with three things to know:

* CouchDB 3 has **no "admin party"** and creates no system databases by itself; the set-up in [Getting started](getting-started.md) already covers that.
* From 3.1.2, CouchDB sends attachments with `Content-Security-Policy: sandbox`, which stops their scripts. To serve Skylight, set `[csp] attachments_enable = false`, and keep the attachment checks described in [Security](security.md).
* **Rewrites are deprecated** in CouchDB 3 (still working). Opening `.../_design/skylight/index.html` directly doesn't need them.

## How it was tested

CouchDB's binary packages for 2.3.1 are no longer at their old address, so CouchDB was **built from Apache's 2.3.1 source release**, on Linux, with the dependencies that release asks for:

* **Erlang/OTP 21.2.6** (Ubuntu's packages, unpacked locally; 2.3.1 refuses Erlang 20 before 20.3.8.11);
* **SpiderMonkey 1.8.5** (`libmozjs185`), which runs the validation functions;
* the source release's own Fauxton.

It ran as a single node, on `127.0.0.1:5984`, set up as in [Getting started](getting-started.md): system databases, an administrator, and `tools/push.sh` and `tools/add-user.sh`. The PowerShell tools, `push.ps1` and `add-user.ps1`, were run with PowerShell 7.4 on Linux against the same CouchDB, and installed an equivalent design document (the same attachments, rewrites and checks); Windows PowerShell 5.1 itself wasn't available.

The pages were tested in **Chromium** through Playwright, with the PHP version's browser test adapted to read CouchDB's documents: logging in (including a wrong password), staying logged in after a reload, the home menu, the Terminal, toasts, dialogs with OK and Cancel (including HTML in the message, shown as text), files (writing, reading, listing, a refused `../` name, a missing file), shortcuts (adding, dragging and saving, double-clicking), windows (front, maximize, hide, taskbar Close), the Settings app (background, notification time, a refused value), `App.closeWindow`, a missing app, a page in the Web Browser trying to send messages to the desktop, and logging out. The Notes tutorial was built from this documentation's code, pushed, and tested: open from the menu, save, reopen, Clear with OK.

CouchDB's side was tested with `curl`: writes to the `skylight` database by anonymous and logged-in users, bad settings, unknown documents, wrong and oversized files, attachments in a user's database and on their `_users` document, another user and an anonymous visitor reading a user's database, anonymous sign-up, making yourself an administrator, deleting a database's checks, and a user changing their own password. All behaved as described in [Security](security.md). The screenshots come from these runs.

What wasn't tested:

* **The Windows and macOS builds of CouchDB 2.3.1**, and Windows PowerShell 5.1. The pages and tools only use CouchDB's HTTP API, which is the same on every platform.
* **Browsers other than Chromium**, although the pages are the PHP version's, which use only ES5 and long-standing browser features.
* **A cluster of several CouchDB nodes.** couch_peruser and the rest work the same way in a cluster, but there may be a short delay before a new user's database exists on every node.
