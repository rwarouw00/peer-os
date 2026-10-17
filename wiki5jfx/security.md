# Security

Like the PHP-GTK2 version, this is a desktop program: nothing listens on the network, and no web page can control the desktop.

## What is protected

| Concern | How |
| --- | --- |
| Stored passwords | PBKDF2-SHA256, random salt, 20,000 iterations; compared in constant time (`MessageDigest.isEqual`) |
| Users reading each other's settings and files | Each user has their own folder; file names can't contain `/` or start with `.` |
| Bad settings values | Checked when saved and when read back |
| Text from apps | Labels, notifications and dialogs show text as typed |
| Web pages in the Web Browser | WebView runs pages in their own engine; they have no access to Skylight's objects |

## What isn't

* **Skylight's login doesn't protect files from other people using the same computer.** Anyone who can open `data/` can read every user's files and edit `users.properties`. Give each person their own operating-system account and copy of Skylight, or set folder permissions.
* **Apps are fully trusted.** Apps run inside Skylight's Java process and can do anything Java can, including reading and deleting the user's files. An `app.jar` is code: only install apps you trust.
* **The Terminal runs any JavaScript**, and Nashorn can reach every Java class (`java.io.File`, `java.lang.Runtime`, ...). Remove it from `apps/apps.txt` if users shouldn't have it.
* **Java 8 and JavaFX 8 are old.** Keep Java 8 updated if you use it; the WebView engine in particular is old, so be careful browsing untrusted sites.
