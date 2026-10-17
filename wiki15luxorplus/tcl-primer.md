# Tcl and Jacl

Apps in this version are written in **Tcl**, run by **Jacl 1.3.3**, a Tcl interpreter written in Java. This page covers the parts of Tcl that apps use, how Tcl reaches Java, and what's particular about Jacl.

## Jacl

| | |
| --- | --- |
| What it is | A Tcl interpreter in pure Java, from Sun Microsystems' Tcl/Java project, later maintained on SourceForge ([tcljava](https://sourceforge.net/projects/tcljava/)) |
| Version | 1.3.3, August 2006 (the 1.4 series followed, up to 1.4.1 in April 2008, and JTcl continued it after that) |
| Jars | `jacl.jar` (the interpreter) and `tcljava.jar` (the `java::` commands), about 940 KB together |
| Tcl level | Reports itself as Tcl 8.0 (`info patchlevel`), with some later additions: `string is`, `lsort -dictionary`, `end-1` indexes and `é` escapes all work. Not available: the `eq` and `ne` operators (use `string equal`), `\d` in regular expressions (use `[0-9]`), `dict`, `lassign`, `{*}` |
| Licence | Sun Microsystems' and the University of California's terms, allowing use, changes and redistribution |

**Jacl 1.3.3 doesn't run on Java 8 as released.** Its file and console streams use `sun.io.ByteToCharConverter` and `sun.io.CharToByteConverter`, private JDK classes that Java 8 removed, so creating an interpreter fails with `NoClassDefFoundError: sun/io/UnknownCharacterException`. Skylight adds five small stand-in classes with the same names, written on `java.nio.charset` ([`compat/`](../site15luxorplus/compat/README.md)), in `lib/jacl-java8.jar`. Jacl's own jars are used unchanged.

## Tcl in five minutes

Everything is a command and its words, separated by spaces; a line is a command:

```tcl
set name World                  ;# a variable
puts "Hello, $name"             ;# $name is the variable's value
set n [expr {6 * 7}]            ;# [...] runs a command and uses its result
if {$n > 40} {
    puts big
}
foreach file {a.txt b.txt} {
    puts $file
}
proc greet {who {greeting Hello}} {    ;# a procedure, with a default argument
    return "$greeting, $who"
}
greet Sam                       ;# Hello, Sam
```

* **`"..."`** groups words and substitutes `$variables` and `[commands]` inside; **`{...}`** groups words without substituting anything. Bodies of `if`, `proc` and `foreach` are in braces, so they're substituted when they run.
* **Everything is a string.** Lists are strings of words: `llength {a b c}` is 3, `lindex {a b c} 0` is `a`.
* **Procs only see their own variables.** A proc reaches top-level variables by naming them with `global`, which is why every proc in Skylight's apps starts with `global app ...`.
* **Errors** stop the script, unless caught: `if {[catch {risky} message]} { ... }`.

## Tcl and Java

`package require java` (which Skylight runs for every app) adds the `java::` commands, and a Java object in Tcl is a **handle**: a word like `java0x1b` that works as a command.

| Tcl | Java |
| --- | --- |
| `$note setText Hello` | `note.setText("Hello")` |
| `set text [$note getText]` | `String text = note.getText()` |
| `java::new javax.swing.JTree` | `new JTree()` |
| `java::call javax.swing.BorderFactory createEmptyBorder 3 3 3 3` | `BorderFactory.createEmptyBorder(3, 3, 3, 3)` (a static method) |
| `java::field $result ok` | `result.ok` |
| `java::field $settings notificationTime 3000` | `settings.notificationTime = 3000` |
| `java::bind $note keyTyped {set changed 1}` | `note.addKeyListener(...)` running Tcl on each typed key |
| `[java::event]`, inside a binding | The event object |
| `java::null`, `java::isnull $x` | `null`, `x == null` |

How values cross:

* **Strings, numbers and booleans** become Tcl values: a method returning `String` gives the text, `int` a number, `boolean` gives `1` or `0`. A Java `null` *string* becomes the empty string.
* **Other objects** stay handles. A method declared to return `Object` gives a handle even when the object is a string, so `[$list get 0]` is a handle; `[[$list get 0] toString]` is the text. That's why Skylight's App API returns lists as Tcl lists rather than Java lists.
* **Overloaded methods**: Jacl picks the version that fits the arguments where it can (`$page setPage $url` uses `setPage(String)`). When it can't, it says `ambiguous constructor signature, could not choose between ...`, and the signature has to be spelled out: `java::new {java.awt.Color int int int} 48 128 224`.
* **Only public classes.** Jacl calls methods through an object's class, which must be public; Skylight hands scripts each control as its nearest public class (a Luxor label is a private subclass of `JLabel`).

## Events and threads

`java::bind` doesn't run its Tcl directly: it queues it for the interpreter's own thread, and runs it at once only if the event happens on that thread. Skylight creates each window's interpreter on Swing's event thread, which is where Swing's events happen, so bound Tcl runs straight away. A Swing event from any other thread would wait for that interpreter's next turn.

## In Skylight

* **One interpreter per window**, made when the window opens (about 3 milliseconds) and freed when it closes. Two Notes windows don't share variables.
* **Variables:** `app` (the [App API](app-api.md)) and one per `id` in the app's XUL file, all at the top level.
* **Commands:** `command="save"` in the XUL runs the script's proc `save`; `command="choose 3"` runs `choose 3`. The first word must be one of the script's procs.
* **Callbacks** given to the App API are Tcl, usually a proc's name: `$app openDialog "Clear?" clearNote`.
* **`exit` is removed**, so a script can't close Skylight.
* **Files:** `app.tcl` is read as UTF-8.
