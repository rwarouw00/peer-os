# App API

An app's Tcl script reaches Skylight through the variable **`app`**, a Java object ([`App.java`](../site15luxorplus/src/skylight/App.java)) with one instance per window. Its methods are called like commands: `$app postNotification Hello`. The method names match the other versions' App APIs (see [Compared with the other versions](comparison.md)).

```tcl
$app postNotification Hello "Hello, [$app getUsername]"
```

Values come back as Tcl values: text, numbers, `1` or `0` for true or false, and Tcl lists for lists. A few methods return Java objects (`getSettings`, `writeFile`), whose fields `java::field` reads and sets. **Callbacks** (for `openDialog` and `setCanClose`) are Tcl, usually a proc's name, run in the app's own interpreter.

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
| [`listFiles`](#listfiles-readfile-writefile) | a Tcl list of names |
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
| `<gadget id="chart"/>` | an empty `JPanel` (with `BorderLayout`) | `$chart add [java::new javax.swing.JScrollPane [java::new javax.swing.JTree]]` |

Swing events are handled with `java::bind`, which runs Tcl when the event happens; `[java::event]` is the event object:

```tcl
java::bind $note keyTyped {set changed 1}
java::bind $address actionPerformed go                     ;# Enter in a text box
java::bind $page hyperlinkUpdate {puts [[[java::event] getURL] toString]}
```

A **command** (`command="save"` on a button) runs the script's proc of that name; `command="choose 3"` runs `choose 3`.

## Windows

### openWindow

```tcl
$app openWindow apps/settings/
$app openWindow apps/notes/ {file todo.txt}     ;# the Notes window reads [$app getArgs]
```

Opens an app in a new window and returns its AppID. `args` is a Tcl list of names and values. If the folder isn't a working app, the user gets a notification and the result is empty.

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
    return [expr {!$changed}]     ;# 0 keeps the window open
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
java::field $settings background          ;# images/backgrounds/3.jpg
java::field $settings notificationTime    ;# 7000 (milliseconds)
java::field $settings shortcuts           ;# a Java list of shortcuts, each with path, x and y
```

Returns a **copy**: changing it has no effect until you save it.

### saveSettings

```tcl
set settings [$app getSettings]
java::field $settings notificationTime 3000
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
| `$app getArgs` | The values passed to `openWindow`, as a Tcl list of names and values (`array set args [$app getArgs]`); empty if none |
| `$app getWindow` | This app's window, a Swing `JDialog` |

## Files

### appFile

```tcl
set url [[[$app appFile start.html] toUri] toString]
```

A file in your app's own folder, as a `java.nio.file.Path`. The Web Browser uses it for its start page.

### listFiles, readFile, writeFile

```tcl
set names [$app listFiles]                  ;# the user's own files, as a Tcl list
set text [$app readFile notes.txt]          ;# empty if there's no such file
set result [$app writeFile notes.txt $text]
if {![java::field $result ok]} {
    $app openDialog "Could not save: [java::field $result error]"
}
```

Files are stored as UTF-8 in `data/users/<name>/files/`. Names may use letters, digits, spaces, `-`, `_` and `.` (up to 100 characters, not starting with `.`), so they can't point outside the user's folder.

## Threads

Swing runs scripts, commands and event handlers on its event thread, and the App API must be called there too. A long-running proc freezes the desktop until it returns.
