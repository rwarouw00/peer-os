# Data and configuration

## apps/apps.json

The apps in the home menu, in order. Each entry is an app folder ending in `/`:

```json
[
	"apps/terminal/",
	"apps/settings/",
	"apps/web-browser/"
]
```

Changes show up at the next login.

## skylight.gtkrc

Colours for Skylight's own parts, in GTK's resource-file format (see the [primer](gtk-primer.md#styles)):

| Widget name | What it is |
| --- | --- |
| `skylight-panel` | The top panel |
| `skylight-toast` | Notification toasts |
| `skylight-shortcut` | Desktop shortcut labels |

To use a whole GTK theme, such as GNOME 2's **Clearlooks**, uncomment the `gtk-theme-name` line.

## data/

Everything is created as it is needed. The folder must be writable by the user running Skylight.

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
{"admin": {"password": "pbkdf2-sha256$20000$1f7e3180...$d23d0a7a..."}}
```

Passwords are stored as **PBKDF2-SHA256** hashes with a random salt and 20,000 iterations, built from `hash_hmac()` so they work on PHP 5.2. Manage users with:

```
php tools\add-user.php <username> <password>
```

To remove a user, delete their entry and their `users/<name>/` folder.

### Settings

`users/<name>/settings.json`:

```json
{
	"background": "images/backgrounds/1.jpg",
	"notificationTime": 3000,
	"shortcuts": [{"path": "apps/terminal/", "x": 20, "y": 20}]
}
```

| Setting | Default | Rule |
| --- | --- | --- |
| `background` | `images/backgrounds/3.jpg` | An image in `images/backgrounds/` (`.jpg`, `.jpeg` or `.png`) |
| `notificationTime` | `7000` | Milliseconds, 1000 to 60000 |
| `shortcuts` | none | Up to 50 entries; `path` must be an app folder like `apps/name/`; `x`, `y` in pixels |

A user without a settings file gets the defaults. Every change goes through `Storage::cleanSetting()`, and anything without a rule is refused.

### User files

`users/<name>/files/` holds what apps save with `writeFile()`. It's a flat folder: names can't contain slashes or start with a dot.
