# Security

Like the other desktop versions, this is a program on one computer: nothing listens on the network, and no web page can control the desktop.

## What is protected

| Concern | How |
| --- | --- |
| Stored passwords | PBKDF2-SHA256, random salt, 20,000 iterations; compared in constant time (`MessageDigest.isEqual`) |
| Users reading each other's settings and files | Each user has their own folder; file names can't contain `/` or start with `.` |
| Bad settings values | Checked when saved and when read back |
| Text from apps | Notifications and labels show text as given. (Swing treats any text starting with `<html>` as HTML, but that can only change how it looks: Swing labels can't run code) |
| Web pages in the Web Browser | Swing's `JEditorPane` doesn't run JavaScript, and pages have no access to Skylight's objects |

## What isn't

* **Skylight's login doesn't protect files from other people using the same computer.** Anyone who can open `data/` can read every user's files and edit `users.properties`. Give each person their own operating-system account and copy of Skylight, or set folder permissions.
* **Apps are fully trusted.** An app's Tcl runs inside Skylight's Java process, and Jacl's `java::` commands reach any Java class (`java.io.File`, `java.lang.Runtime`, ...), so it can read and delete the user's files, or anyone's. Jacl's own `open`, `file` and `exec` commands work too. Only `exit` is removed, so a script can't close Skylight by accident. Adding a folder to `apps/` is installing a program; only install apps you trust. The same goes for `ui/`, whose commands Skylight runs.
* **The Terminal runs any Tcl**, with the same power. Remove it from `apps/apps.txt` if users shouldn't have it.
* **The libraries are old and unmaintained.** Luxor 1.0 b8 (2003), Jacl 1.3.3 (2006) and JDOM b8 haven't had security fixes for many years; Skylight's changes to Luxor and its `sun.io` stand-ins for Jacl are new code, tested only as far as [Limitations](limitations.md#how-it-was-tested) says. Skylight only gives them its own files to read, but don't use them to open files or pages from people you don't trust. JDOM, as Luxor and Skylight use it, also reads external entities in XML: tested, a `.xul` file can pull the contents of another local file into a label's text. That matters only if `.xul` files come from someone else, which is the same trust as installing their app.
* **The Web Browser is old too.** Swing's HTML viewer is safe in that it runs no scripts, but it does load pages and images from the addresses it's given.
* **Java 8 is old.** Keep it updated if you use it.
