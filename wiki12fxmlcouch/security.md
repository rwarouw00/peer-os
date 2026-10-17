# Security

Skylight itself is a desktop program, as in the [FXML version](../wiki11fxml/security.md): nothing listens on the network, and no web page can control the desktop. What's new is that its apps, which are programs, now come over the network from the app store.

## What is protected

| Concern | How |
| --- | --- |
| Stored passwords | PBKDF2-SHA256, random salt, 20,000 iterations; compared in constant time (`MessageDigest.isEqual`) |
| Users reading each other's settings and files | Each user has their own folder; file names can't contain `/` or start with `.` |
| Bad settings values | Checked when saved and when read back |
| Text from apps | Labels, notifications and dialogs show text as typed |
| Web pages in the Web Browser | WebView runs pages in their own engine; they have no access to Skylight's objects |
| Changing the apps in the store | The store's `validate_doc_update` refuses any write from a user who isn't a CouchDB administrator (tested: anonymous and ordinary users get 401 or 403) |
| Users' data on the server | There is none: the store holds only apps, which anyone may read |

## What isn't

* **Whoever controls the store controls every desktop.** An FXML app's scripts run inside Skylight's Java process, and Nashorn lets them use any Java class (`java.io.File`, `java.lang.Runtime`, ...), so an app can read and delete the user's files, or anything else that user may touch. A store is a way to install software on many computers at once. Protect the CouchDB administrator accounts accordingly, and don't point Skylight at a store you don't trust.
* **Plain HTTP can be tampered with.** Skylight downloads apps exactly as given in `appStore=`. Over plain `http://` on a network you don't control, someone in between could change an app on its way to the desktop. Use `https://` (CouchDB 2.x can serve TLS itself, or put it behind a proxy such as nginx); Java checks the server's certificate (HTTPS wasn't tested here; see [Limitations](limitations.md#how-it-was-tested)). Plain HTTP is fine for `127.0.0.1` or a trusted network.
* **Apps can't be signed.** Skylight trusts whatever the store serves; there's no check that an app came from a particular author.
* **The store is public to read.** Don't put secrets (passwords, keys) in an app's files: anyone who can reach the server can download them.
* **Skylight's login doesn't protect files from other people using the same computer.** Anyone who can open `data/` can read every user's files and edit `users.properties`. Give each person their own operating-system account and copy of Skylight, or set folder permissions. `skylight.properties` must be protected the same way: whoever can edit it chooses where apps come from.
* **The Terminal runs any JavaScript**, with the same power as any app. Remove it from `apps.txt` in the store if users shouldn't have it.
* **Java 8 and JavaFX 8 are old.** Keep Java 8 updated if you use it; the WebView engine in particular is old, so be careful browsing untrusted sites.
