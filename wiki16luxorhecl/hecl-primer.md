# Hecl

Apps in this version are written in **Hecl**, a small scripting language written in Java. It reads like Tcl, and anyone who knows Tcl will feel at home for the first few lines, but it isn't Tcl: this page covers what apps use, where Hecl differs from Tcl, and how Hecl reaches Java. Every example here was run.

## Hecl

| | |
| --- | --- |
| What it is | A scripting language for Java by David N. Welton, with Wolfgang Kechel and others ([github.com/davidw/hecl](https://github.com/davidw/hecl)). Written first for mobile phones (Java ME), where small mattered, and also built for desktop Java and Android |
| Version | The git repository's last commit, `ba81ffc` (September 2010). The last release was `hecl-20080730` |
| Size | The language and its Java bridge are 46 source files, about 140 KB of classes (75 KB compressed in `skylight.jar`) |
| In Skylight | Included as source in [`site16luxorhecl/hecl/`](../site16luxorhecl/hecl/README.md), with Skylight's fixes to how it calls Java, and compiled with Skylight |
| Licence | Apache License 2.0 |

## Hecl in five minutes

Everything is a command and its words, separated by spaces; a line is a command:

```tcl
set name World                  ;# a variable
puts "Hello, $name"             ;# $name is the variable's value
set n [* 6 7]                   ;# [...] runs a command and uses its result: 42
if {> $n 40} {
    puts big
}
foreach file {a.txt b.txt} {
    puts $file
}
proc greet {who greeting} {     ;# a procedure
    return "$greeting, $who"
}
greet Sam Hello                 ;# Hello, Sam
```

* **`"..."`** groups words and substitutes `$variables` and `[commands]` inside; **`{...}`** groups words without substituting anything. Bodies of `if`, `proc` and `foreach` are in braces, and run later.
* **Lists** are words: `llen {a b c}` is 3, `lindex {a b c} 0` is `a`, `lindex {a b c} -1` is `c`.
* **Procs only see their own variables.** A proc reaches top-level variables by naming them with `global`, which is why every proc in Skylight's apps starts with `global app ...`.
* **Errors** stop the script, unless caught: `if {catch {risky} error} { ... }`. `throw "message"` raises one.

## Where Hecl differs from Tcl

| | Tcl | Hecl |
| --- | --- | --- |
| Maths | `expr {$a * 2 + 1}` | Commands: `+ [* $a 2] 1`. Also `-`, `/` (whole numbers stay whole: `/ 7 2` is 3, `/ 7.0 2` is 3.5), `%`, `max`, `min`, `abs`, `sqrt`, `round` and more. There's no `expr` |
| Comparing | `$a == $b`, `$a eq $b` | `= $a $b`, `!=`, `<`, `>`, `<=`, `>=` for numbers (they fail on text); `eq $a $b` and `ne $a $b` for text; `not`, `and`, `or` |
| Conditions | `if {$changed} ...` | The condition is **a command**, whose result is tested: `if {= $changed 1}`, `if {eq $a $b}`, `if {$result -field ok}`, `if {catch {...} e}`. `if {$changed}` tries to run a command named `1` |
| Changing a variable | `incr n`, `lappend list x` (the name) | `incr $n`, `lappend $list x` (**the value**: Hecl changes the value in place). `incr n` fails; `lappend l x` quietly changes nothing useful |
| Shared values | Values are copied | Variables can **share one value**: after `set r $q`, `incr $r` changes `$q` too. `set r [copy $q]` makes a separate one. A literal in a proc is fresh each time the proc runs |
| Strings | `string length`, `string range`, `string first`, `string trim`, `string toupper` | `strlen`, `strrange s 1 3`, `strfind needle s` (−1 if missing), `strtrim`, `strupper`, `strlower`, `strindex`, `strreplace`, `split`, `join`, `append` |
| Errors | `error msg`; `catch {...} msg` gives the message | `throw msg`; `catch {...} e` gives a list, `{ERROR message line} {command line} ...`, so the message is `[lindex [lindex $e 0] 1]` |
| Arrays and dictionaries | `array set`, `dict` | Hashes: `set h [hash {a 1 b 2}]`, `hget $h a`, `hset $h c 3`, `hcontains $h a`, `hkeys $h` |
| Procs | Default arguments: `proc greet {who {greeting Hello}}` | No defaults: `{greeting Hello}` is accepted, then fails when called. `args` as the last parameter collects the rest |
| Running at the top level | `uplevel #0 $code` | `upeval 0 $code` |
| Text to number | `scan`, `string is integer` | `int $text` (fails on words, but turns `3.5` into 3) |
| Not there | | `expr`, `string`, `regexp`, `format`, `info`, `array`, `dict`, `scan`, `file`, `open`, `source`, `exit` (Skylight removes it), `after` (removed) |

## Hecl and Java

Skylight loads Hecl's `java` and `null` commands into every app's interpreter, and adds [`bind`](app-api.md#events-bind) for events.

| Hecl | Java |
| --- | --- |
| `$note setText Hello` | `note.setText("Hello")` |
| `set text [$note getText]` | `String text = note.getText()` |
| `$note settext Hello` | the same: method names aren't case-sensitive |
| `java javax.swing.JTree JTree` | makes a command, `JTree`, for the class |
| `JTree -new [list]` | `new JTree()` |
| `Color -new [list 48 128 224]` | `new Color(48, 128, 224)` (after `java java.awt.Color Color`) |
| `Math max 3 7` | `Math.max(3, 7)`, a static method (after `java java.lang.Math Math`) |
| `Integer -field MAX_VALUE` | `Integer.MAX_VALUE`, a constant |
| `$result -field ok` | `result.ok` |
| `$settings -field notificationTime 3000` | `settings.notificationTime = 3000` |
| `bind $note keyTyped {set changed 1}` | `note.addKeyListener(...)` running Hecl on each typed key; `$event` is the event |
| `[null]` | `null` |

How values cross:

* **Strings, numbers and booleans** become Hecl values: a method returning `String` gives the text, `int` or `Integer` a number, `boolean` gives `1` or `0`. A Java `null` string becomes the empty string.
* **A `List`** becomes a Hecl list (so `[$app listFiles]` works with `foreach` and `llen`), and a Hecl list can be passed where Java wants a `List`.
* **Other objects** stay objects, and are called the same way: `[[$app appFile start.html] toUri] toString`.
* **Arguments are matched to overloads** by their kind: text fits `String`, a whole number `int` (then `long`, `double` or `String`), an object its own class. Two whole numbers fit `Color(int, boolean)`, since a number fits a `boolean`; give three for `Color(int, int, int)`. When nothing fits, the error is `No method matched settext for class javax.swing.JTextArea`.
* **Hecl reaches every public method**, including those of objects whose class is private, such as Luxor's labels or the `Path` from `appFile` (one of [Skylight's fixes](../site16luxorhecl/hecl/README.md#skylights-changes) to Hecl).

## Events and threads

Hecl has nothing like Jacl's `java::bind`; Skylight's `bind` command fills the gap with a Java proxy that implements the listener interface and runs the bound script. Swing calls listeners on its event thread, which is where Skylight runs everything else, so bound scripts run at once, in order, between other events.

Each Hecl interpreter starts a thread of its own when it's made, to run Hecl's timers and background tasks. Skylight removes the commands that would use it (`after`, `twait`, `tnotify`, `bgerror`), and stops the thread when the window closes.

## In Skylight

* **One interpreter per window**, made when the window opens (about 0.3 milliseconds) and stopped when it closes. Two Notes windows don't share variables.
* **Variables:** `app` (the [App API](app-api.md)) and one per `id` in the app's XUL file, all at the top level.
* **Commands:** `command="save"` in the XUL runs the script's proc `save`; `command="choose 3"` runs `choose 3`. The first word must be one of the script's procs (not a built-in command like `puts`).
* **Callbacks** given to the App API are Hecl, usually a proc's name: `$app openDialog "Clear?" clearNote`.
* **Errors** come back as notifications with Hecl's message and where it happened: `something broke (in boom at line 1)`.
* **Files:** `app.hcl` is read as UTF-8.
