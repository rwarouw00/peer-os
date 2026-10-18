# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names: the browser version ([`../wiki03`](../wiki03/README.md)), PHP-GTK2 ([`../wiki04phpgtk`](../wiki04phpgtk/README.md)), Java 8 with JavaFX 8 ([`../wiki05jfx`](../wiki05jfx/README.md)), C# 5 with Windows Forms ([`../wiki06cs`](../wiki06cs/README.md)), Java 7 with JavaFX 2.2 ([`../wiki07java7`](../wiki07java7/README.md)), Python 2.7 with Tkinter ([`../wiki08py27tk`](../wiki08py27tk/README.md)), CouchDB couchapps ([`../wiki09couchdb`](../wiki09couchdb/README.md), [`../wiki10couchdb16`](../wiki10couchdb16/README.md), [`../wiki14couchsky`](../wiki14couchsky/README.md)), JavaFX FXML ([`../wiki11fxml`](../wiki11fxml/README.md), and with its apps in CouchDB, [`../wiki12fxmlcouch`](../wiki12fxmlcouch/README.md)), and this one, site13luxor: Luxor XUL on Swing.

## What an app looks like

The same Notes window, in the FXML version and in this one:

**FXML** (`app.fxml`, with its code in `notes.js`):

```xml
<BorderPane xmlns:fx="http://javafx.com/fxml">
	<top>
		<ToolBar>
			<Button text="Save" onAction="save()"/>
			<Button text="Clear" onAction="askToClear()"/>
			<Region HBox.hgrow="ALWAYS"/>
			<Label fx:id="status"/>
		</ToolBar>
	</top>
	<center>
		<TextArea fx:id="note" wrapText="true"/>
	</center>
	<fx:script source="notes.js"/>
</BorderPane>
```

**Luxor XUL** (`app.xul`, with its code in `app.py`):

```xml
<xul>
	<vbox id="notes">
		<hbox>
			<button label="Save" command="save"/>
			<button label="Clear" command="askToClear"/>
			<spacer flex="1"/>
			<label id="status" value="Hello, a-long-user-name"/>
		</hbox>
		<textarea id="note" rows="12" cols="40" flex="1"/>
	</vbox>
</xul>
```

And the code behind Save:

```javascript
// FXML: JavaScript (Nashorn)
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
# Luxor: Python (Jython 2.1)
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

They're remarkably alike: markup with ids and event names, and a script in which the ids are variables and `app` is the App API. XUL is a little shorter (no imports or namespaces, plain boxes and `flex` instead of `BorderPane` regions); FXML can use every JavaFX class and property directly, where XUL has its own fixed set of tags.

## Next to the FXML version

| | site11fxml (JavaFX FXML) | site13luxor (Luxor XUL) |
| --- | --- | --- |
| Windows described in | FXML: any JavaFX class as an element, any property as an attribute | XUL: Luxor's tags (`vbox`, `button`, `textarea`, ...), and `<gadget>` for anything else |
| Event handlers | `onAction="save()"`: a line of script | `command="save"`: the name of a function |
| App code | JavaScript (Nashorn, ES5.1) | Python 2.1 (Jython 2.1) |
| UI toolkit | JavaFX 8 | Swing |
| Layout | JavaFX panes, `HBox.hgrow`, padding, spacing | `vbox`, `hbox`, `flex`; no padding or spacing |
| Styling | Full JavaFX CSS, in a stylesheet | A few inline properties; named colours only |
| Mistakes reported | By FXMLLoader, with file and line | By Skylight's own checks and Luxor's messages, with file and line |
| Libraries | None beyond Java 8 with JavaFX | Luxor and Jython (about 2 MB, downloaded) |
| Needs | A Java 8 that includes JavaFX | Any Java 8 |
| Last updated | JavaFX and FXML are maintained (OpenJFX) | Luxor 2005, Jython 2.1 2001 |
| Desktop Java code | ~930 lines in 7 files | ~1,320 lines in 8 files: Swing needs more code for the same desktop, and `Xul` bridges Luxor |
| Built-in apps (markup + code, non-blank lines) | Terminal 47, Settings 62, Web Browser 40 | Terminal 46, Settings 67, Web Browser 49 |
| Notes tutorial | 54 | 49 |

## The idea, across the versions

| Version | An app is | Layout in | Logic in |
| --- | --- | --- | --- |
| site03, site09, site10, site14 (browser) | An HTML page | HTML and CSS | JavaScript |
| site04phpgtk | A PHP class | PHP | PHP |
| site05jfx, site07java7 | A Java class | Java | Java |
| site08py27tk | A Python class | Python | Python |
| site11fxml, site12fxmlcouch | An FXML file | FXML and CSS | JavaScript |
| site13luxor | A XUL file and a Python file | XUL | Python |

Luxor was one of several early-2000s projects that brought Mozilla's XUL idea to Java (others included Thinlet, SwiXML and XUI); JavaFX's FXML is the same idea, built into the toolkit and still maintained. For a new desktop app in Java, FXML is the practical choice. This version shows that the design was already there twenty years ago, and that it still runs.

## Notes in every version

The same Notes app, written for all fourteen versions, is compared side by side in [Notes, in every version](../wikicompare/README.md).
