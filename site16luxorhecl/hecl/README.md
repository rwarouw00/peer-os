# Hecl, for Skylight

[Hecl](https://github.com/davidw/hecl) is a scripting language written in Java by David N. Welton (with Wolfgang Kechel and others), meant first for mobile phones (Java ME) and also built for desktop Java. It reads like Tcl. It is under the **Apache License 2.0** ([`LICENSE`](LICENSE), [`NOTICE`](NOTICE)).

`src/` holds the two parts of Hecl that Skylight uses, 46 source files, which `build.sh` compiles into `skylight.jar`:

| Package | What it is |
| --- | --- |
| `org.hecl` | The language: the interpreter, and its commands for variables, procs, control, lists, strings, hashes and maths |
| `org.hecl.java` | The `java` command and calls on Java objects (`$label setText Hi`), and `null` |

Hecl's file commands, its networking, its phone and Android parts and its command-line program aren't included.

## Where it comes from

* **Hecl's git repository**, `github.com/davidw/hecl`, at commit `ba81ffc` (10 September 2010), its last change. Hecl's last release, `hecl-20080730`, is older; the repository has two more years of fixes.
* **Preprocessed for desktop Java.** Hecl's source has Antenna preprocessor lines (`//#if j2se ... //#else ... //#endif`) that choose between the phone and desktop versions. [`preprocess.py`](preprocess.py) resolved them the way Hecl's own build does for desktop Java (`j2se`, Java 1.8), keeping only the desktop lines, from Hecl's `core/` and `java/` folders:
  `python3 preprocess.py <hecl checkout> <output folder> core java`
* **Then changed for Skylight**, in three files. Every change is marked with a `Skylight:` comment, and [`skylight.patch`](skylight.patch) shows them as a diff against the preprocessed source.

## Skylight's changes

| File | Change |
| --- | --- |
| `Interp.java` | The console (`readEvalPrint`) uses Hecl's own simple line reader instead of the jline library, so Hecl needs nothing else. The interpreter's thread is a daemon, so it never keeps Java running |
| `java/JavaCmd.java` | The automatic class commands made for each window's interpreter are no longer added to a static list that grew for as long as Java ran |
| `java/Reflector.java` | How Hecl calls Java, below |

Hecl calls Java methods through `java/Reflector.java`, which matches Hecl values to a method's parameters and turns the result back into a Hecl value. Testing it for Skylight found these problems, now fixed:

* The matched arguments were kept in one **static** array, shared by every interpreter and thread.
* When choosing between **overloaded methods**, only the last argument decided the match.
* **`null`** matched `int` and `boolean` parameters (and then failed).
* An object matched any parameter type, so the wrong overload could be picked; it now has to fit.
* An `int` didn't fit a `long` or `double` parameter, and a number or list didn't fit a `String` parameter (`$label setText 42` failed). Both now fit, tried after exact matches.
* **A string passed to an `Object` parameter** arrived as Hecl's own internal value; it is now a `java.lang.String` (and a number an `Integer`, `Long` or `Double`).
* A string argument that happened to match one of the class's constant names (such as `CENTER`) was replaced by the constant, even for `String` parameters.
* **Boxed results** (`Integer`, `Double`, `Boolean` and so on, as from `List.get`) came back as opaque objects instead of numbers.
* A `null` element in a returned `List` crashed the conversion.
* A Hecl list couldn't be passed to a `List` or `Collection` parameter.
* **Methods of non-public classes** couldn't be called, though they're public methods: a `Path` is really a private `sun.nio.fs.UnixPath`, and Luxor's labels are private subclasses of `JLabel`. Each method is now called through the public class or interface that declares it.
* Errors from the called method were wrapped twice, listed every argument, and failed with a `NullPointerException` on a `null` argument; they are now short: `Path.toUri: java.io.IOError: ...`.
