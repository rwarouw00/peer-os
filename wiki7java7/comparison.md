# Compared with the other versions

Skylight OS exists in several restructured forms, all with the same design and App API names:

* **site3**: browser version: ES5 JavaScript, HTML and CSS with a PHP server ([`../wiki3`](../wiki3/README.md))
* **site4phpgtk**: desktop program in PHP 5.2 with PHP-GTK2 ([`../wiki4phpgtk`](../wiki4phpgtk/README.md))
* **site5jfx**: desktop program in Java 8 with JavaFX 8 ([`../wiki5jfx`](../wiki5jfx/README.md))
* **site6cs**: desktop program in C# 5 with Windows Forms ([`../wiki6cs`](../wiki6cs/README.md))
* **site7java7**: desktop program in Java 7 with JavaFX 2.2 (this one)

## Next to the Java 8 version

This version is the closest relative of site5jfx: the same classes, files, data format, apps and App API, written for the platform one step older.

| | site5jfx (Java 8, JavaFX 8) | site7java7 (Java 7, JavaFX 2.2) |
| --- | --- | --- |
| Event handlers | Lambdas: `b.setOnAction(e -> save())` | Anonymous classes: `new EventHandler<ActionEvent>() {...}` |
| `openDialog` callback | `() -> ...` | `new Runnable() {...}` |
| Dialogs | JavaFX `Alert` | A small window Skylight builds |
| Notification time | `Spinner` | `Slider` |
| Filling the screen | `setMaximized(true)` | Sized to the screen's visual bounds |
| Look | Modena | Caspian |
| Terminal engine | Nashorn | Rhino (Nashorn when run on Java 8) |
| Password hashing | `PBKDF2WithHmacSHA256` | The same algorithm, built from `HmacSHA256` |
| Starting it | `java -jar skylight.jar` | `jfxrt.jar` must be added to the class path (the scripts do it) |
| Size | ~1,070 lines of Java | ~1,340 lines of Java (anonymous classes are longer) |

Apps written for this version also run on site5jfx (Java 8 runs Java 7 code). The other way round, a site5jfx app needs its lambdas rewritten and any JavaFX 8 classes replaced (see the [primer](javafx-primer.md#missing-from-javafx-22)).

## All versions side by side

| | site3 (browser) | site4phpgtk (PHP-GTK2) | site5jfx (JavaFX 8) | site6cs (WinForms) | site7java7 (JavaFX 2.2) |
| --- | --- | --- | --- | --- | --- |
| Language | ES5, HTML, CSS, PHP | PHP | Java 8, CSS | C# 5 | Java 7, CSS |
| Runs as | Web pages + web server | One PHP process | One Java process | One .exe | One Java process |
| App windows | Drawn in the page | Real windows | Real windows | Real windows | Real windows |
| Apps are | HTML pages in iframes | PHP classes | Java classes or `app.jar` | C# classes or `app.dll` | Java classes or `app.jar` |
| Callbacks | Functions relayed by message | `array($obj, 'method')` | Lambdas | Lambdas | Anonymous classes |
| Terminal runs | JavaScript (browser) | PHP | JavaScript (Nashorn) | C# | JavaScript (Rhino) |
| Web Browser | Pages in a frame | System browser | WebView (WebKit, ~2018) | IE 11 engine | WebView (WebKit, 2012) |
| Data format | JSON | JSON | `.properties` | JSON | `.properties` |
| Passwords | bcrypt | PBKDF2-SHA256 | PBKDF2-SHA256 | PBKDF2-SHA256 | PBKDF2-SHA256 |

## What it costs to go back to Java 7

* **More code.** Each one-line lambda becomes a five-line anonymous class.
* **A platform that's long out of support.** Public updates for Java 7 ended in 2015, and its JavaFX and web engine are older still. This version suits a machine that is fixed on Java 7; for anything else, the Java 8 version is the better choice.
* **More set-up.** JavaFX 2.2 isn't on Java 7's class path, so the scripts need `JAVA_HOME`.

## Notes in every version

The same Notes app, written for all fourteen versions, is compared side by side in [Notes, in every version](../wikicompare/README.md).
