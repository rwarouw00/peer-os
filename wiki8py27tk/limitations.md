# Limitations

## Behaviour

* **Single user, single computer.** There's no server; each person runs their own copy.
* **Notifications** are kept only while Skylight is running.
* **Long-running code freezes the desktop.** Everything runs in Tk's event loop on one thread; slow work should go on a background thread (see [App API](app-api.md#threads)).
* **An endless loop in the Terminal** freezes Skylight, since its code runs in the same loop. Close Skylight from the Task Manager (or with Ctrl+C in the console it was started from).
* **Dialogs take all input** until they're answered (a Tk grab), so the rest of Skylight waits.
* **Pictures** are GIFs: icons have hard edges, and without Pillow the backgrounds have only 256 colours. Without Pillow, backgrounds are scaled in steps (such as 1.5× or 2×), so a little more of the picture may be cut off than in the other versions.
* **The home menu closes on clicks inside Skylight only.** Clicking another program, or a window's title bar, leaves it open; press Escape or click the desktop.
* **The Web Browser** can only hand addresses to the system's browser.
* **Apps are loaded once.** After changing an `app.py`, restart Skylight.

## Tied to Python 2.7

The code is written for Python 2.7, as requested: `print` statements, `unicode`, `Tkinter`, `imp`, `StringIO`. Python 3 would need the usual changes (module names, `print()`, `str`/`bytes`, `importlib` instead of `imp`) but the same design.

## How it was tested

Skylight was tested on Linux with the versions that matter:

* **Python 2.7.18**, built from source;
* **Tcl/Tk 8.5.19**, built from source, with Python's `_tkinter` compiled against it, because Windows' Python 2.7 bundles Tk 8.5 (8.5.15) rather than the Tk 8.6 of current Linux systems. With it, Tk refuses PNG files, just as it would on Windows;
* a virtual X display with **metacity** (GNOME's window manager).

The tests used the real login window (including a wrong password) and drove the desktop with simulated mouse and keyboard input:

* the home menu, opened and closed both ways;
* opening apps;
* the Terminal, including clicking **OK** on a dialog whose callback was a `lambda` typed in the Terminal;
* closing a toast;
* desktop shortcuts: double-clicking, dragging and removing;
* the window list: hiding, showing and closing;
* logging out and quitting;
* the Notes tutorial: saving, reopening, the unsaved-changes question (Cancel and OK) and Clear.

An automated driver also checked every App API method, Python errors in the Terminal (`NameError`, exceptions, syntax errors), variables kept between runs, settings validation, file storage (including non-English text and a refused `../` file name), apps that fail to open (missing folder, an import error, non-GIF icons), errors in event handlers, toasts expiring and logging out. It ran both without Pillow and with Pillow 6.2.2, the last version for Python 2.7. Password hashes were checked against the PHP-GTK2 version's code. The screenshots come from these runs, without Pillow.

What wasn't tested:

* **Windows itself**, including `run.bat`, the `vista` ttk theme and `state('zoomed')`. On Windows, buttons and other ttk widgets use the native look, so the screenshots' Linux "default" theme will look different.
* **The Web Browser opening a real browser**, since the test machine has none; the address check and the `webbrowser` call were exercised.
