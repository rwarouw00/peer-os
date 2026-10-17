# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names:

* **site3**: browser version: ES5 JavaScript, HTML and CSS with a PHP server ([`../wiki3`](../wiki3/README.md))
* **site4phpgtk**: desktop program in PHP 5.2 with PHP-GTK2 ([`../wiki4phpgtk`](../wiki4phpgtk/README.md))
* **site5jfx**: desktop program in Java 8 with JavaFX 8 ([`../wiki5jfx`](../wiki5jfx/README.md))
* **site6cs**: desktop program in C# 5 with Windows Forms (this one)

## Side by side

| | site3 (browser) | site4phpgtk (PHP-GTK2) | site5jfx (JavaFX) | site6cs (WinForms) |
| --- | --- | --- | --- | --- |
| Languages | ES5, HTML, CSS, PHP | PHP | Java, CSS | C# |
| Runs as | Web pages + web server | One PHP process | One Java process | One .exe |
| Needs installing | Web server with PHP | PHP-GTK2 | Java 8 with JavaFX | Nothing on Windows 11 |
| Look | Skylight's own CSS | GTK+ 2 theme | JavaFX Modena | Native Windows controls |
| App windows | Drawn in the page | Real windows | Real windows | Real windows |
| Apps are | HTML pages in iframes | PHP classes | Java classes (built in, or `app.jar`) | C# classes (built in, or `app.dll`) |
| Desktop ↔ apps | `postMessage` JSON | Method calls | Method calls | Method calls |
| Callbacks | Functions relayed by message | `array($obj, 'method')` | Lambdas | Lambdas (`Action`) |
| Dialogs | Non-modal | Non-modal | Non-modal | Modal (`MessageBox`) |
| Terminal runs | JavaScript (browser) | PHP (`eval`) | JavaScript (Nashorn) | C# (compiled by `csc.exe`) |
| Web Browser | Pages in a frame | Opens the system browser | Real pages (WebView) | Real pages (IE 11 engine) on Windows |
| Styles | `desktop.css`, `ui.css` | `skylight.gtkrc` | `skylight.css` | None: system look, a little custom drawing |
| Data format | JSON | JSON | `.properties` | JSON |
| Passwords | bcrypt | PBKDF2-SHA256 | PBKDF2-SHA256 | PBKDF2-SHA256 |
| Logins | PHP sessions | One user per running program | One user per running program | One user per running program |
| An error in one app | Breaks that window | A fatal error closes everything | Exceptions are reported; the rest carries on | Opening errors are reported; later ones get Windows Forms' Continue/Quit dialog |
| Size | ~1,390 lines, 51 files | ~1,150 lines of PHP | ~1,070 lines of Java + 80 CSS | ~1,400 lines of C# |

## The App API side by side

| site3 (JavaScript) | site5jfx (Java) | site6cs (C#) |
| --- | --- | --- |
| `App.openWindow(path)` | `openWindow(path)` | `OpenWindow(path)` |
| `App.closeWindow()` | `closeWindow()` | `CloseWindow()` |
| `App.openDialog(msg, function () {...})` | `openDialog(msg, () -> ...)` | `OpenDialog(msg, () => ...)` |
| `App.postNotification(h, b)` | `postNotification(h, b)` | `PostNotification(h, b)` |
| `App.getSettings()` / `App.saveSettings({...})` | `getSettings()` / `saveSettings(settings)` | `GetSettings()` / `SaveSettings(settings)` |
| `App.setBackground(p)`, `App.addShortcut(p)` | same names | `SetBackground(p)`, `AddShortcut(p)` |
| `App.readFile`, `writeFile`, `listFiles` | same names; `writeFile` returns `App.Result` | `ReadFile`, `WriteFile`, `ListFiles`; `WriteFile` returns `Result` |
| (none) | `build()`, `canClose()` | `Build()` returns a `Control`; `CanClose()` |

The PHP-GTK2 version uses the same names as site3, called on `$this`.

## What C# and Windows Forms brought

* **Nothing to install on Windows.** The compiler and the UI library are part of every Windows 11, so `build.bat` works on a fresh machine.
* **A native look.** Buttons, text boxes, menus and title bars are the system's own.
* **A compiled language in the Terminal**, with real compile errors and the same language as the apps.
* **Plug-in apps** as an `app.dll`, compiled against `Skylight.exe`.
* **Closest to the original layout of data:** JSON files, like site3 and site4phpgtk.

## What it costs

* **Windows first.** It runs on Linux and macOS with Mono, but Mono's Windows Forms looks dated and has no working web browser control.
* **C# 5 only**, since that's what Windows' built-in compiler understands (see the [primer](winforms-primer.md#c-5-the-language-cscexe-understands)).
* **An old web engine.** The `WebBrowser` control is Internet Explorer 11, which many sites no longer support.
* **Less styling.** Windows Forms has no stylesheets; anything beyond the system look is drawn in code.
