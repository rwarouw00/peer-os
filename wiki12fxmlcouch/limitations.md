# Limitations

## Behaviour

* **Apps are shared, data isn't.** The store serves apps to many desktops, but each computer keeps its own users, settings and files in `data/`; logging in on another computer doesn't bring them along.
* **No network, no apps.** Skylight doesn't keep a copy of the store. Apps already open keep working, but nothing new can be opened until the store is back. To work offline, replicate the store to a CouchDB on the same computer.
* **Opening an app waits for the store.** The files are fetched on JavaFX's thread, so a slow or unreachable server makes the desktop pause while an app opens (a server that refuses the connection fails at once, but Java's default is to wait for a silent one with no time limit).
* **Notifications** are kept only while Skylight is running.
* **Long-running code freezes the desktop.** Everything runs on the JavaFX application thread; slow work should go on a background thread (see [App API](app-api.md#threads)).
* **An endless loop in the Terminal** freezes Skylight, since scripts run on the same thread.
* **The WebView engine** in JavaFX 8 is from around 2018, so some current websites may not display correctly.
* **FXML apps are read each time they open**, so a pushed update shows up at once. Java apps (`class=`) are loaded once per window from `app.jar`. `app.properties` and `apps.txt` are read at login.
* **No caching.** Every time an app opens, its files are downloaded again. They're small (the Terminal is about 9 KB with its icons), so this is quick on a local network, but there's no offline copy.
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
* **CouchDB 2.3.1** for the store, on the same computer;
* a virtual X display with **metacity** (GNOME's window manager).

For every test except the local-folder one, the Skylight folder had **no `apps/` folder**, so everything had to come from the store. The apps were pushed with `tools/push-apps.sh`; `tools/push-apps.ps1`, run with PowerShell 7, produced an identical store.

The tests were the FXML version's, run against the store:

* **Simulated mouse and keyboard**, from the real login window, three times: the home menu opened and closed both ways; opening apps; the Terminal, including clicking **OK** on a dialog whose callback was a JavaScript function; closing a toast; desktop shortcuts (double-clicking, dragging, removing); the window list (hiding, showing, closing); logging out and quitting.
* **An automated driver**: every App API method; the Terminal; settings; file storage; both dialog answers; a missing app, and an FXML file with a mistake (reported as `/skylight-apps/broken/app.fxml`); toasts expiring.
* **The Notes tutorial** from the store: saving, reopening, the unsaved-changes question, Clear. Also a **Java app** whose `app.jar` was loaded from the store, using `loadFxml`.
* **The Web Browser**, loading a local test page, following a link, going back and forward; and **`app.getArgs()`**.
* **Live updates**: with Skylight running, Notes' `app.fxml` was changed and only `notes` pushed; the next Notes window showed the change.
* **A replica**: the store replicated to a second database with `_replicate`, and Skylight pointed at it with `-Dskylight.appStore=`.
* **A plain web server**: the same files served by `python3 -m http.server`, including the Java app.
* **An unreachable store**: an empty home menu, and "Cannot open app" when opening one.
* **The local `apps/` folder**, with `-Dskylight.appStore=` empty: the automated driver again.
* **The store's rule**, with curl: anonymous users and an ordinary CouchDB user couldn't change an app, add one, or change `_design/store` (401 or 403); anonymous reading worked.

Testing found two problems, both fixed: the Settings app's background pictures, which used `@../../images/...` and so pointed into the store, now use `app.desktopUrl`; and an app missing from an unreachable store was reported as "not an app folder", now "isn't in the app store at ..., or the store can't be reached".

What wasn't tested:

* **Windows and macOS**, and the `.bat` scripts. **Windows PowerShell 5.1** for `push-apps.ps1`.
* **CouchDB 1.6 and 3.x**, and a store on another computer. The store uses only plain document and attachment requests, which those versions support too.
* **HTTPS** for the store.
* **A slow network.**
* **Newer Java with a separate Nashorn**, as described above.
* **Real websites** in the Web Browser, since the test machine couldn't reach the internet.
