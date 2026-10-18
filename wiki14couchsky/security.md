# Security

The pages are the PHP version's, with its [protections](../wiki03/security.md): text from apps is shown with `textContent`, only the desktop's own iframes on the same origin can send it messages (so a site shown in the Web Browser can't control it), and the dialog passes no code around. This page covers what CouchDB changes.

## What CouchDB protects

| Concern | How | Tested |
| --- | --- | --- |
| Reading another user's data | Each user's database has only them as a member | Bob gets 403 reading Alice's database; anonymous requests get 401 |
| Changing Skylight itself, or its apps | The `skylight` database's validation function refuses anyone but administrators | Anonymous and logged-in users changing, deleting or adding an app document, or adding an attachment to one, get 401 |
| Broken app documents | The same function refuses, even from administrators, an app document without `"type": "app"` and a manifest with a title, or with a name other than lowercase letters, digits and `-` | 403 |
| Storing bad settings or files | The user database's validation function checks every document's type and fields, file names and sizes | Bad values, unknown documents and a 1.1 MB file are refused with 403 |
| Removing those checks | `tools/add-user` leaves users as members, not admins, of their database | Deleting the design document gets 403 |
| Anyone creating an account | The `_users` validation function added by `tools/push` refuses new users from anyone but administrators | Anonymous sign-up gets 403 |
| HTML attachments used against an administrator | Neither a user's database nor their `_users` document accepts attachments. (An HTML attachment is served from CouchDB's own address, so if an administrator opened it, its scripts would run with the administrator's login; see CVE-2021-38295.) | Both refused with 403 |
| Making yourself an administrator | CouchDB itself: only administrators may set roles | Refused with 403 |
| Forged requests from other sites | The pages send JSON (`Content-Type: application/json`), which a browser won't send cross-site without CORS permission; CouchDB's CORS is off by default | — |

## What it doesn't

* **Password hashes are weak by default.** CouchDB 2.3.1 stores PBKDF2-SHA1 with **10** iterations unless you raise `[couch_httpd_auth] iterations` (for example to 10000) before adding users. Existing users get the new setting when their password changes. The `_users` database is readable only by administrators, but the hashes should still be strong.
* **couch_peruser can undo the member-only setting.** If a user changes their own password through CouchDB's API, couch_peruser makes them an admin of their database again, and they could then remove its checks. That only affects their own data; running `tools/add-user` (which you'd use to change passwords anyway) sets it back.
* **Apps are trusted.** Every app is a page on the same origin, with the user's login cookie, so it can do anything the user can in CouchDB: read and change all their documents. Only push apps you trust: pushing one installs it for every user at once.
* **The Terminal runs any JavaScript** as the user. Remove its `menu` number (it can still be opened by path) or delete its document if users shouldn't have it.
* **CouchDB administrators can see everything**, including every user's database. Their names and passwords are in CouchDB's configuration; keep them out of shared scripts (the tools take them as an argument).
* **Use HTTPS beyond your own computer.** CouchDB listens on `127.0.0.1` by default. If you open it to the network, put it behind HTTPS (CouchDB's `[ssl]` settings, or a proxy), or passwords and cookies cross the network in the clear.
* **CouchDB 2.3.1 is old.** It was released in 2019 and the 2.x line no longer gets security fixes. Later fixes that concern it include CVE-2021-38295 (attachments used against administrators, which Skylight's checks block for its own databases) and CVE-2022-24706 (a remote-code-execution hole through the Erlang cluster port, when that port is reachable and the default Erlang cookie is used). Keep CouchDB's ports away from untrusted networks, and set your own Erlang cookie in `vm.args`.
