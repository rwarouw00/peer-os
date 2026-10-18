# Compared with the browser version

The PHP-GTK2 version keeps the design of the browser version ([`../site3`](../site3), documented in [`../wiki3`](../wiki3/README.md)) but rebuilds every part in PHP.

## What maps to what

| Browser version (site3) | PHP-GTK2 version |
| --- | --- |
| Browser + web server + PHP | One PHP-GTK2 program |
| `index.html` + `desktop.js` (`Desktop` object) | `lib/Desktop.php` (`Desktop` class) |
| `app.js` (`App` object) loaded in each iframe | `lib/App.php`, a base class each app extends |
| `core.js` + `api/*.php` + `lib/skylight.php` | `lib/Storage.php`, called directly |
| Apps as HTML pages in iframes | Apps as PHP classes in `apps/<name>/app.php` |
| Windows drawn by Skylight in the page | Real windows, managed by the operating system |
| `postMessage` JSON messages, checked for origin and sender | Ordinary method calls |
| PHP sessions and cookies | Not needed: one user per running program |
| bcrypt (`crypt()`, PHP 5.3.7+) | PBKDF2-SHA256 (`hash_hmac()`, PHP 5.2+) |
| `desktop.css` + `ui.css` | GTK theme + `skylight.gtkrc` |
| `system/apps.json` | `apps/apps.json` |
| `app.json`: icons relative to the app folder | Same, plus `class` |
| Terminal runs JavaScript | Terminal runs PHP |
| Web Browser shows pages in a frame | Web Browser opens the system's default browser |

## The App API side by side

| Browser (JavaScript) | PHP-GTK2 (PHP, inside your app class) |
| --- | --- |
| `App.openWindow('apps/notes/')` | `$this->openWindow('apps/notes/')` |
| `App.closeWindow()` | `$this->closeWindow()` |
| `App.openDialog('Sure?', function () { ... })` | `$this->openDialog('Sure?', array($this, 'method'))` |
| `App.postNotification('Hi', 'There')` | `$this->postNotification('Hi', 'There')` |
| `App.getSettings()` / `App.saveSettings({...})` | `$this->getSettings()` / `$this->saveSettings(array(...))` |
| `App.setBackground(path)`, `App.addShortcut(path)` | `$this->setBackground($path)`, `$this->addShortcut($path)` |
| `App.getAppID()`, `App.getUsername()` | `$this->getAppID()`, `$this->getUsername()` |
| `App.readFile(name)`, `App.writeFile(name, text)`, `App.listFiles()` | `$this->readFile($name)`, `$this->writeFile($name, $text)`, `$this->listFiles()` |
| `App.openFile(url)` | `file_get_contents($this->appFile($name))` |
| `App.parseXml`, `App.serializeXml` | PHP's own XML classes, if your PHP build includes them |
| `App.getQueryStringArgs()`, `App.gotoLocation()` | `$this->args`; no pages to move between |
| (none) | `build()` and `canClose()` |

## What got simpler

* **No security boundary to defend between the desktop and apps.** The browser version needed origin and sender checks, text-only rendering and request headers to keep web pages out. Here there is no web, so none of that code exists.
* **No window manager to write.** Dragging, stacking, maximizing and minimizing come from the operating system, and windows can be resized.
* **No client/server split.** No API endpoints, no JSON replies, no sessions; settings and files are read and written directly.
* **Real callbacks.** Dialog callbacks are ordinary PHP callbacks instead of a relay of messages between windows.

## What got harder or was lost

* **More code to build the interface.** Creating GTK widgets in PHP takes more lines than writing HTML: `Desktop.php` is about 630 lines, against 430 for `desktop.js` plus 44 for `index.html`.
* **No web pages inside Skylight.** The Web Browser can only hand addresses to the real browser.
* **One mistake can stop everything.** All apps share one PHP process, so a fatal error in any app (or in the Terminal) closes Skylight OS.
* **Single user, single machine.** There's no server, so no shared data or remote access.

## Size

| | site3 (browser) | site4phpgtk |
| --- | --- | --- |
| Languages | ES5 JavaScript, HTML, CSS, PHP | PHP |
| Code files | 51 | 35 (including images) |
| Non-blank lines of code | 1,390 | about 1,200 (1,149 of PHP) |

## Notes in every version

The same Notes app, written for all fourteen versions, is compared side by side in [Notes, in every version](../wikicompare/README.md).
