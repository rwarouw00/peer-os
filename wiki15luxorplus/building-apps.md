# Building your first app

This tutorial builds **Notes**, an app that keeps one note per user. Like the built-in apps, it's three plain files: XUL for the window, Tcl for what it does, and a manifest. There's no Java and nothing to compile. It covers the parts of the [App API](app-api.md) most apps need: reading and saving the user's files, notifications, a confirmation dialog, and stopping the window closing while there are unsaved changes. Every step was tested, with the code exactly as it is here.

![The finished Notes app](images/notes.png)

You need Skylight OS built and running; see [Getting started](getting-started.md). New to Tcl? The [Tcl primer](tcl-primer.md) has what you need.

## 1. Create the folder

```
site15luxorplus/apps/notes/
├── app.properties
├── app.xul
├── app.tcl
└── icons/16.png, 32.png, 48.png, 64.png
```

For the icons, copy the four PNGs from `apps/settings/icons/` for now.

## 2. Describe the app

`app.properties` (see [App manifest](app-manifest.md)):

```properties
title=Notes
xul=app.xul
script=app.tcl
width=440
height=320
icon.16=icons/16.png
icon.32=icons/32.png
icon.48=icons/48.png
icon.64=icons/64.png
```

## 3. Lay out the window

`app.xul`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xul>
	<vbox id="notes" spacing="4" style="padding: 6;">
		<hbox spacing="4">
			<button label="Save" command="save" tooltip="Save the note"/>
			<button label="Clear" command="askToClear" tooltip="Empty the note"/>
			<spacer flex="1"/>
			<label id="status" value=""/>
		</hbox>
		<textarea id="note" rows="12" cols="40" flex="1" wrap="true"/>
	</vbox>
</xul>
```

* Everything is inside **`<xul>`**. The first box in it, `<vbox id="notes">`, becomes the window's contents.
* **Boxes** put their contents in a column (`<vbox>`) or a row (`<hbox>`). **`flex="1"`** gives a control the spare room: the spacer pushes the status label to the right, and the text area fills the window.
* **`spacing="4"`** leaves 4 pixels between a box's children, and **`style="padding: 6;"`** leaves 6 around them (both from Skylight's [improved Luxor](luxor-improved.md)).
* **`command="save"`** makes the button run the script's proc `save`. **`tooltip`** is shown when the mouse rests on it.
* **`id="note"`** makes the control a variable, `$note`, in the script.
* **`wrap="true"`** wraps long lines at word boundaries.

## 4. Write the script

`app.tcl`:

```tcl
# Notes: one note per user, kept in their files.
set changed 0

# 1. Show the saved note (an empty string if there isn't one yet), and watch for typing.
$note setText [$app readFile notes.txt]
$status setText "Hello, [$app getUsername]"
java::bind $note keyTyped {set changed 1}

# 2. Save. writeFile reports success, or an error to show the user.
proc save {} {
    global app note status changed
    set result [$app writeFile notes.txt [$note getText]]
    if {[java::field $result ok]} {
        set changed 0
        $status setText Saved
        $app postNotification Notes "Your note was saved."
    } else {
        $app openDialog "Could not save: [java::field $result error]"
    }
}

# 3. Ask before throwing work away. clearNote runs only if the user clicks OK.
proc clearNote {} {
    global note status
    $note setText ""
    $status setText "Cleared (not saved yet)"
}

proc askToClear {} {
    global app
    $app openDialog "Clear the note? This can't be undone." clearNote
}

# 4. Don't lose unsaved work when the window is closed.
proc closeAnyway {} {
    global app changed
    set changed 0
    $app closeWindow
}

proc canClose {} {
    global app changed
    if {$changed} {
        $app openDialog "Close without saving your changes?" closeAnyway
        return 0
    }
    return 1
}

