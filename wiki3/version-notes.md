# Version notes

The code in `siteroot` is **Skylight OS Arctic Eagle v1.00.00** (copyright 2013). The [SourceForge wiki](https://sourceforge.net/p/skylightos/wiki/), exported to [`../wiki`](../wiki/README.md), was written in 2014 and documents **prealpha A2 and later**. This page lists where the two disagree, so readers of the old wiki know what to expect.

## Documented on the wiki, missing here

| Feature | Wiki says | This release |
| --- | --- | --- |
| Permissions | Apps request `CHANGE_WALLPAPER`, `OPEN_WINDOWS` and others in `appData.json` | No permission system; every app can do everything |
| Themes | Installable packages, with "default" and "Chrome" themes | Fixed look; only the background can change |
| `App.speak` | Text-to-speech through Google | Not present |
| `App.registerError` | Reports serious errors | Not present |
| `App.selectFileForOpen`, Open File dialog | File picker for opening | Not present |
| `App.selectFileForSave` | File picker for saving | Dialog started, not usable by apps |
| `App.loadTheme` | Loads a theme | Not present |
| Dialog button labels | `App.openDialog(text, btnLabels, onOK)` | `App.openDialog(text, onOK)`; labels fixed |
| Default and pulse buttons | `class="default"`, `class="pulse"` | Classes not defined in `ui.css` |
| Packaged apps | `.sapp` files | Not supported |
| `appData.json` `UI` and `permissions` | Window opacity, permissions | Ignored |

## Here, but not on the wiki

* [`App.addShortcut`](app-api.md#appaddshortcut) and desktop shortcuts
* [`App.setBackground`](app-api.md#appsetbackground) (listed on the wiki, never documented)
* The `System` object and the message protocol ([System API](system-api.md), [Architecture](architecture.md))
* Login, user settings and the configuration files ([Configuration](configuration.md))
* Context menus
* The built-in apps ([User guide](user-guide.md#built-in-apps))
* The server scripts the code depends on ([Server API](server-api.md))

## Details the wiki gets wrong for this release

* `App.getQueryStringArgs` returns an object, not an array.
* Toasts truncate at 20 header and 110 body characters, not 30 and 100.
* `App.closeWindow` can close any window, not only your own.
* The tutorial's paths `../../../js/app.js` and `../../../css/ui.css` should be `../../../system/js/app.js` and `../../../system/css/ui.css`, and it doesn't mention `system/apps.xml`.
* `App.openFile` paths are relative to the app's page, so the wiki's example `users/admin/Documents/test.txt` would look inside the app's folder.
