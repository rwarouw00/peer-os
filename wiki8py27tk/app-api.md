# App API

An app is a class in its folder's `app.py` that extends `App` ([`skylight/app.py`](../site8py27tk/skylight/app.py)) and adds its window's contents in `build()`:

```python
# -*- coding: utf-8 -*-
import ttk

from skylight.app import App


class HelloApp(App):

    def build(self):
        button = ttk.Button(self.window, text='Say hello',
                            command=lambda: self.postNotification('Hello', 'Hello, ' + self.getUsername()))
        button.pack(padx=40, pady=40)
```

The method names are the same as in the other versions (see [Compared with the other versions](comparison.md)); they keep the other versions' `camelCase` rather than Python's usual `snake_case`, so code reads the same everywhere. Settings and `writeFile` results are dictionaries, as in the PHP-GTK2 version.

## Lifecycle

| Method | When | What to do |
| --- | --- | --- |
| `build()` | Once, before the window is shown | Add widgets to `self.window` |
| `canClose()` | When the user closes the window | Return `True` to allow it, `False` to keep the window open (for example, to ask about unsaved work first) |

Skylight creates the app as `YourApp(desktop, appID, path, manifest, window, args)`. If you need your own `__init__`, take the same arguments and call `App.__init__(self, ...)` with them first.

## Attributes

| Attribute | Holds |
| --- | --- |
| `self.window` | Your `Tkinter.Toplevel` |
| `self.appID` | This window's ID |
| `self.path` | Your folder, e.g. `apps/notes/` |
| `self.manifest` | Your `app.json`, as a dictionary |
| `self.args` | The dictionary passed to `openWindow(path, args)`, or `{}` |
| `self.desktop` | The `Desktop`; prefer the methods below |

## Summary

| Method | Returns |
| --- | --- |
| [`openWindow(path, args=None)`](#openwindow) | AppID, or `None` |
| [`closeWindow(appID=None)`](#closewindow) | — |
| [`openDialog(message, onOK=None)`](#opendialog) | — |
| [`postNotification(header, body='')`](#postnotification) | — |
| [`getSettings()`](#getsettings) | `dict` (a copy) |
| [`saveSettings(changes)`](#savesettings) | `True` or `False` |
| [`setBackground(path)`](#setbackground) | `True` or `False` |
| [`addShortcut(path)`](#addshortcut) | — |
| [`getAppID()`](#identity) | string |
| [`getUsername()`](#identity) | string |
| [`appFile(name)`](#appfile) | full path |
| [`listFiles()`](#listfiles-readfile-writefile) | list of names |
| [`readFile(name)`](#listfiles-readfile-writefile) | text (`unicode`), or `None` |
| [`writeFile(name, contents)`](#listfiles-readfile-writefile) | `{'ok': True}` or `{'ok': False, 'error': ...}` |

## Windows

### openWindow

```python
self.openWindow('apps/settings/')
self.openWindow('apps/notes/', {'file': 'todo.txt'})   # NotesApp reads self.args['file']
```

Opens an app in a new window and returns its AppID. If the folder isn't a working app, or its `build()` raises an exception, the user gets a notification and `None` is returned.

### closeWindow

```python
self.closeWindow()         # this window
self.closeWindow(appID)    # another one
```

Closes without asking: `canClose()` isn't called.

## Dialogs and notifications

### openDialog

```python
self.openDialog('The file was saved.')                  # OK only
self.openDialog('Delete this note?', self.deleteNote)   # OK and Cancel
self.openDialog('Delete it?', lambda: self.delete('notes.txt'))
```

With `onOK`, the user sees a question with **OK** and **Cancel**, and `onOK()` is called only on OK. Any function works: a method, a `lambda`, or a function defined inside your method. Closing the dialog with its × counts as Cancel.

![A dialog](images/dialog.png)

The dialog is a small window over yours. It takes all input (a Tk *grab*) until it's answered, but `openDialog` returns straight away; the answer arrives later through `onOK`.

### postNotification

```python
self.postNotification('Download complete', 'report.pdf was saved.')
```

Shows a toast and adds the notification to the home menu. The body is optional.

## Settings and the desktop

### getSettings

```python
settings = self.getSettings()
settings['background']         # 'images/backgrounds/3.jpg'
settings['notificationTime']   # 7000 (milliseconds)
settings['shortcuts']          # [{'path': 'apps/terminal/', 'x': 20, 'y': 20}, ...]
```

Returns a **copy**: changing it has no effect until you save it.

### saveSettings

```python
self.saveSettings({'notificationTime': 3000})
```

Checks, saves and applies the settings you pass; the others keep their values. Returns `False`, after telling the user why, if a value was refused (see [Data and configuration](data-and-configuration.md#settings)).

### setBackground

```python
self.setBackground('images/backgrounds/2.jpg')
```

Background paths name the JPEG; without Pillow, Skylight shows the GIF copy beside it.

### addShortcut

```python
self.addShortcut('apps/notes/')
```

Adds a desktop shortcut (saved with the user's settings). Adding one that exists does nothing.

## Identity

`getAppID()` returns this window's ID; `getUsername()` the logged-in user.

## Files

### appFile

```python
logo = Tkinter.PhotoImage(file=self.appFile('images/logo.gif'))
self.logo = logo   # keep a reference, or Tkinter forgets the picture
```

The full path of a file in your app's own folder.

### listFiles, readFile, writeFile

```python
names = self.listFiles()                      # the user's own files
text = self.readFile('notes.txt')             # None if there's no such file
result = self.writeFile('notes.txt', text)
if not result['ok']:
    self.openDialog('Could not save: ' + result['error'])
```

Files are stored as UTF-8 in `data/users/<name>/files/`. `readFile` returns `unicode`; `writeFile` takes `unicode` or a UTF-8 `str`. Names may use letters, digits, spaces, `-`, `_` and `.` (up to 100 characters, not starting with `.`), so they can't point outside the user's folder.

## Errors

If `app.py` can't be loaded, or the constructor or `build()` raises an exception, the window isn't opened and the user gets a "Cannot open app" notification with Python's message. An exception in one of your event handlers later on becomes an "Error in an app" notification, and Skylight carries on; the full traceback goes to the console, if there is one.

## Threads

Tkinter isn't thread-safe: create widgets and call the App API only from the thread Skylight runs on (the one that calls your event handlers). For slow work on another thread, put results in a `Queue.Queue` and check it with `self.window.after(100, ...)`.