$app setCanClose canClose
```

How it works:

1. **The script runs once**, when the window is being built. Every `id` in `app.xul` is a variable holding its Swing control (`$note` is a `JTextArea`, `$status` a `JLabel`), and **`$app`** is the [App API](app-api.md). `$note setText ...` calls the text area's `setText` method.
2. **Procs** named in `command="..."` attributes run when their button is clicked. A proc sees top-level variables only after naming them with `global`, which is why each proc starts with one.
3. **Events:** `java::bind $note keyTyped {set changed 1}` runs that Tcl for every key typed in the note. Bound Tcl runs at the top level, so it sets the top-level `changed` directly.
4. **Files:** `$app readFile` and `$app writeFile` use the user's private storage, so each user has their own `notes.txt`. A missing file reads as an empty string. `writeFile` returns a Java object, whose fields `java::field` reads.
5. **Dialogs:** the Tcl given to `$app openDialog` (here the proc `clearNote`) runs only if the user clicks OK.
6. **Closing:** `$app setCanClose canClose` makes Skylight run `canClose` before the window closes. Returning `0` keeps it open; here it asks first, and closes the window itself if the user agrees.

## 5. Run it

Open the **Terminal** and run:

```tcl
$app openWindow apps/notes/
```

Type something, click **Save**, close the window and open it again: the note is still there. Type some more and close the window: you're asked first.

![Closing with unsaved changes](images/notes-close.png)

To change the app, edit `app.xul` or `app.tcl` and open the window again: both are read each time an app opens. (`app.properties` is read once per login.)

## 6. Add it to the home menu

Add the folder to `apps/apps.txt`:

```
apps/terminal/
apps/settings/
apps/web-browser/
apps/notes/
```

Log out and in again: Notes appears in the home menu. `$app addShortcut apps/notes/` in the Terminal puts it on the desktop.

## Seeing the layout without running it

`tools/preview.sh` (`tools\preview.bat` on Windows) shows every `.xul` file in Skylight in Luxilla, one tab each, including your new `notes` box. The buttons do nothing there, since Luxilla runs no scripts, but it's a quick way to check a layout.

## Going further

* **Commands with arguments**: `command="choose 3"` runs `choose 3`, as the Settings app does for its six pictures.
* **Any Swing component**: `<gadget id="chart"/>` in the XUL is an empty panel that the script fills: `$chart add [java::new javax.swing.JScrollPane [java::new javax.swing.JTree]]`.
* **Java classes from Tcl**: `java::new`, `java::call` and `java::field` reach every class on the class path (see the [Tcl primer](tcl-primer.md#tcl-and-java)).
* **Several notes**: `$app listFiles` returns the names of the user's files, as a Tcl list.
* **Settings**: `$app getSettings` and `$app saveSettings`, as in the Settings app (`apps/settings/app.tcl`).

## Troubleshooting

When an app can't open, the "Cannot open app" notification gives the file and the reason.

| Symptom | Likely cause |
| --- | --- |
| "Error on line 5: The element type ... must be terminated" | The XUL isn't well-formed XML: a tag isn't closed (`<button .../>` needs its `/`) |
| "syntax error: unsupported value ... for css property ..." | A `style` Luxor doesn't understand |
| "expected <xul> with a <vbox> or <hbox> inside it" | The file's root isn't `<xul>`, or the window's box is missing |
| `apps/notes/app.tcl: invalid command name "..." while executing ...` | A mistake in the script, with the command that failed |
| "Error in Notes: ..." when a button is clicked | A mistake inside that proc; the notification gives Tcl's error and where it happened |
| "Error in Notes: The script has no proc save." | A `command` in the XUL names a proc the script doesn't have |
| `can't read "note": no such variable` | A proc uses a top-level variable without `global note` |
| `syntax error in expression "$a eq $b"` | Jacl's Tcl has no `eq`; use `[string equal $a $b]` |
| `ambiguous constructor signature, could not choose between ...` | An overloaded Java constructor or method; name the signature: `java::new {java.awt.Color int int int} 48 128 224` |
| `no accessible method "..." in class ...` | A misspelt method, or one that isn't public |
| A Java result looks like `java0x1f` | It's an object, not a string; call `toString` on it, or `java::field` for a field |
