# Security

The PHP-GTK2 version is a desktop program, not a website, which removes most of the security problems of the browser versions. Nothing listens on the network, no web page can reach the desktop, and no request can come from another computer.

## What is protected

| Concern | How |
| --- | --- |
| Stored passwords | PBKDF2-SHA256 with a random salt and 20,000 iterations; compared in constant time |
| Users reading each other's settings and files | Each user's data is in their own folder, and file names can't contain `/` or start with `.` |
| Bad settings values | Every setting is checked by `Storage::cleanSetting()`; unknown settings are refused |
| Text containing markup | Labels, notifications and dialogs show text as typed |
| The Web Browser running commands | Only plain `http://` and `https://` addresses without spaces, quotes or `%` are passed to the system's browser |

## What isn't

* **Skylight's login doesn't protect files from other people using the same computer.** Anyone who can open the `data` folder can read every user's settings and files, and can edit `users.json`. If several people share a computer, give each one their own operating-system account and their own copy of Skylight, or set folder permissions.
* **Apps are fully trusted.** Every app runs in the same PHP process as the desktop and can do anything PHP can, including reading and deleting any file the user can. Only install apps you trust.
* **The Terminal runs any PHP code**, by design. Remove it from `apps/apps.json` if users shouldn't have it.
* **PHP 5.2 and the GTK+ 2 runtime are old** and no longer receive security fixes. That matters far less for a program that never talks to the network, but be wary of opening images or files from untrusted sources.
