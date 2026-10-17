# Data and configuration

All of Skylight's own files are JSON, read and written with Python's `json` module. The formats are the same as the [PHP-GTK2 version](../wiki4phpgtk/data-and-configuration.md)'s, so a `data/` folder, including its users and passwords, can be copied between the two.

## apps/apps.json

The apps in the home menu, in order:

```json
[
	"apps/terminal/",
	"apps/settings/",
	"apps/web-browser/"
]
```

Changes show up at the next login. Each folder needs an [`app.json`](app-manifest.md) and an `app.py`.

## images/

| Path | Contents |
| --- | --- |
| `images/backgrounds/N.jpg` | The desktop backgrounds, used when Pillow is installed |
| `images/backgrounds/N.gif` | The same pictures as GIFs (256 colours), used without Pillow |
| `images/home.gif`, `power.gif`, `skylight.gif` | Panel, power-button and window icons |

To add a background, put both a `.jpg` and a `.gif` with the same name in `images/backgrounds/`. The Settings app offers `1` to `6`; an app can choose any other with `setBackground`.

## data/

Created as needed; it must be writable by the user running Skylight.

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
{"admin": {"password": "pbkdf2-sha256$20000$a78f8b67b0b299249ca089a96d5e681a$cae50c295e420ce37bd3e53f83ef8b78a4b41b508aac97ab41c2c52c15f4f5fc"}}
```

Each password is stored as a **PBKDF2-SHA256** hash with 20,000 iterations, using `hashlib.pbkdf2_hmac` (Python 2.7.8 and later). The salt is 16 random bytes written as hex, and, as in the PHP version, that hex text is what goes into PBKDF2. Manage users with:

```
python add-user.py <username> <password>
```

To remove a user, delete their entry and their `users/<name>/` folder.

### Settings

`users/<name>/settings.json`:

```json
{"shortcuts": [{"y": 375, "path": "apps/settings/", "x": 255}], "notificationTime": 3000, "background": "images/backgrounds/1.jpg"}
```

| Setting | Default | Rule |
| --- | --- | --- |
| `background` | `images/backgrounds/3.jpg` | An image in `images/backgrounds/` (`.jpg`, `.jpeg` or `.png`; the `.gif` beside it is used without Pillow) |
| `notificationTime` | `7000` | Milliseconds, 1000 to 60000 |
| `shortcuts` | `[]` | Up to 50; each `path` must be an app folder like `apps/name/`; `x` and `y` are pixels from the desktop's top left |

A user without a settings file gets the defaults. Bad values in the file are ignored when it's read, and refused when apps try to save them.

### User files

`users/<name>/files/` holds what apps save with `writeFile()`, as UTF-8 text. It's a flat folder: names can't contain slashes or start with a dot.

## Environment

| Variable | Effect |
| --- | --- |
| `SKYLIGHT_HOME` | The folder holding `apps/`, `images/` and `data/`. Default: the folder `start-skylight.pyw` is in. |
