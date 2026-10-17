# Limitations

## Behaviour

* **Single user, single computer.** There's no server; each person runs their own copy.
* **Notifications** are kept only while Skylight is running.
* **Long-running code freezes the desktop.** Everything runs on the JavaFX application thread; slow work should go on a background thread (see [App API](app-api.md#threads)).
* **An endless loop in the Terminal** freezes Skylight, since scripts run on the same thread.
* **The WebView engine** in JavaFX 8 is from around 2018, so some current websites may not display correctly.
* **FXML apps are read each time they open**, so edits show up at once. Java apps (`class=`) are loaded once: after rebuilding an `app.jar`, restart Skylight. `app.properties` and `apps/apps.txt` are read at login.
* **Script mistakes appear at run time.** A misspelt function or property in a script is found only when that code runs: when the window opens (a "Cannot open app" notification) or when the button is clicked (JavaFX reports the error; with a console, its message is printed).

## Tied to Java 8, and to Nashorn

The code is written for Java 8 and JavaFX 8. FXML itself works the same in every later JavaFX, but its scripts need a JavaScript engine, and **Nashorn was removed from Java in version 15**. On a newer Java you would need:

* **JavaFX as a separate download** (OpenJFX 11 or later);
* **a JavaScript engine for the scripts**: the standalone Nashorn library (`org.openjdk.nashorn`, for Java 15 and later) on the class path, or GraalJS. FXMLLoader finds any engine registered under the name `javascript`;
* or, without an engine, apps written as Java classes with FXML layouts (`class=` and `loadFxml`), which need no scripts at all.

Nashorn understands ES5.1 (with a few ES6 additions in later Java 8 updates), so scripts use `var` and `function`, not `let` or arrow functions.

## How it was tested

Skylight was built and tested on Linux with the versions it targets:

* **OpenJDK 1.8.0** and **OpenJFX 8u161** (JavaFX 8, with Nashorn, WebView and FXML);
* a virtual X display with **metacity** (GNOME's window manager).

The tests were the Java 8 version's, adapted to find controls by their `fx:id`:

* **Simulated mouse and keyboard**, from the real login window: the home menu opened and closed both ways; opening apps; the Terminal, including clicking **OK** on a dialog whose callback was a JavaScript function; closing a toast; desktop shortcuts (double-clicking, dragging, removing); the window list (hiding, showing, closing); logging out and quitting.
* **An automated driver**: every App API method; the Terminal's output, errors, and variables kept between runs; settings validation; file storage; both dialog answers; a missing app and an FXML file with a mistake (reported with its file); toasts expiring.
* **The Notes tutorial**, as written in this documentation: saving, reopening, the unsaved-changes question, Clear. Also a Java app using `loadFxml`, with an `@FXML` field and a `#save` handler.
* **The Web Browser**, loading a local test page, following a link, going back and forward.
* **`app.getArgs()`**, with arguments passed from a script as a JavaScript object.

Testing found five problems, all fixed: a script's `<` read as XML (scripts now use CDATA), `addListener`'s two versions confusing Nashorn, a JavaScript number reaching a `Spinner` as a `Double`, a script function called `load` hidden by Nashorn's own, and, in the mouse tests, an occasional JavaFX crash when the login window closed during its own key event (it now closes just after). The [FXML primer](fxml-primer.md) explains the first four, since app authors can meet them too.

What wasn't tested:

* **Windows and macOS.** JavaFX and FXML behave the same there, but fonts, window decorations and the `.bat` scripts differ.
* **Newer Java with a separate Nashorn**, as described above.
* **Real websites** in the Web Browser, since the test machine couldn't reach the internet.
