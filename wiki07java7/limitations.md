# Limitations

## Behaviour

* **Single user, single computer.** There's no server; each person runs their own copy.
* **Notifications** are kept only while Skylight is running.
* **Long-running code freezes the desktop.** Everything runs on the JavaFX application thread; slow work should go on a background thread (see [App API](app-api.md#threads)).
* **An endless loop in the Terminal** freezes Skylight, since scripts run on the same thread.
* **The desktop window isn't maximized**, just sized to the screen, because JavaFX 2.2 can't maximize. If the screen resolution changes, log out and in again.
* **The WebView engine** in JavaFX 2.2 is from 2012: many current websites display badly, and some HTTPS sites won't load with Java 7's TLS settings.
* **Apps are loaded once.** After rebuilding an `app.jar`, restart Skylight.

## Tied to Java 7

The code is written for Java 7 and JavaFX 2.2, as requested, and runs unchanged on Java 8 with JavaFX 8. Beyond that, the same limits as the Java 8 version apply: JavaFX is a separate download from Java 11, and Nashorn and Rhino are both gone from the JDK, so newer Javas would need OpenJFX and a JavaScript library for the Terminal.

## How it was tested

There was no Java 7 with JavaFX 2.2 available (Oracle's downloads need an account, and OpenJDK 7 never included JavaFX), so the code was checked in three ways:

1. **Java 7 language and library:** compiled with `javac --release 7`, which accepts only Java 7 syntax and Java 7's standard library (no lambdas, no `java.util.Base64`, no streams), with all lint warnings on. It compiled cleanly.
2. **JavaFX 2.2 API:** a script listed every JavaFX class, constructor, method and field used by the compiled classes and looked each one up in the OpenJFX 8 sources, which mark everything added after JavaFX 2.2 with `@since JavaFX 8.0` or `@since JavaFX 8u40`. Nothing used by Skylight or the Notes tutorial is newer than JavaFX 2.2. (Run on the Java 8 version, the same script finds exactly the JavaFX 8 features this version replaces.)
3. **Running it:** on Linux with OpenJDK 1.8.0 and OpenJFX 8u161, using JavaFX's **Caspian** theme (JavaFX 2.2's look) through `-Djavafx.userAgentStylesheetUrl=CASPIAN`, on a virtual X display with metacity.

The running tests used the real login window (including a wrong password) and drove the desktop with simulated mouse and keyboard input: the home menu, opened and closed both ways; opening apps; the Terminal, including clicking **OK** on a dialog whose callback was a JavaScript function; closing a toast; desktop shortcuts (double-clicking, dragging and removing); the window list (hiding, showing and closing); logging out and quitting. An automated driver also checked every App API method, settings validation, file storage, both dialog answers, loading the Notes tutorial from its own `app.jar` (including the unsaved-changes question), and the Web Browser loading a local test page, following a link and going back and forward. The screenshots in this documentation come from those runs.

Rhino was tested separately: Mozilla Rhino 1.7R3, the release Java 7's built-in engine is based on, passed a JavaScript function to a Java method that expects a `Runnable`, as the Terminal's callbacks need, and returned `6 * 7` as `42.0`. Passwords were checked against Python's PBKDF2-SHA256.

What wasn't tested:

* **A real Java 7 runtime with JavaFX 2.2**, including Java 7's own Rhino-based script engine. JavaFX 2.2's rendering differs in small ways from JavaFX 8 with Caspian.
* **Windows**, including the `.bat` scripts. JavaFX looks and behaves almost the same there, but fonts and window decorations differ.
* **Real websites** in the Web Browser, since the test machine couldn't reach the internet.
