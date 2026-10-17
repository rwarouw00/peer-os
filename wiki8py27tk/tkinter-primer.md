# Tkinter primer

A short guide to the Tkinter and Python 2.7 ideas Skylight uses. Python 2.7's documentation for `Tkinter` and `ttk`, and the Tk manual pages, cover the rest.

## Python 2.7 names

| Python 2.7 | Python 3 |
| --- | --- |
| `import Tkinter as tk` | `import tkinter as tk` |
| `import ttk` | `from tkinter import ttk` |
| `import tkFont`, `tkMessageBox` | `tkinter.font`, `tkinter.messagebox` |
| `print 'hi'` | `print('hi')` |
| `unicode` and `str` (bytes) | `str` and `bytes` |

Tkinter hands back typed text as `unicode`. Skylight reads and writes files as UTF-8 with `io.open`, and each source file starts with `# -*- coding: utf-8 -*-` so it may contain non-English text.

## Widgets and geometry managers

A window is a `Toplevel` (the hidden main window is `Tk`). Widgets are created with their parent as the first argument, then placed by a **geometry manager**:

| Manager | Use | In Skylight |
| --- | --- | --- |
| `pack(side=..., fill=..., expand=...)` | Stack along an edge | Toolbars, the panel, most windows |
| `grid(row=..., column=...)` | Rows and columns | The login form, the home menu's app icons |
| `place(x=..., relwidth=...)` | Exact positions | The two home-menu pages, stacked on each other |

Don't mix `pack` and `grid` in the same parent.

| Need | Tkinter |
| --- | --- |
| Themed buttons, labels, entries, frames | `ttk.Button`, `ttk.Label`, `ttk.Entry`, `ttk.Frame` (native look on Windows) |
| Multi-line text | `tk.Text` (`get('1.0', 'end-1c')` for its contents) |
| Numbers | `tk.Spinbox(from_=1, to=60)` |
| A row of choices shown as pictures | `tk.Radiobutton(..., indicatoron=False)` |
| Toggle buttons | `ttk.Checkbutton(..., style='Toolbutton')` (the window list) |
| Pop-up menus | `tk.Menu(tearoff=False)` and `tk_popup(x, y)` |
| Drawing, and things to drag around | `tk.Canvas` (the desktop: background picture and shortcuts) |
| Two resizable panes | `tk.PanedWindow` (the Terminal) |

## Events and callbacks

```python
ttk.Button(frame, text='Save', command=self.save)            # a button's action
entry.bind('<Return>', lambda event: self.go())             # key and mouse events get an event object
canvas.tag_bind('shortcut1', '<B1-Motion>', move)            # events on canvas items with a tag
window.protocol('WM_DELETE_WINDOW', self.onClose)            # the window's close button
self.window.after(1000, self.tick)                           # run something later (timers)
```

A `lambda` written in a loop needs the loop variable as a default argument (`lambda path=path: self.open(path)`), or every callback sees the last value.

## Pictures

`Tkinter.PhotoImage` reads **GIF** and PPM files; Tk 8.6 adds PNG, but the Tk 8.5 bundled with Python 2.7 on Windows doesn't have it. Tk can scale a picture only by whole numbers, with `zoom(n)` and `subsample(n)`; Skylight combines them for fractions (`zoom(3).subsample(2)` is 1.5 times the size).

Keep a reference to every `PhotoImage` (in a list or an attribute). When the Python object is garbage-collected, the picture disappears from the screen.

The Pillow library (`from PIL import Image, ImageTk`) reads JPEG and PNG and scales smoothly; `ImageTk.PhotoImage` turns its pictures into Tk ones.

## Windows that aren't normal windows

| Need | Tkinter |
| --- | --- |
| Keep a window in front of another | `window.transient(owner)` |
| No title bar (the home menu, toasts) | `window.overrideredirect(True)` |
| Stay above other windows | `window.attributes('-topmost', True)` |
| Modal: take all input until closed | `window.grab_set()` (the dialogs) |
| Hide and show | `withdraw()`, `deiconify()` |
| Maximize | `state('zoomed')` on Windows and macOS, `attributes('-zoomed', True)` on Linux |

## Errors in callbacks

Tkinter catches an exception raised in a callback and passes it to `Tk.report_callback_exception`, which normally prints it. Skylight replaces that method to show a notification instead, so a mistake in one app doesn't go unnoticed, or stop the others, under `pythonw` where there's no console.
