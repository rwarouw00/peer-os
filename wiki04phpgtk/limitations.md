# Limitations

## Behaviour

* **A fatal PHP error closes Skylight OS.** All apps share one process, so an error such as calling an undefined function, in any app or in the Terminal, ends the program. Warnings and exceptions in the Terminal are caught and shown.
* **No web pages inside Skylight.** PHP-GTK2 has no usable web view on Windows, so the Web Browser opens the system's browser.
* **Single user, single computer.** There's no server; each person runs their own copy.
* **Notifications** are kept only while Skylight is running.
* **High-resolution screens:** GTK+ 2 doesn't scale with Windows' display scaling, so it may look small or blurry on high-DPI screens.
* **Long-running code freezes the desktop.** Everything runs on one thread, so a slow app method holds up the whole desktop until it returns.

## Not carried over from the browser version

* Showing web pages in the Web Browser (see above).
* `App.openFile`, `App.parseXml`/`serializeXml`, `App.getQueryStringArgs` and `App.gotoLocation`, which only made sense for web pages. See [Compared with the browser version](comparison.md#the-app-api-side-by-side).

## How it was tested

There was no Windows machine available, so Skylight was tested on Linux, as close to the target as possible:

* **PHP 5.6.40** (the last PHP 5 release), built from source with the `json` and `hash` extensions;
* **PHP-GTK2** built from the project's final source code against **GTK+ 2.24**, with the PECL `cairo` extension it needs;
* a virtual X display with **metacity**, GNOME 2's window manager, and the **Clearlooks** theme.

The tests logged in through the real login window (including a wrong password), clicked through the panel, home menu, desktop shortcuts (open, drag, remove), toasts and window list with simulated mouse and keyboard input, and checked every app and App API method, the saved data and logout. The screenshots in this documentation come from those runs.

What wasn't tested:

* **Windows itself.** GTK+ 2 code is the same on both systems, but the theme, fonts, window decorations and file paths differ, and the Web Browser uses a different command (`start`) on Windows.
* **PHP 5.2 and PHP-GTK 2.0.1.** The code avoids PHP 5.3+ features, but it ran on PHP 5.6 and a later PHP-GTK2 build. PHP-GTK 2.0.1 binds an older GTK+ 2, so a method used here may be missing from your build; the most likely candidate, `set_tooltip_text` (GTK 2.12), is already checked before use.
