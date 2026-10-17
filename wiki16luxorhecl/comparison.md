# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names: the browser version ([`../wiki3`](../wiki3/README.md)), PHP-GTK2 ([`../wiki4phpgtk`](../wiki4phpgtk/README.md)), Java 8 with JavaFX 8 ([`../wiki5jfx`](../wiki5jfx/README.md)), C# 5 with Windows Forms ([`../wiki6cs`](../wiki6cs/README.md)), Java 7 with JavaFX 2.2 ([`../wiki7java7`](../wiki7java7/README.md)), Python 2.7 with Tkinter ([`../wiki8py27tk`](../wiki8py27tk/README.md)), CouchDB couchapps ([`../wiki9couchdb`](../wiki9couchdb/README.md), [`../wiki10couchdb16`](../wiki10couchdb16/README.md), [`../wiki14couchsky`](../wiki14couchsky/README.md)), JavaFX FXML ([`../wiki11fxml`](../wiki11fxml/README.md), [`../wiki12fxmlcouch`](../wiki12fxmlcouch/README.md)), Luxor XUL with Python ([`../wiki13luxor`](../wiki13luxor/README.md)), an improved Luxor XUL with Tcl ([`../wiki15luxorplus`](../wiki15luxorplus/README.md)), and this one, site16luxorhecl: the improved Luxor XUL with Hecl.

## The same Save button, in Tcl and in Hecl

```tcl
# site15luxorplus: Tcl (Jacl 1.3.3)
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

```tcl
# site16luxorhecl: Hecl
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

Almost the same: Hecl reads a field with `$result -field ok` instead of `java::field`, and its `if` runs the condition as a command, so no `[...]` is needed around it. The differences show elsewhere, in maths, comparisons and strings:

| | site15luxorplus (Tcl) | site16luxorhecl (Hecl) |
| --- | --- | --- |
| The Terminal's sample | `expr {6 * 7}` | `* 6 7` |
| Settings, outlining the picture in use | `if {[string equal $current images/backgrounds/$n.jpg]}` | `if {eq $current "images/backgrounds/$n.jpg"}` |
| Settings, checking a number | `regexp {^[0-9]+$} $text`, then `scan` | `catch {set number [int $text]}`, then `ne $number $text` (there's no `regexp`) |
| Notes, "are there unsaved changes?" | `if {$changed}` | `if {= $changed 1}` |
| Web Browser, adding to the history | `lappend history $url` | `lappend $history $url` |
| A Java class | `java::new {java.awt.Color int int int} 48 128 224` | `java java.awt.Color Color`, then `Color -new [list 48 128 224]` |
| A Swing event | `java::bind $page hyperlinkUpdate {... [java::event] ...}` | `bind $page hyperlinkUpdate {... $event ...}` (Skylight's command) |

## Jacl or Hecl?

| | Jacl 1.3.3 (site15) | Hecl (site16) |
| --- | --- | --- |
| Language | Tcl, at about the 8.0 level | Tcl-like, but its own: maths and comparisons are commands, conditions are commands, `incr` and `lappend` take values |
| Built-in commands | Tcl's: `string`, `regexp`, `format`, `clock`, `array`, `file`, ... | A smaller set: `str...` commands, lists, hashes, maths. No `regexp`, `format` or `info` |
| Calling Java | `java::new`, `java::call`, `java::field`, handles like `java0x1b` | Objects are values, called directly; `java <class> <name>` makes a command for a class |
| Swing events | `java::bind`, built in | None built in; Skylight adds `bind` |
| Errors | `errorInfo`: the message and the commands it came through | A list of `{ERROR message line}` and `{command line}`; Skylight turns it into a message |
| On Java 8 | Needs five stand-in `sun.io` classes | Runs once its preprocessor lines are resolved for desktop Java |
| Size | 940 KB of jars, downloaded | 46 source files (about 140 KB of classes), included and compiled with Skylight |
| Licence | Sun's and the University of California's terms | Apache 2.0 |
| Fixes Skylight needed | The `sun.io` stand-ins, and passing the nearest public class for Luxor's controls | A dozen fixes to how Hecl matches arguments to Java methods and converts results (see [`hecl/`](../site16luxorhecl/hecl/README.md#skylights-changes)), a daemon thread, no jline |
| Last change | 1.3.3 in 2006 (1.4.1 in 2008; JTcl after that) | September 2010 |

Hecl was made small for phones, and it shows: its Java bridge was the least finished part, and most of Skylight's fixes are there. Once fixed, it's pleasant to use from Swing: Java objects are ordinary values, and the App API's `List` results arrive as Hecl lists. Jacl is the real Tcl, with Tcl's complete set of commands; Hecl asks Tcl programmers to unlearn a few habits (`if {$x}`, `incr x`, `expr`). Both are frozen.

## Next to site15luxorplus

| | site15luxorplus | site16luxorhecl |
| --- | --- | --- |
| Luxor | 1.0 beta 8, 7 files changed | The same |
| Script language | Tcl (Jacl 1.3.3), downloaded | Hecl, included as source |
| Script files | `app.tcl` | `app.hcl` |
| Libraries downloaded by `tools/get-libs` | Luxor's, and Jacl | Luxor's only |
| App API lists (`listFiles`, `getArgs`) | Returned as Tcl list strings | Returned as Java `List`s, which Hecl turns into lists |
| Errors in notifications | Tcl's `errorInfo` | Hecl's message, with `(line 3, in save at line 1)` |
| Interpreter per window | About 3 ms to make; freed on close | About 0.3 ms to make; its thread stopped on close |
| Java code (non-blank lines) | ~1,380 in 8 files | ~1,470 in 8 files (`Script` grows from 113 to 215 lines, with `bind` and the error messages) |
| Apps (XUL and script, non-blank lines) | Terminal 44, Settings 71, Web Browser 56, Notes 55 | Terminal 46, Settings 78, Web Browser 57, Notes 55 |

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
| site16luxorhecl | A XUL file and a Hecl file | XUL | Hecl |
