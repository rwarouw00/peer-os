# Data and configuration

All of Skylight's files use Java's standard **`.properties`** format (`key=value` lines), so no JSON library is needed. Java 8 reads and writes these files as ISO-8859-1, escaping other characters as `é`; Skylight's own code handles that, but keep it in mind when editing by hand.

## apps/apps.txt

The apps in the home menu, one folder per line, in order:

```
apps/terminal/
apps/settings/
apps/web-browser/
```

Changes show up at the next login.

## ui/

The desktop's own windows, as plain files read when they're shown. Edit them to change Skylight's look and layout without rebuilding it; see [Desktop internals](desktop-internals.md#extending-the-desktop) for what must stay.

| File | What it is |
| --- | --- |
| `login.fxml` | The login window |
| `desktop.fxml` | The desktop window: the panel and the desktop area |
| `menu.fxml` | The home menu: apps page, notifications page and footer |
| `notification.fxml` | One notification, in the menu and as a toast |
| `skylight.css` | Skylight's styles, added to every window |

`skylight.css` styles only Skylight's own parts; everything else is JavaFX's default Modena look.

| Style class | What it is |
| --- | --- |
| `.desktop-area` | The desktop and its background image |
| `.shortcut` | Desktop shortcut labels |
| `.home-menu`, `.home-menu-footer`, `.menu-app` | The home menu |
| `.notification`, `.notification-header`, `.toast`, `.close-button` | Notifications |
| `.terminal-code`, `.terminal-output` | The Terminal |
| `.heading` | Headings in Settings |

## data/

Created as needed; it must be writable by the user running Skylight.

```
data/
├── users.properties
└── users/
    └── <name>/
        ├── settings.properties
        └── files/
```

### users.properties

```properties
admin=pbkdf2-sha256$20000$zRZ58RUC7dTY6UeWKo348g\=\=$iylvxxZGOsMC0SEfDjgWmJGsFH317dH7i+KsS1PcQaM\=
```

One line per user: a **PBKDF2-SHA256** hash (Java 8's `PBKDF2WithHmacSHA256`) with a random 16-byte salt and 20,000 iterations. Manage users with:

```
java -cp skylight.jar skylight.AddUser <username> <password>
```

To remove a user, delete their line and their `users/<name>/` folder.

### Settings

`users/<name>/settings.properties`:

```properties
background=images/backgrounds/1.jpg
notificationTime=3000
shortcut.0.path=apps/settings/
shortcut.0.x=270
shortcut.0.y=375
```

| Setting | Default | Rule |
| --- | --- | --- |
| `background` | `images/backgrounds/3.jpg` | An image in `images/backgrounds/` (`.jpg`, `.jpeg` or `.png`) |
| `notificationTime` | `7000` | Milliseconds, 1000 to 60000 |
| `shortcut.N.path`, `.x`, `.y` | none | Up to 50; `path` must be an app folder like `apps/name/` |

A user without a settings file gets the defaults. Bad values in the file are ignored when it's read, and refused when apps try to save them.

### User files

`users/<name>/files/` holds what apps save with `writeFile()`, as UTF-8 text. It's a flat folder: names can't contain slashes or start with a dot.
