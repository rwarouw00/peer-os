# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names: the browser version ([`../wiki3`](../wiki3/README.md)), PHP-GTK2 ([`../wiki4phpgtk`](../wiki4phpgtk/README.md)), Java 8 with JavaFX 8 ([`../wiki5jfx`](../wiki5jfx/README.md)), C# 5 with Windows Forms ([`../wiki6cs`](../wiki6cs/README.md)), Java 7 with JavaFX 2.2 ([`../wiki7java7`](../wiki7java7/README.md)), Python 2.7 with Tkinter ([`../wiki8py27tk`](../wiki8py27tk/README.md)), CouchDB 2.3.1 ([`../wiki9couchdb`](../wiki9couchdb/README.md)) and CouchDB 1.6 ([`../wiki10couchdb16`](../wiki10couchdb16/README.md)) couchapps, and this one, site11fxml.

## Next to the Java 8 version

Same runtime, same look, same App API and data. What moved from Java into FXML:

| | site5jfx (Java 8) | site11fxml (FXML) |
| --- | --- | --- |
| An app is | A Java class, compiled into a jar | An `app.fxml`, with JavaScript inside it or beside it |
| Changing an app | Edit, compile, rebuild the jar, restart Skylight | Edit, open the app again |
| Event handlers | Java lambdas | One-line scripts calling script functions (`onAction="save()"`) |
| App logic | Java | JavaScript (Nashorn); Java still possible with `class=` |
| Layout | Built in code (`new HBox(...)`, `setTop(...)`) | Elements and attributes in XML |
| The desktop's windows | Built in `Desktop.java` | `ui/login.fxml`, `desktop.fxml`, `menu.fxml`, `notification.fxml`; `Desktop` is their controller |
| Stylesheet | Inside the jar | `ui/skylight.css`, a plain file |
| Mistakes found | When compiling | When the window opens (a "Cannot open app" notification with the file and line) |
| Window-closing check | Override `canClose()` | `app.setCanClose(function () { ... })` |

### Did it make less code?

Some, but the bigger gain is in *what kind* of code it is:

| | site5jfx | site11fxml |
| --- | --- | --- |
| Java | ~1,070 lines, 9 files | ~910 lines, 7 files |
| Desktop windows in FXML | — | ~80 lines, 4 files |
| Terminal | 66 lines of Java | 47 lines of FXML and JavaScript |
| Settings | 59 | 62 (the six backgrounds are written out one by one) |
| Web Browser | 50 | 40 |
| Notes tutorial | 67 | 54 |

(Non-blank lines.) The total is about the same. The Java that remains is the part that has to be code anyway: storage, login checks, dragging shortcuts, toasts, the window list. What FXML removes is layout code, and the compile step for apps. Layout is easier to read as a tree of elements than as nested constructor calls, and an app can be written, changed and tried without a JDK.

### What it costs

* **Two languages instead of one** in each app: XML for layout and JavaScript for behaviour, with a few traps where they meet (see the [FXML primer](fxml-primer.md#javascript-and-java)).
* **Mistakes show up later.** Java's compiler catches a misspelt method before anything runs; in a script, it's a runtime error when the button is clicked (reported as an "Error" notification by JavaFX's handler, or in the console).
* **Tied to Nashorn.** Script apps need a JavaScript engine, which Java removed in version 15 (see [Limitations](limitations.md)).

## The idea, across the versions

Several versions describe apps as files rather than compiled code:

| Version | An app is | Layout in | Logic in |
| --- | --- | --- | --- |
| site3, site9, site10 (browser) | An HTML page | HTML and CSS | JavaScript |
| site4phpgtk | A PHP class | PHP | PHP |
| site8py27tk | A Python class | Python | Python |
| site11fxml | An FXML file | FXML and CSS | JavaScript |

site11fxml is the closest of the desktop versions to the browser one: markup for the window and JavaScript for the behaviour, but with real windows and JavaFX's controls instead of HTML.
