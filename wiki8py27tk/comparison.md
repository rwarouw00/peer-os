# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names:

* **site3**: browser version: ES5 JavaScript, HTML and CSS with a PHP server ([`../wiki3`](../wiki3/README.md))
* **site4phpgtk**: desktop program in PHP 5.2 with PHP-GTK2 ([`../wiki4phpgtk`](../wiki4phpgtk/README.md))
* **site5jfx**: desktop program in Java 8 with JavaFX 8 ([`../wiki5jfx`](../wiki5jfx/README.md))
* **site6cs**: desktop program in C# 5 with Windows Forms ([`../wiki6cs`](../wiki6cs/README.md))
* **site7java7**: desktop program in Java 7 with JavaFX 2.2 ([`../wiki7java7`](../wiki7java7/README.md))
* **site8py27tk**: desktop program in Python 2.7 with Tkinter (this one)

## Next to the PHP-GTK2 version

The two dynamic-language desktop versions are built the same way: no compiler, apps are source files in their folders, settings are dictionaries (arrays in PHP), and the data files are interchangeable.

| | site4phpgtk (PHP-GTK2) | site8py27tk (Tkinter) |
| --- | --- | --- |
| Needs installing | A PHP-GTK2 runtime (32-bit Windows builds) | Python 2.7, which includes Tkinter |
| App file | `apps/<name>/app.php` | `apps/<name>/app.py` |
| Calling the API | `$this->postNotification(...)` | `self.postNotification(...)` |
| Callbacks | `array($this, 'method')` | Any function: `self.method`, `lambda: ...` |
| Terminal runs | PHP (`eval`); a fatal error ends Skylight | Python; every error is caught and shown |
| An error in an app | A fatal error closes everything | Reported as a notification; the rest carries on |
| Desktop drawing | `GtkFixed` with widgets | `Canvas` with items |
| Pictures | PNG and JPEG (GdkPixbuf) | GIF (Tk 8.5), or JPEG with Pillow |
| Look | GTK+ 2 theme, plus `skylight.gtkrc` | ttk's theme: the native look on Windows |
| Data files | JSON | The same JSON, the same password hashes |
| Size | ~1,150 lines of PHP | ~900 lines of Python |

## All versions side by side

| | site3 (browser) | site4phpgtk | site5jfx | site6cs | site7java7 | site8py27tk |
| --- | --- | --- | --- | --- | --- | --- |
| Language | ES5 + PHP | PHP 5.2 | Java 8 | C# 5 | Java 7 | Python 2.7 |
| UI | HTML/CSS | GTK+ 2 | JavaFX 8 | WinForms | JavaFX 2.2 | Tkinter |
| Build step | No | No | Yes | Yes | Yes | No |
| Apps are | Pages in iframes | PHP classes | Java classes | C# classes | Java classes | Python classes |
| Terminal runs | JavaScript | PHP | JavaScript | C# | JavaScript | Python |
| Web Browser | Pages in a frame | System browser | WebView | IE 11 engine | WebView | System browser |
| Data format | JSON | JSON | `.properties` | JSON | `.properties` | JSON |

The App API keeps the same method names everywhere; in Python they're called on `self` (or `app` in the Terminal): `self.openWindow(path)`, `self.openDialog(message, onOK)`, `self.getSettings()`, `self.saveSettings({'notificationTime': 3000})`, `self.readFile(name)`, `self.writeFile(name, text)`.

## What Python and Tkinter brought

* **Nothing extra to install** beyond Python 2.7, and nothing to compile.
* **Errors are contained.** A mistake in an app becomes a notification instead of ending Skylight, which was the PHP-GTK2 version's weak point.
* **The smallest code** of the desktop versions, thanks to Python's dictionaries, functions as callbacks, and Tk's canvas.
* **The Terminal speaks the same language as the apps**, and keeps its variables between runs.

## What it costs

* **Pictures.** Tk 8.5 reads only GIF and PPM, so icons are GIFs with hard edges, and backgrounds are 256-colour GIF copies unless Pillow is installed.
* **No web page widget**, as in the PHP-GTK2 version.
* **Python 2.7 is out of support** (since January 2020); see [Security](security.md).
