# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names: the browser version ([`../wiki3`](../wiki3/README.md)), PHP-GTK2 ([`../wiki4phpgtk`](../wiki4phpgtk/README.md)), Java 8 with JavaFX 8 ([`../wiki5jfx`](../wiki5jfx/README.md)), C# 5 with Windows Forms ([`../wiki6cs`](../wiki6cs/README.md)), Java 7 with JavaFX 2.2 ([`../wiki7java7`](../wiki7java7/README.md)), Python 2.7 with Tkinter ([`../wiki8py27tk`](../wiki8py27tk/README.md)), CouchDB couchapps ([`../wiki9couchdb`](../wiki9couchdb/README.md), [`../wiki10couchdb16`](../wiki10couchdb16/README.md), [`../wiki14couchsky`](../wiki14couchsky/README.md)), JavaFX FXML ([`../wiki11fxml`](../wiki11fxml/README.md), [`../wiki12fxmlcouch`](../wiki12fxmlcouch/README.md)), Luxor XUL with Python ([`../wiki13luxor`](../wiki13luxor/README.md)), and this one, site15luxorplus: an improved Luxor XUL with Tcl.

## The same Save button, three ways

```javascript
// site11fxml: JavaScript (Nashorn), with FXML
function save() {
	var result = app.writeFile('notes.txt', note.getText());
	if (result.ok) {
		changed = false;
		status.setText('Saved');
		app.postNotification('Notes', 'Your note was saved.');
	} else {
		app.openDialog('Could not save: ' + result.error);
	}
}
```

```python
# site13luxor: Python (Jython 2.1), with Luxor XUL
def save():
    global changed
    result = app.writeFile('notes.txt', note.getText())
    if result.ok:
        changed = 0
        status.setText('Saved')
        app.postNotification('Notes', 'Your note was saved.')
    else:
        app.openDialog('Could not save: ' + result.error)
```

```tcl
# site15luxorplus: Tcl (Jacl 1.3.3), with the improved Luxor XUL
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
```

The three are the same shape: the controls are variables, `app` is the App API, and Java methods are called directly. Tcl's differences are visible: commands rather than method calls (`$status setText Saved`), `[...]` for a command's result, `global` for every outside variable, and `java::field` for a Java field.

## Next to site13luxor

| | site13luxor | site15luxorplus |
| --- | --- | --- |
| Luxor | 1.0 beta 8 as released | 1.0 beta 8 with 7 files changed, built from source |
| Styles | Named colours, `serif`/`sans-serif`/`monospace` only, no padding | Also `#rrggbb` and `rgb()`, any Java font, `padding`, `margin`, `spacing` |
| Labels | Keep the width of their first text | Grow with their text |
| `max-width` | Never worked (a Luxor bug) | Works |
| Button tooltips, picture buttons | Set in Java; `label=""` needed | `tooltip="..."`; no label needed |
| Finding controls | Some named; text areas through Luxor's input index | Every control named after its id |
| Web Browser page | A Swing `JEditorPane` added through a `<gadget>` | Luxor's `<iframe>`, steered from Tcl |
| Script language | Python 2.1 (Jython 2.1) | Tcl, at about the 8.0 level (Jacl 1.3.3) |
| Commands | A function's name | A proc's name, with arguments (`command="choose 3"`) |
| Callbacks | Python functions | Tcl scripts, usually a proc's name |
| Script libraries | 720 KB (Jython) | 940 KB (Jacl) plus a 4 KB Java 8 fix |
| Java 8 as released | Jython 2.1 works | Jacl 1.3.3 needs five stand-in `sun.io` classes |
| Java code (non-blank lines) | ~1,320 in 8 files | ~1,380 in 8 files |
| Apps (XUL and script, non-blank lines) | Terminal 46, Settings 67, Web Browser 49, Notes 49 | Terminal 44, Settings 71, Web Browser 56, Notes 55 |

The apps are about the same size. Tcl spends lines on `global`; XUL gains a few with `spacing`, `padding` and tooltips. The improvements show most in the desktop's own windows: site13 set their padding and a tooltip in Java, and started labels with dummy text so they'd be wide enough.

## Python or Tcl?

| | Jython 2.1 | Jacl 1.3.3 |
| --- | --- | --- |
| Language level | Python 2.1 (2001): no `True`, generators or `with` | Tcl 8.0 (1997) with some later commands: no `eq`, `dict` or `\d` |
| Calling Java | Looks like Java: `note.setText('x')`, `result.ok` | Commands: `$note setText x`, `java::field $result ok` |
| Swing events | Event properties: `note.keyTyped = edited` | `java::bind $note keyTyped {set changed 1}` |
| Callbacks | Functions | Strings of Tcl |
| Libraries | Python's own library isn't in the jar (`re`, `string` missing) | Tcl's built-in commands are all there (`regexp`, `string`, `clock`, ...) |
| On Java 8 | Works as released | Needs Skylight's `compat/` classes |

Jython reads more like the Java underneath; Tcl is a better fit for XUL's `command="..."` (a command *is* Tcl) and its built-in commands are complete. Both are frozen: Jython 2.1 since 2001, Jacl since 2008.

## The idea, across the versions

| Version | An app is | Layout in | Logic in |
| --- | --- | --- | --- |
| site3, site9, site10, site14 (browser) | An HTML page | HTML and CSS | JavaScript |
| site4phpgtk | A PHP class | PHP | PHP |
| site5jfx, site7java7 | A Java class | Java | Java |
| site8py27tk | A Python class | Python | Python |
| site11fxml, site12fxmlcouch | An FXML file | FXML and CSS | JavaScript |
| site13luxor | A XUL file and a Python file | XUL | Python |
| site15luxorplus | A XUL file and a Tcl file | XUL | Tcl |
