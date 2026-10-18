# Compared with the other versions

Skylight OS now exists in three restructured forms, all with the same design and App API names:

* **site03**: browser version: ES5 JavaScript, HTML and CSS with a PHP server ([`../wiki03`](../wiki03/README.md))
* **site04phpgtk**: desktop program in PHP 5.2 with PHP-GTK2 ([`../wiki04phpgtk`](../wiki04phpgtk/README.md))
* **site05jfx**: desktop program in Java 8 with JavaFX 8 (this one)

## Side by side

| | site03 (browser) | site04phpgtk (PHP-GTK2) | site05jfx (JavaFX) |
| --- | --- | --- | --- |
| Languages | ES5, HTML, CSS, PHP | PHP | Java, CSS |
| Runs as | Web pages + web server | One PHP process | One Java process |
| Look | Skylight's own CSS | GTK+ 2 theme | JavaFX Modena |
| App windows | Drawn in the page | Real windows | Real windows |
| Apps are | HTML pages in iframes | PHP classes | Java classes (built in, or `app.jar`) |
| Desktop ↔ apps | `postMessage` JSON | Method calls | Method calls |
| Callbacks | Functions relayed by message | `array($obj, 'method')` | Lambdas |
| Terminal runs | JavaScript (browser) | PHP (`eval`) | JavaScript (Nashorn) |
| Web Browser | Pages in a frame | Opens the system browser | Real pages (WebView) |
| Styles | `desktop.css`, `ui.css` | `skylight.gtkrc` | `skylight.css` |
| Data format | JSON | JSON | `.properties` |
| Passwords | bcrypt | PBKDF2-SHA256 | PBKDF2-SHA256 |
| Logins | PHP sessions | One user per running program | One user per running program |
| An error in one app | Breaks that window | A fatal error closes everything | Exceptions are reported; the rest carries on |
| Size | ~1,390 lines, 51 files | ~1,150 lines of PHP | ~1,070 lines of Java + 80 CSS |

## The App API side by side

| site03 (JavaScript) | site04phpgtk (PHP) | site05jfx (Java) |
| --- | --- | --- |
| `App.openWindow(path)` | `$this->openWindow($path)` | `openWindow(path)` |
| `App.closeWindow()` | `$this->closeWindow()` | `closeWindow()` |
| `App.openDialog(msg, function () {...})` | `$this->openDialog($msg, array($this, 'm'))` | `openDialog(msg, () -> ...)` |
| `App.postNotification(h, b)` | `$this->postNotification($h, $b)` | `postNotification(h, b)` |
| `App.getSettings()` / `App.saveSettings({...})` | `$this->getSettings()` / `$this->saveSettings(array(...))` | `getSettings()` / `saveSettings(settings)` |
| `App.setBackground(p)`, `App.addShortcut(p)` | same names | same names |
| `App.readFile`, `writeFile`, `listFiles` | same names | same names; `writeFile` returns `App.Result` |
| (none) | `build()`, `canClose()` | `build()` returns the root node; `canClose()` |

## What JavaFX brought

* **The Web Browser works properly again**, with a real engine inside the window.
* **JavaScript in the Terminal**, through Nashorn, with JavaScript functions usable as callbacks.
* **Errors are contained.** An exception in an app's event handler is reported by JavaFX and the rest of Skylight carries on; a missing app class becomes a notification.
* **Real CSS** for styling, close to the browser version's.
* **Plug-in apps:** an app can ship as an `app.jar` without rebuilding Skylight.

## What it costs

* **A larger runtime.** Users need a Java 8 with JavaFX (around 200 MB), and not every Java 8 includes JavaFX.
* **A build step.** Java must be compiled; the browser and PHP versions run their source directly.
* **Java 8 limits.** Nashorn and the bundled JavaFX were removed from later Java versions, so this code is tied to Java 8 unless it's ported (see [Limitations](limitations.md)).

## Notes in every version

The same Notes app, written for all fourteen versions, is compared side by side in [Notes, in every version](../wikicompare/README.md).
