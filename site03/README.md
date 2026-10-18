# Skylight OS (site03)

A restructured version of the Skylight OS Arctic Eagle code in [`../siteroot`](../siteroot). It keeps the same desktop, apps and `App` API names, written in plain **ES5 JavaScript** and **PHP 5.3.7+**, with the security problems and bugs found in the review fixed. Documentation: [`../wiki03`](../wiki03/README.md).

## Quick start

```
cd site03
php tools/add-user.php admin choose-a-password
cd public
php -S localhost:8000
```

Open <http://localhost:8000/> and log in.

## Layout

```
site03/
├── public/        the web root: point the web server here
│   ├── index.html       desktop page (markup only)
│   ├── api/             login.php, logout.php, settings.php, files.php
│   ├── apps/            terminal/, settings/, web-browser/
│   └── system/          js/ (core, desktop, app), css/, dialog/, images/, apps.json
├── lib/skylight.php     shared PHP, not reachable from the web
├── data/                users, settings and files, not reachable from the web
└── tools/add-user.php   create users and set passwords
```

The web server needs write access to `data/`.
