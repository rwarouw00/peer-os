# Limitations

## Behaviour

* **Single user, single computer.** There's no server; each person runs their own copy.
* **Notifications** are kept only while Skylight is running.
* **Long-running code freezes the desktop.** Scripts, commands and events all run on Swing's event thread, so an endless loop in the Terminal freezes Skylight.
* **App windows have no minimize button.** They're dialogs owned by the desktop window, so the operating system doesn't give them taskbar buttons either; hide them with the window list instead.
* **The Web Browser** shows HTML 3.2 with a little CSS and no JavaScript. Modern pages look wrong: the test page's styled box below came out as a solid blue rectangle.

![A modern test page in the Web Browser](images/web-browser-test.png)

* **XUL files and scripts are read each time an app opens**, so edits show up at once. `app.properties` and `apps/apps.txt` are read at login.
* **Script mistakes appear at run time**: when the window opens (a "Cannot open app" notification) or when a button is clicked ("Error in ...").

## Luxor's remaining limits

The improvements are listed in [Luxor, improved](luxor-improved.md#whats-left), with what's still Luxor's own: one global table of definitions and actions, errors that are only logged, a fixed set of tags, inline styles only, and the HTML 3.2 viewer. Only the parts of Luxor that Skylight uses were tested, before and after the changes.

## Hecl's limits

* **Not Tcl.** Tcl habits fail in Hecl, some loudly and some quietly; the [Hecl primer](hecl-primer.md#where-hecl-differs-from-tcl) lists them. The quiet ones are the ones to watch: `lappend l x` (the name, not `$l`) changes nothing useful, `set r $q` followed by `incr $r` changes `$q` too, `int 3.5` gives 3, and two whole numbers fit `Color(int, boolean)`.
* **A small set of commands.** No `regexp`, `format`, `info`, `array` or `dict`, no default values for proc arguments, and no `expr`. Java fills some gaps, at the cost of longer code: after `java java.util.regex.Pattern Pattern`, `Pattern matches {[0-9]+} $text` gives `1` or `0`, and after `java java.lang.String String`, `String format {%s items} [list 3]` gives `3 items`.
* **Errors name places, not files.** A Hecl error gives its message, a line within the proc or script it happened in, and the commands it came through: `Variable nope does not exist (line 1, in inner at line 3, in outer at line 6)`. The file name is added only when a script fails as its window opens.
* **No test for a Java `null` object.** `[null]` makes one, but Hecl can't compare a value with it. Skylight's App API returns empty text, never `null`, for missing text.
* **Hecl's Java bridge** had the problems listed in [`hecl/README.md`](../site16luxorhecl/hecl/README.md#skylights-changes), all found while building this version and fixed. Overloads it can't tell apart, such as `JPanel.add(Component)` and `add(PopupMenu)`, are chosen by whether the object fits, and otherwise by the order Java lists the methods.
* **An interpreter has its own idle thread**, which wakes about once a second until the window closes. Skylight removes the commands that would use it.
* **Java 8.** Everything was built and tested for Java 8. Hecl has no Java 8-specific parts, but newer Javas weren't tried.
* **Hecl is frozen.** Its last change was in September 2010.

## How it was tested

Skylight was built and tested on Linux with:

* **OpenJDK 1.8.0** (Swing, Metal look);
* **Luxor 1.0 beta 8** built from its source with Skylight's changes by `tools/get-libs.sh`. `tools/get-libs.ps1`, run with PowerShell 7, built the same: the downloaded libraries byte for byte, and Luxor with byte-identical classes;
* **Hecl** from its git repository, preprocessed for desktop Java and compiled with Skylight by `build.sh`;
* a virtual X display with **metacity** (GNOME's window manager).

**Hecl on its own** was tried first: preprocessed from its repository, it compiled for Java 8 once its console stopped using the jline library. Small Java programs then checked what apps need, one script line at a time: Java objects as variables, calling methods with every kind of argument, `List` results, fields, static methods and constructors, `null`, errors and `catch`, `upeval`, `global`, how `if`, `incr` and `lappend` behave, and the Tcl commands Hecl lacks. That found the problems in Hecl's Java bridge listed in [`hecl/README.md`](../site16luxorhecl/hecl/README.md#skylights-changes); after each fix the same lines were run again.

The final tests ran on a fresh copy of the `site16luxorhecl` folder, with `tools/get-libs.sh` downloading and building Luxor again, `build.sh` compiling Skylight and Hecl, and the Notes app copied out of [Building your first app](building-apps.md) by a script:

* **An automated driver** working the real Swing controls: logging in (a wrong password, then the right one); the home menu; the Terminal (output from `puts`, a command's result, an error with its line, variables kept between runs, a Java call, files including an accented character, a refused file name, a missing file); dialogs with Cancel and OK, and a message-only one; a missing app, a XUL file with an XML mistake (reported with its line), a script with an error (reported with Hecl's message); `openWindow` with arguments, read back with `getArgs` through `foreach` and a hash; the Notes tutorial (save, the unsaved-changes question, reopening, Clear with OK, closing unchanged, and the window list afterwards); shortcuts, including a duplicate and a non-app; Settings (a background, 3 seconds, a refused 99, a non-number); the Web Browser (its start page, a test page, a second page, Back, Reload, following a link); the window list (hiding and showing); toasts showing their text, expiring after 3 seconds and closing with ×; logging out.
* **Hecl in Skylight**: one interpreter thread per window, stopped when the window closes and at logout; a second Terminal not seeing the first one's variables; `exit`, `after` and `twait` refused; errors inside procs, inside a bound event (an "Error in Terminal" notification) and from `throw` in a command; Tcl habits that fail (`incr n`, `lappend l x`, `if {$c}`, a missing `]`, a missing `global`); `bind` to an event that doesn't exist, and a bound listener removed again; numbers passed to `String` parameters, strings and objects to `Object` parameters, methods of a private class (the `Path` from `appFile`); Settings refusing 3.5 and accepting " 5 "; `command="puts"` refused because `puts` isn't one of the script's procs; the App API examples from [App API](app-api.md) (shortcuts from `getSettings`, the `writeFile` check, `canClose`, a gadget's `JTree`); and making and stopping 200 interpreters, to time them.
* **The improved Luxor in an app**: a label growing from 35 to 210 pixels without being cut off, a text box held to `max-width: 60` next to one that takes the rest, a button tooltip, a `#rrggbb` colour and a Java font name, a button without a label, a gadget filled with a `JTree`, a command with no proc, and a `throw` inside a command.
* **Real mouse and keyboard**, through xdotool, three times: typing the login; the home menu opening and closing with a click outside; opening Notes from the menu, typing, clicking Save; Alt+F4 with unsaved changes (the question, then Enter for OK, then checking the window list); typing Hecl into the Terminal and clicking Run; dragging a shortcut (its new position saved) and double-clicking it; **Close** from the window list's right-click menu; closing the desktop window to log out; Escape to quit.
* **Luxilla**, through `tools/preview.sh`, showing every window's layout with the improved Luxor.

Building and testing found these problems, all fixed:

* in Hecl's Java bridge: the twelve listed in [`hecl/README.md`](../site16luxorhecl/hecl/README.md#skylights-changes). The ones that showed in Skylight itself were a number refused by `setText` (the Settings app's seconds), a string arriving as Hecl's internal value in an `Object` parameter (the Web Browser's Reload), and a `Path`'s methods not found (the Web Browser's start page, which failed to open);
* in Hecl itself: its console needing the jline library, each interpreter's thread keeping Java running unless stopped, and a static list growing with every window;
* in Skylight: error messages that printed a Java object's whole description as the "command" an error came through, and showed `(line )` for errors without a line.

What wasn't tested:

* **Windows and macOS**, and the `.bat` scripts. **Windows PowerShell 5.1** for `get-libs.ps1`.
* **Java versions other than 8.**
* **Parts of Hecl that Skylight doesn't use or include**: its file and network commands, its phone and Android parts, and its own test suite, which wasn't run.
* **Parts of Luxor that Skylight doesn't use** (menus, trees, tables, portals and the rest), with or without the changes.
* **Real websites** in the Web Browser, since the test machine couldn't reach the internet beyond SourceForge.
