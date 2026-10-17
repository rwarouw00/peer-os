# Data and configuration

All of Skylight's own files are **JSON**, read and written with the .NET Framework's built-in `JavaScriptSerializer`, so no JSON library is needed. Skylight writes them as UTF-8 without a byte-order mark.

## apps\apps.json

The apps in the home menu, in order:

```json
[
	"apps/terminal/",
	"apps/settings/",
	"apps/web-browser/"
]
```

Changes show up at the next login. Each folder needs an [`app.json`](app-manifest.md).

## data\

Created as needed; it must be writable by the user running Skylight. If Skylight is in a protected folder such as `C:\Program Files`, it can't write there: keep it in your own folders, or set `SKYLIGHT_HOME` to a writable copy.

```
data\
├── users.json
└── users\
    └── <name>\
        ├── settings.json
        └── files\
```

### users.json

```json
{"admin":"pbkdf2-sha256$20000$rxIYso1vpr6Ruzq4AFcNaQ==$XIF2UDnPfdQ3Yb6HJITU8AF7jaLlBP5HJRKM9DMjRCk="}
```

One entry per user: a **PBKDF2-SHA256** hash with a random 16-byte salt and 20,000 iterations, in base64. The .NET Framework's own PBKDF2 class (`Rfc2898DeriveBytes`) only offers SHA-1 before version 4.7.2, so `Storage` builds PBKDF2 from `HMACSHA256` itself; the result is the standard algorithm and matches other PBKDF2-SHA256 implementations. Manage users with:

```
AddUser.exe <username> <password>
```

To remove a user, delete their entry and their `users\<name>\` folder.

### Settings

`users\<name>\settings.json`:

```json
{"Background":"images/backgrounds/1.jpg","NotificationTime":3000,"Shortcuts":[{"Path":"apps/settings/","X":255,"Y":385}]}
```

The keys are the property names of the `Settings` class, so they start with capitals.

| Setting | Default | Rule |
| --- | --- | --- |
| `Background` | `images/backgrounds/3.jpg` | An image in `images/backgrounds/` (`.jpg`, `.jpeg` or `.png`) |
| `NotificationTime` | `7000` | Milliseconds, 1000 to 60000 |
| `Shortcuts` | `[]` | Up to 50; each `Path` must be an app folder like `apps/name/`; `X` and `Y` are pixels from the desktop's top left |

A user without a settings file gets the defaults, and so does a user whose file is damaged or holds bad values. Apps that try to save bad values are refused, and the user is told why.

### User files

`users\<name>\files\` holds what apps save with `WriteFile()`, as UTF-8 text. It's a flat folder: names can't contain slashes or start with a dot.

## Environment

| Variable | Effect |
| --- | --- |
| `SKYLIGHT_HOME` | The folder holding `apps\`, `images\` and `data\`. Default: the folder `Skylight.exe` is in. |
