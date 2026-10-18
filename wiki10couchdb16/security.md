# Security

The pages are the PHP version's, with its [protections](../wiki03/security.md): text from apps is shown with `textContent`, only the desktop's own iframes on the same origin can send it messages (so a site shown in the Web Browser can't control it), and the dialog passes no code around. This page covers CouchDB 1.6.

## A serious CouchDB 1.6.1 bug

**CVE-2017-12635** (fixed in CouchDB 1.7.0 and 2.1.1): CouchDB checks a user document with its JavaScript validation functions, but stores it with its Erlang JSON parser, and the two disagree about a field given twice. A document with `"roles": ["_admin"], "roles": []` passes the checks (JavaScript sees the empty list) but is stored with the `_admin` role. On a plain CouchDB 1.6.1, where anyone may sign up and users may edit their own document, **anyone who can reach the server can make themselves a server administrator**. This was confirmed on the test server before Skylight was installed.

Skylight's `_users` check (from [`users/validate_doc_update.js`](../site10couchdb16/users/validate_doc_update.js), installed by `tools/push`) refuses every write to `_users` by anyone but an administrator, whatever the document says. With it, the same requests are refused, both as a sign-up and as an edit of one's own account. The cost is that users can't change their own password; an administrator does it with `tools/add-user`.

A related bug, CVE-2017-12636, lets an *administrator* run programs on the server through the configuration API. It doesn't give ordinary users anything, but it's one more reason to keep administrator passwords safe, and to prefer 1.7.2.

## What CouchDB protects

| Concern | How | Tested |
| --- | --- | --- |
| Becoming an administrator (CVE-2017-12635) | The `_users` check refuses all non-administrator writes | Refused with 403, as a sign-up and on one's own account |
| Anyone creating an account | The same check | Refused with 403 |
| Reading another user's data | Each user's database has only them as a member | Another user and an anonymous visitor both get 401 |
| Changing Skylight itself | The `skylight` database's validation function refuses anyone but administrators | Anonymous and logged-in writes get 401 |
| Storing bad settings or files | The user database's validation function checks every document's type and fields, file names and sizes | Bad values, unknown documents and a 1.1 MB file are refused with 403 |
| Removing those checks | `tools/add-user` makes users members, not admins, of their database | Deleting the design document gets 401 |
| HTML attachments used against an administrator | Neither a user's database nor `_users` accepts them from users. (An HTML attachment is served from CouchDB's own address, so if an administrator opened it, its scripts would run with the administrator's login.) | Refused with 403 |
| Forged requests from other sites | The pages send JSON, which a browser won't send cross-site without CORS permission; CouchDB 1.6's CORS is off by default | — |

## What it doesn't

* **"Admin party" until you create an administrator.** A new CouchDB 1.6 lets everyone do anything. Create an administrator before anything else, as in [Getting started](getting-started.md).
* **Duplicate fields in a user's own documents.** The same parser difference lets a user store, say, two `notificationTime` fields, one of which skipped the checks. Only their own data is affected, and the pages read the checked value; CouchDB itself refuses a duplicated `_attachments`.
* **Password hashes are weak by default**: PBKDF2-SHA1 with **10** iterations, unless you raise `[couch_httpd_auth] iterations` before adding users. `_users` is readable only by administrators, but the hashes should still be strong.
* **Apps are trusted.** Every app is a page on the same origin, with the user's login cookie, so it can do anything the user can in CouchDB. Only add apps you trust.
* **The Terminal runs any JavaScript** as the user. Remove it from `apps.json` if users shouldn't have it.
* **CouchDB administrators can see everything.** Keep their names and passwords out of shared scripts (the tools take them as an argument).
* **Use HTTPS beyond your own computer.** CouchDB listens on `127.0.0.1` by default; if you open it to the network, use its `[ssl]` settings or a proxy.
* **CouchDB 1.6 is very old.** It was released in 2014, and the 1.x line ended with 1.7.2 in 2018. Other later fixes (for example CVE-2018-8007 and CVE-2018-11769, both about administrators running programs through the configuration) don't affect ordinary users but do make administrator accounts more dangerous. Keep it away from untrusted networks.
