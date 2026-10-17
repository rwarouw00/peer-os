# Limitations

## Behaviour

* **Single user, single computer.** There's no server; each person runs their own copy.
* **Notifications** are kept only while Skylight is running.
* **Long-running code freezes the desktop.** Everything runs on the JavaFX application thread; slow work should go on a background thread (see [App API](app-api.md#threads)).
* **An endless loop in the Terminal** freezes Skylight, since scripts run on the same thread.
* **The WebView engine** in JavaFX 8 is from around 2018, so some current websites may not display correctly.
* **Apps are loaded once.** After rebuilding an `app.jar`, restart Skylight.

## Tied to Java 8

The code is written for Java 8 and JavaFX 8, as requested. Moving to a newer Java would need:

* **JavaFX as a separate download** (OpenJFX 11 or later), since it was removed from the JDK;
* **a replacement for Nashorn** in the Terminal (removed in Java 15), such as the standalone Nashorn library or GraalJS;
* small changes to the class-path-based app loading if modules are used.

The rest is ordinary Java and JavaFX code that later versions still support.

## How it was tested

There was no Windows machine available. Skylight was built and tested on Linux with the versions it targets:

* **OpenJDK 1.8.0_504**, compiled with `-source 8 -target 8`;
* **OpenJFX 8u161** (JavaFX 8, including WebView);
* a virtual X display with **metacity** (GNOME's window manager).

The tests used the real login window (including a wrong password) and drove the desktop with simulated mouse and keyboard input:
* the home menu, opened and closed both ways;
* opening apps;
* the Terminal, including clicking **OK** on a dialog whose callback was a JavaScript function;
* closing a toast;
* desktop shortcuts: double-clicking, dragging and removing;
* the window list: hiding, showing and closing;
* logging out and quitting.

An automated driver also checked every App API method, settings validation, file storage, loading an app from its own `app.jar` (the Notes tutorial), and the Web Browser loading a page. The screenshots in this documentation come from those runs.

What wasn't tested:

* **Windows itself.** JavaFX looks and behaves almost the same on Windows, but fonts, window decorations and `.bat` scripts differ.
* **Oracle JDK 8 or other JavaFX 8 builds.** Any Java 8 with JavaFX 8u40 or later should work, because Skylight uses `Alert` and `Spinner`, which arrived in 8u40.
* **Real websites** in the Web Browser, since the test machine couldn't reach the internet; a local test site was used.
