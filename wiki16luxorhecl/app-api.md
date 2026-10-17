# App API

An app's Hecl script reaches Skylight through the variable **`app`**, a Java object ([`App.java`](../site16luxorhecl/src/skylight/App.java)) with one instance per window. Its methods are called like commands: `$app postNotification Hello`. The method names match the other versions' App APIs (see [Compared with the other versions](comparison.md)).

```tcl
$app postNotification Hello "Hello, [$app getUsername]"
```

Values come back as Hecl values: text, numbers, `1` or `0` for true or false, and Hecl lists for Java lists. A few methods return Java objects (`getSettings`, `writeFile`), whose fields `-field` reads and sets: `$result -field ok`. **Callbacks** (for `openDialog` and `setCanClose`) are Hecl, usually a proc's name, run in the app's own interpreter.

## Summary

| Method | Returns |
| --- | --- |
| [`openWindow path ?args?`](#openwindow) | AppID, or empty |
| [`closeWindow ?appID?`](#closewindow) | — |
| [`setCanClose script`](#setcanclose) | — |
| [`openDialog message ?onOK?`](#opendialog) | — |
| [`postNotification header ?body?`](#postnotification) | — |
| [`getSettings`](#getsettings) | a `Settings` object (a copy) |
| [`saveSettings settings`](#savesettings) | `1` or `0` |
| [`setBackground imagePath`](#setbackground) | `1` or `0` |
| [`addShortcut appPath`](#addshortcut) | — |
| [`getAppID`, `getUsername`, `getArgs`, `getWindow`](#information-about-the-app) | see below |
| [`appFile name`](#appfile) | a `java.nio.file.Path` |
| [`listFiles`](#listfiles-readfile-writefile) | a Hecl list of names |
| [`readFile name`](#listfiles-readfile-writefile) | text; empty if there's no such file |
| [`writeFile name contents`](#listfiles-readfile-writefile) | a result with the fields `ok` and `error` |

## The window's controls

Besides `app`, every element with an `id` in the app's `.xul` file is a variable holding its Swing component, so scripts use Swing's own methods:

| XUL | Variable is a | For example |
| --- | --- | --- |
| `<button id="save" .../>` | `JButton` | `$save setEnabled 0` |
| `<label id="status" .../>` | `JLabel` | `$status setText Saved` |
| `<textbox id="address" .../>` | `JTextField` | `$address getText` |
| `<password id="pin" .../>` | `JPasswordField` | `$pin getPassword` |
| `<textarea id="note" .../>` | `JTextArea` | `$note append more`, `$note setLineWrap 1` |
| `<checkbox id="bold" .../>` | `JCheckBox` | `$bold isSelected` |
| `<iframe id="page" .../>` | `JEditorPane` | `$page setPage http://example.com/` |
| `<gadget id="chart"/>` | an empty `JPanel` (with `BorderLayout`) | `$chart add [JScrollPane -new [list [JTree -new [list]]]]`, after `java javax.swing.JScrollPane JScrollPane` and `java javax.swing.JTree JTree` |

Method names aren't case-sensitive in Hecl (`$status settext Saved` works too).

## Events: bind

Hecl has no way of its own to handle Java events, so Skylight adds one command, **`bind`**:

```tcl
bind $note keyTyped {set changed 1}
bind $address actionPerformed go                     ;# Enter in a text box
bind $page hyperlinkUpdate {puts [[$event getURL] toString]}
```

`bind object method script` finds the object's `add...Listener` method whose listener has a method of that name (`keyTyped` is in `KeyListener`, so `addKeyListener`), and adds a listener that runs the script each time that method is called. The script runs at the top level of the app's interpreter, with the variable **`event`** holding the event object. The listener's other methods do nothing. `bind` returns the listener, which the object's `remove...Listener` method takes away again.

An error in a bound script is shown as an "Error in *app*" notification, like an error in a command. If the object has no such listener, `bind` itself fails: `bind: javax.swing.JTextArea has no listener with the method nothingHappened`.

A **command** (`command="save"` on a button) runs the script's proc of that name; `command="choose 3"` runs `choose 3`.

## Windows

### openWindow

```tcl
$app openWindow apps/settings/
$app openWindow apps/notes/ {file todo.txt}     ;# the Notes window reads [$app getArgs]
```

Opens an app in a new window and returns its AppID. `args` is a Hecl list of names and values. If the folder isn't a working app, the user gets a notification and the result is empty.

### closeWindow

```tcl
$app closeWindow           ;# this window
$app closeWindow $appID    ;# another one
```

Closes without asking: the `setCanClose` script isn't run.

### setCanClose

```tcl
proc canClose {} {
    global changed
    return [not $changed]     ;# 0 keeps the window open
}
$app setCanClose canClose
```

Skylight runs the script when the user closes the window (its close button, Alt+F4, or **Close** in the window list). If it returns `0`, the window stays open. Without one, the window always closes.

## Dialogs and notifications

### openDialog

```tcl
$app openDialog "The file was saved."              ;# OK only
proc remove {} {
    global app
    $app writeFile notes.txt ""
}
$app openDialog "Delete this note?" remove         ;# OK and Cancel
```

With a script, the user sees a question with **OK** and **Cancel**, and the script runs only on OK. The dialog is a Swing `JOptionPane` belonging to your window, but not modal: the rest of the desktop keeps working. Swing shows a message that starts with `<html>` as HTML, and any other message as plain text.

![A dialog](images/dialog.png)

### postNotification

```tcl
$app postNotification "Download complete" "report.pdf was saved."
```

Shows a toast in the top-right corner and adds the notification to the home menu. The body is optional.

## Settings and the desktop

### getSettings

```tcl
set settings [$app getSettings]
$settings -field background          ;# images/backgrounds/3.jpg
$settings -field notificationTime    ;# 7000 (milliseconds)
foreach shortcut [$settings -field shortcuts] {
    puts "[$shortcut -field path] at [$shortcut -field x],[$shortcut -field y]"
}
```

Returns a **copy**: changing it has no effect until you save it.

### saveSettings

```tcl
set settings [$app getSettings]
$settings -field notificationTime 3000
$app saveSettings $settings
```

Checks, saves and applies the settings. Returns `0`, after telling the user why, if they were refused (see [Data and configuration](data-and-configuration.md#settings)).

### setBackground

```tcl
$app setBackground images/backgrounds/2.jpg
```

### addShortcut

```tcl
$app addShortcut apps/notes/
```

Adds a desktop shortcut (saved with the user's settings). Adding one that exists does nothing; a path that isn't an app gives a notification.

## Information about the app

| Method | Returns |
| --- | --- |
| `$app getAppID` | This window's ID |
| `$app getUsername` | The logged-in user |
| `$app getArgs` | The values passed to `openWindow`, as a list of names and values; empty if none. `foreach {name value} [$app getArgs] {...}` goes through them, and `set args [hash [$app getArgs]]` makes a Hecl hash, so that `hget $args file` gives one |
| `$app getWindow` | This app's window, a Swing `JDialog` |

## Files

### appFile

```tcl
set url [[[$app appFile start.html] toUri] toString]
```

A file in your app's own folder, as a `java.nio.file.Path`. The Web Browser uses it for its start page.

### listFiles, readFile, writeFile

```tcl
set names [$app listFiles]                  ;# the user's own files, as a Hecl list
set text [$app readFile notes.txt]          ;# empty if there's no such file
set result [$app writeFile notes.txt $text]
if {not [$result -field ok]} {
    $app openDialog "Could not save: [$result -field error]"
}
```

Files are stored as UTF-8 in `data/users/<name>/files/`. Names may use letters, digits, spaces, `-`, `_` and `.` (up to 100 characters, not starting with `.`), so they can't point outside the user's folder.

## Threads

Swing runs scripts, commands and bound events on its event thread, and the App API must be called there too. A long-running proc freezes the desktop until it returns.

Each Hecl interpreter also starts a thread of its own, for Hecl's timers (`after`) and waits (`twait`). Skylight removes those commands, since they would run Hecl away from Swing's thread, so the thread only idles; Skylight stops it when the window closes.
