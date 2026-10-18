# Security

Like the PHP-GTK2 and JavaFX versions, this is a desktop program: nothing listens on the network, and no web page can control the desktop.

## What is protected

| Concern | How |
| --- | --- |
| Stored passwords | PBKDF2-SHA256, random salt, 20,000 iterations; compared in constant time |
| Users reading each other's settings and files | Each user has their own folder; file names can't contain `/` or `\` or start with `.` |
| Bad settings values | Checked when saved and when read back |
| Text from apps | Labels, notifications and dialogs show text as typed |
| Web pages in the Web Browser | The `WebBrowser` control runs pages in Internet Explorer's engine; Skylight doesn't give them access to its objects (`ObjectForScripting` isn't set) |
| Web Browser addresses | Only `http` and `https` addresses are opened |

## What isn't

* **Skylight's login doesn't protect files from other people using the same computer.** Anyone who can open `data\` can read every user's files and edit `users.json`. Give each person their own Windows account and copy of Skylight, or set folder permissions.
* **Apps are fully trusted.** Apps run inside Skylight's process and can do anything the .NET Framework can, including reading and deleting the user's files. An `app.dll` is a program: only install apps you trust. Windows may also block a DLL downloaded from the internet until you unblock it in its Properties.
* **The Terminal runs any C#**, with the same rights as Skylight. Remove it from `apps\apps.json` if users shouldn't have it.
* **The web engine is old.** Internet Explorer 11 no longer gets new features; Windows still patches the engine for security, but be careful browsing untrusted sites.
