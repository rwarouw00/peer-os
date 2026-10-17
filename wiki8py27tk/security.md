# Security

Like the other desktop versions, this is a desktop program: nothing listens on the network, and no web page can control the desktop.

## What is protected

| Concern | How |
| --- | --- |
| Stored passwords | PBKDF2-SHA256, random salt, 20,000 iterations; compared in constant time (`hmac.compare_digest`) |
| Users reading each other's settings and files | Each user has their own folder; file names can't contain `/` or `\` or start with `.` |
| Bad settings values | Checked when saved and when read back |
| Text from apps | Labels, notifications and dialogs show text as typed |
| Web Browser addresses | Only `http` and `https` addresses made of ordinary URL characters are passed to the system browser |

## What isn't

* **Skylight's login doesn't protect files from other people using the same computer.** Anyone who can open `data/` can read every user's files and edit `users.json`. Give each person their own operating-system account and copy of Skylight, or set folder permissions.
* **Apps are fully trusted.** An app's `app.py` runs inside Skylight's Python process and can do anything Python can, including reading and deleting the user's files. Only install apps you trust; adding a folder to `apps/` is installing a program.
* **The Terminal runs any Python**, with the same rights as Skylight. Remove it from `apps/apps.json` if users shouldn't have it.
* **Python 2.7 is out of support.** Its last release (2.7.18) was in April 2020 and gets no security fixes. Skylight itself doesn't touch the network, but keep that in mind for the computer it runs on.
