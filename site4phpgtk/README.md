# Skylight OS for PHP-GTK2

Skylight OS rebuilt as a desktop program in PHP with PHP-GTK2 (GTK+ 2, the toolkit GNOME 2 was built on). The browser, web server and JavaScript are gone: one PHP process draws the desktop, and each app is a PHP class with its own real window. Documentation: [`../wiki4phpgtk`](../wiki4phpgtk/README.md).

## Quick start (Windows)

```
php tools\add-user.php admin choose-a-password
run.bat
```

`run.bat` starts `skylight.php` with `php-win.exe`, which has no console window. To see error messages, run `php skylight.php` from a command prompt instead.

## Layout

```
site4phpgtk/
├── skylight.php        entry point
├── run.bat             Windows launcher
├── skylight.gtkrc      colours for the panel, toasts and shortcuts
├── lib/
│   ├── Desktop.php     the desktop: login, panel, menu, notifications, windows
│   ├── App.php         base class for apps (the App API)
│   └── Storage.php     users, settings and files in data/
├── apps/               apps.json, and one folder per app (app.json, app.php, icons/)
├── images/             backgrounds, home and power icons
├── data/               users, settings and user files (created as needed)
└── tools/add-user.php  create users and set passwords
```
