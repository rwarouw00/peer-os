# Configuration and data

## apps.json

[`public/system/apps.json`](../site03/public/system/apps.json) lists the apps in the home menu, in order. Each entry is an app folder relative to the site root, ending in `/`:

```json
[
	"apps/terminal/",
	"apps/settings/",
	"apps/web-browser/"
]
```

Entries without a readable `app.json` are skipped. Changes show up when the page is next loaded.

## The data folder

[`site03/data/`](../site03/data/) is outside the web root, so the web server never hands its files out. The web server's user must be able to write to it. Everything in it is created as needed:

```
data/
├── users.json
└── users/
    └── <name>/
        ├── settings.json
        └── files/
```

### users.json

```json
{"admin": {"password": "$2y$10$UPRxiWXheQMJRfYb..D7gOJzAV0yKounTm6XdwHx.bHxivLUBCNnS"}}
```

One entry per user, holding a bcrypt hash of the password. Manage it with `php tools/add-user.php <username> <password>`; there is no need to edit it by hand. To remove a user, delete their entry and their `users/<name>/` folder.

### Settings

`users/<name>/settings.json`:

```json
{
	"background": "system/images/backgrounds/1.jpg",
	"notificationTime": 3000,
	"shortcuts": [{"path": "apps/settings/", "left": 100, "top": 60}]
}
```

| Setting | Default | Meaning |
| --- | --- | --- |
| `background` | `system/images/backgrounds/3.jpg` | Desktop background, relative to the site root |
| `notificationTime` | `7000` | How long toasts stay, in milliseconds (1000–60000) |
| `shortcuts` | `[]` | Desktop shortcuts: app path and position in pixels |

A user with no settings file gets the defaults; the file is written the first time they change something. The defaults and the rules for each setting are in `default_settings()` and `clean_setting()` in [`lib/skylight.php`](../site03/lib/skylight.php).

The backgrounds that ship with the system are `system/images/backgrounds/1.jpg` to `6.jpg`.

### User files

`users/<name>/files/` holds what the user's apps saved with [`App.writeFile`](app-api.md#appwritefile). It's a flat folder: file names can't contain slashes.

## Sessions

Logins use PHP's standard sessions, so PHP's own settings decide where session files go (`session.save_path`) and how long an idle session lasts (`session.gc_maxlifetime`). The session cookie lasts until the browser closes.
