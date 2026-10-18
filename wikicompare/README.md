# Notes, in every version of Skylight OS

Every version of Skylight OS has the same tutorial: **Notes**, an app that keeps one note per user, with **Save** and **Clear** buttons and a status label. That makes it a fair way to compare them: the same app, written fourteen times against the same App API names. This page puts those versions side by side. Every snippet here is copied from that version's *Building your first app* page, where it was tested.

## The fourteen versions

| Version | Desktop runs on | An app is | Lines in the tutorial's files | Tutorial |
| --- | --- | --- | --- | --- |
| site03 | A web browser, with PHP on the server | A web page: `index.html`, `style.css`, `notes.js`, `app.json` | 69 | [wiki03](../wiki03/building-apps.md) |
| site04phpgtk | PHP-GTK2 | A PHP class: `app.php`, `app.json` | 90 | [wiki04phpgtk](../wiki04phpgtk/building-apps.md) |
| site05jfx | Java 8, JavaFX 8 | A compiled Java class: `NotesApp.java` → `app.jar`, `app.properties` | 74 | [wiki05jfx](../wiki05jfx/building-apps.md) |
| site06cs | C# 5, Windows Forms | A compiled C# class: `NotesApp.cs` → `app.dll`, `app.json` | 79 | [wiki06cs](../wiki06cs/building-apps.md) |
| site07java7 | Java 7, JavaFX 2.2 | A compiled Java class, without lambdas: `NotesApp.java` → `app.jar`, `app.properties` | 100 | [wiki07java7](../wiki07java7/building-apps.md) |
| site08py27tk | Python 2.7, Tkinter | A Python class: `app.py`, `app.json` | 50 | [wiki08py27tk](../wiki08py27tk/building-apps.md) |
| site09couchdb | CouchDB 2.3.1 and a browser | site03's web page, uploaded into CouchDB | 69 | [wiki09couchdb](../wiki09couchdb/building-apps.md) |
| site10couchdb16 | CouchDB 1.6 and a browser | The same, for CouchDB 1.6 | 69 | [wiki10couchdb16](../wiki10couchdb16/building-apps.md) |
| site11fxml | Java 8, JavaFX 8 | FXML for the window, JavaScript (Nashorn) for the code: `app.fxml`, `notes.js`, `app.properties` | 62 | [wiki11fxml](../wiki11fxml/building-apps.md) |
| site12fxmlcouch | Java 8, JavaFX 8, apps from CouchDB | site11's files, kept in a CouchDB app store | 62 | [wiki12fxmlcouch](../wiki12fxmlcouch/building-apps.md) |
| site13luxor | Java 8, Swing, Luxor XUL | XUL for the window, Python (Jython 2.1) for the code: `app.xul`, `app.py`, `app.properties` | 58 | [wiki13luxor](../wiki13luxor/building-apps.md) |
| site14couchsky | CouchDB 2.3.1 and a browser | site03's web page, as its own document in CouchDB | 70 | [wiki14couchsky](../wiki14couchsky/building-apps.md) |
| site15luxorplus | Java 8, Swing, an improved Luxor XUL | XUL and Tcl (Jacl 1.3.3): `app.xul`, `app.tcl`, `app.properties` | 64 | [wiki15luxorplus](../wiki15luxorplus/building-apps.md) |
| site16luxorhecl | Java 8, Swing, the improved Luxor XUL | XUL and Hecl: `app.xul`, `app.hcl`, `app.properties` | 64 | [wiki16luxorhecl](../wiki16luxorhecl/building-apps.md) |

"Lines" counts the non-blank lines of the files the tutorial writes, manifest included, icons not. The four browser versions share one tutorial (site14's manifest has one more line, for its place in the menu), and so do site11 and site12. That leaves **ten different ways** of writing Notes.

The line counts follow how the window is described more than the language. Where the window is built in code (PHP, Java, C#, Python), that code is most of the app. Where it's described in a file of its own (HTML, FXML, XUL), the script only has to wire it up.

## The Save button, ten ways

Each one does the same thing: write the note to `notes.txt`, show "Saved" and post a notification, or show the error.

**JavaScript in a web page** (site03, site09, site10, site14):

```javascript
document.getElementById('save').onclick = function () {
	var reply = App.writeFile(FILE, note.value);
	if (reply.ok) {
		statusLabel.textContent = 'Saved';
		App.postNotification('Notes', 'Your note was saved.');
	} else {
		App.openDialog('Could not save: ' + reply.error);
	}
};
```

**PHP** (site04phpgtk):

```php
function save()
{
	$text = $this->buffer->get_text($this->buffer->get_start_iter(), $this->buffer->get_end_iter());
	$result = $this->writeFile('notes.txt', $text);
	if ($result['ok']) {
		$this->changed = false;
		$this->status->set_text('Saved');
		$this->postNotification('Notes', 'Your note was saved.');
	} else {
		$this->openDialog('Could not save: ' . $result['error']);
	}
}
```

**Java 8** (site05jfx) and **Java 7** (site07java7), the same code:

```java
private void save() {
	Result result = writeFile("notes.txt", note.getText());
	if (result.ok) {
		changed = false;
		status.setText("Saved");
		postNotification("Notes", "Your note was saved.");
	} else {
		openDialog("Could not save: " + result.error);
	}
}
```

**C# 5** (site06cs):

```csharp
void Save()
{
	Result result = WriteFile("notes.txt", note.Text);
	if (result.Ok)
	{
		changed = false;
		status.Text = "Saved";
		PostNotification("Notes", "Your note was saved.");
	}
	else
	{
		OpenDialog("Could not save: " + result.Error);
	}
}
```

**Python 2.7 with Tkinter** (site08py27tk):

```python
def save(self):
    result = self.writeFile('notes.txt', self.note.get('1.0', 'end-1c'))
    if result['ok']:
        self.note.edit_modified(False)
        self.status.configure(text='Saved')
        self.postNotification('Notes', 'Your note was saved.')
    else:
        self.openDialog('Could not save: ' + result['error'])
```

**JavaScript (Nashorn) behind FXML** (site11fxml, site12fxmlcouch):

```javascript
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

**Python 2.1 (Jython) behind XUL** (site13luxor):

```python
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

**Tcl (Jacl) behind XUL** (site15luxorplus):

```tcl
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

**Hecl behind XUL** (site16luxorhecl):

```tcl
proc save {} {
    global app note status changed
    set result [$app writeFile notes.txt [$note getText]]
    if {$result -field ok} {
        set changed 0
        $status setText Saved
        $app postNotification Notes "Your note was saved."
    } else {
        $app openDialog "Could not save: [$result -field error]"
    }
}
```

What changes between them is how each language reaches its toolkit: `note.value` in a web page, three calls to get a GTK text buffer's text, Tk's `'1.0', 'end-1c'` indexes, `getText()` on JavaFX and Swing controls. The App API, `writeFile` returning ok or an error, stays the same throughout.

## Noticing changes, and asking before closing

All but the browser versions keep track of unsaved changes and ask before the window closes. The browser versions' App API has no way to stop a window closing, so their tutorial leaves this out.

| Version | Noticing that the note changed | Asking before the window closes |
| --- | --- | --- |
| site03, 9, 10, 14 | Not done | Not possible: there's no close check in the browser App API |
| site04phpgtk | `$this->buffer->connect_simple('changed', array($this, 'markChanged'))` | Override `canClose()`, returning `false` to keep the window open |
| site05jfx | `note.textProperty().addListener((property, oldText, newText) -> changed = true)` | Override `canClose()` |
| site06cs | `note.TextChanged += delegate { changed = true; }` | Override `CanClose()` |
| site07java7 | An anonymous `ChangeListener<String>` class, five lines, since Java 7 has no lambdas | Override `canClose()`, with an anonymous `Runnable` for the dialog |
| site08py27tk | None needed: Tk's text widget keeps its own flag, `edit_modified()` | Override `canClose()` |
| site11, 12 | `note.textProperty().addListener(new javafx.beans.InvalidationListener(function () { changed = true; }))` | `app.setCanClose(function () { ... })` |
| site13luxor | `note.keyTyped = edited`: Jython turns a Java event into a property | `app.setCanClose(canClose)` |
| site15luxorplus | `java::bind $note keyTyped {set changed 1}` | `$app setCanClose canClose` |
| site16luxorhecl | `bind $note keyTyped {set changed 1}`, a command Skylight adds, since Hecl has none | `$app setCanClose canClose` |

The class-based versions (site04 to site08) ask by overriding a method of Skylight's `App` class. The script-based ones (site11 onwards) can't subclass, so they hand the App API a function, or a proc's name, to call instead.

## From written to running

| Version | After writing the files | Edits show up |
| --- | --- | --- |
| site03 | Nothing: `App.openWindow('apps/notes/');` in the Terminal | The next time the app opens, unless the browser uses a cached copy |
| site09, site10 | Upload into CouchDB: `tools/push.sh http://admin:password@127.0.0.1:5984` | After the next upload |
| site14couchsky | Upload just the app: `tools/push-app.sh http://admin:password@127.0.0.1:5984 notes`; after a reload it's in the home menu, with no list to edit | After the next upload |
| site04phpgtk | Nothing: `$app->openWindow('apps/notes/');` | When Skylight restarts |
| site05jfx | Compile with `javac` against `skylight.jar`, then `jar cf app.jar` | After compiling again and restarting |
| site06cs | Compile with `csc.exe` (or Mono's `mcs`) against `Skylight.exe` | After compiling again and restarting |
| site07java7 | Compile with Java 7's `javac` against `skylight.jar` and `jfxrt.jar`, then `jar cf app.jar` | After compiling again and restarting |
| site08py27tk | Nothing: `app.openWindow('apps/notes/')` | When Skylight restarts |
| site11fxml | Nothing: `app.openWindow('apps/notes/');` | The next time the app opens |
| site12fxmlcouch | Run from the `apps/` folder while writing it, then push to the store | The next time the app opens |
| site13, 15, 16 | Nothing: `app.openWindow('apps/notes/')`, or `$app openWindow apps/notes/` in Tcl and Hecl | The next time the app opens |

Adding the app to the home menu is one line in an apps list in every version except site14couchsky, which finds apps in CouchDB by itself.

## What the comparison shows

* **Describing the window in its own file makes apps shortest.** XUL with a script (58 to 64 lines) and FXML with JavaScript (62) beat every version that builds its window in code (74 to 100). Python with Tkinter (50) is the exception, because Tk's packing is terse and its text widget tracks changes itself.
* **Compiled apps take the most steps.** site05, site06 and site07 need a compiler, the desktop's own jar or exe to compile against, and a restart after each change. The script versions just reopen the window.
* **Older languages show in the details more than the shape.** Java 7 spends its extra 26 lines on anonymous classes where Java 8 has lambdas. Jython 2.1 uses `1` and `0` for lack of `True` and `False`. Jacl's Tcl needs `java::field`. Hecl changes `if` itself, and needs Skylight to add event binding.
* **The App API held.** Every version's Save calls the same `writeFile`, checks the same `ok`, shows the same `error`, and posts the same notification. Only C# capitalises them (`WriteFile`, `Ok`), as C# does.
