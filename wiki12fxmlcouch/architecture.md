# Architecture

## One Java process, and an app store

```mermaid
flowchart TB
    Main["Main<br/>(JavaFX Application)"] --> Desktop
    subgraph JVM["One Java 8 process"]
        Desktop["Desktop (Java)<br/>controller of the ui/ files"]
        UI["ui/*.fxml<br/>login, desktop, menu, notification"]
        Storage["Storage + Settings<br/>users, settings, files"]
        FxmlApp["FxmlApp<br/>(one per FXML app window)"]
        Apps["app.fxml + .js<br/>Terminal, Settings, Web Browser, Notes"]
        Nashorn["Nashorn<br/>(JavaScript)"]
        Desktop -- "loads" --> UI
        Desktop -- "creates" --> FxmlApp
        FxmlApp -- "loads, with app = itself" --> Apps
        Apps -- "scripts run in" --> Nashorn
        Apps -- "app.postNotification() ..." --> FxmlApp
        FxmlApp --> Desktop
        Desktop --> Storage
    end
    Storage --> Data[("data/<br/>.properties files")]
    Store[("CouchDB app store<br/>one document per app,<br/>files attached")] -- "HTTP: apps.txt, app.properties,<br/>app.fxml, .js, icons, app.jar" --> Storage
    Store -. "read over HTTP" .-> Apps
```

| Part | Where | Job |
| --- | --- | --- |
| `Main` | [`Main.java`](../site12fxmlcouch/src/skylight/Main.java) | The JavaFX `Application`: keeps JavaFX running after windows close, and shows the login window |
| `Desktop` | [`Desktop.java`](../site12fxmlcouch/src/skylight/Desktop.java) | Loads the `ui/` files and is their controller; shortcuts, notifications, dialogs, app windows. See [Desktop internals](desktop-internals.md). |
| `App` | [`App.java`](../site12fxmlcouch/src/skylight/App.java) | The [App API](app-api.md), and the base class for Java apps |
| `FxmlApp` | [`FxmlApp.java`](../site12fxmlcouch/src/skylight/FxmlApp.java) | The `App` behind every FXML app: loads its `app.fxml` with `app` set to itself |
| `Storage` | [`Storage.java`](../site12fxmlcouch/src/skylight/Storage.java) | Users, settings and files, as in the Java 8 version; and `appResource`, which turns an app file into a URL in the store (or in `apps/`) |
| `Settings`, `AddUser` | `Settings.java`, `AddUser.java` | As in the Java 8 version |
| The desktop's windows | [`ui/`](../site12fxmlcouch/ui/) | `login.fxml`, `desktop.fxml`, `menu.fxml`, `notification.fxml` and `skylight.css` |
| Built-in apps | [`apps/`](../site12fxmlcouch/apps/) | `terminal/`, `settings/`, `web-browser/`: each an `app.properties`, an `app.fxml` and icons; the Terminal's script is in `terminal.js`. This is the apps' source: they run from the store once pushed. |
| The app store | CouchDB, [`store/`](../site12fxmlcouch/store/), [`tools/`](../site12fxmlcouch/tools/) | One document per app, filled by `tools/push-apps`; see [The app store](app-store.md) |

The Java code is compiled into `skylight.jar`. The desktop's windows (`ui/`), `images/`, `data/` and `skylight.properties` are plain files beside it. The apps are plain files too, but in the store: the computer running Skylight doesn't need an `apps/` folder.

## How it runs

1. `Main.start()` calls `Platform.setImplicitExit(false)`, so logging out doesn't end the program, then `Desktop.showLogin()`.
2. `showLogin()` loads `ui/login.fxml` with the `Desktop` as its controller: the fields `username` and `password` are filled in from the file's `fx:id`s, and its buttons call the methods `logIn()` and `quit()`.
3. A correct password loads `ui/desktop.fxml` (the panel and the desktop area) into a maximized window, and `ui/menu.fxml` into a `Popup`, then adds the app icons, background and shortcuts in code, and starts the clock.
4. From here, JavaFX calls Skylight's code, or an app's script, when something happens.
5. Logging out closes everything and shows the login window again; **Quit** calls `Platform.exit()`.

## Apps are files

`Desktop.openWindow("apps/terminal/", args)`:

1. reads the manifest, `<store>terminal/app.properties` (see [App manifest](app-manifest.md)), once per login;
2. creates the app object: an `FxmlApp` if the manifest says `fxml=`, or the named Java class if it says `class=`;
3. creates a `Stage` owned by the desktop, and gives the app its context;
4. calls `build()`. For an `FxmlApp`, that's an `FXMLLoader` reading `<store>terminal/app.fxml` with the variable `app` set to the `FxmlApp`. Scripts named with `source=` and images named with `@` are fetched from beside it, in the store. The file builds the window's contents, and its `<fx:script>` runs, defining the functions its buttons call;
5. adds a toggle button to the window list, shows the window and brings it to the front.

If the FXML has a mistake, `FXMLLoader` reports the file and line, and the user gets that in a "Cannot open app" notification. If the store can't be reached, the notification says so.

Each window gets its own `FXMLLoader`, and so its own JavaScript engine: two Notes windows don't share variables. The files are read each time a window opens, so an app updated in the store shows its changes the next time it's opened, without restarting Skylight.

## Scripts and Java

An app's script calls the App API on `app` like any Java object (`app.writeFile('notes.txt', text)`), and Nashorn passes JavaScript functions to Java as callbacks (`app.openDialog(message, function () { ... })`). `FxmlApp` is only a few lines: everything an FXML app can do is in `App`. See the [FXML primer](fxml-primer.md#javascript-and-java) for where the two languages meet.

## Real windows

Each app window is a real top-level window, so the operating system moves, resizes, stacks and minimizes it. The desktop only keeps the window list in step.

## Data

Users' data stays local, as in the Java 8 version: `Storage` reads and writes `.properties` files in `data/` (see [Data and configuration](data-and-configuration.md)).

## Where the store fits

Only `Storage.appResource(path, name)` and `Storage.appList()` know where apps come from. Everything else asks them for a URL:

| Code | Asks for |
| --- | --- |
| `Desktop.readManifest` | `app.properties` |
| `Desktop.appImage` | icons, for the menu, shortcuts, window list and window icons |
| `Desktop.loadAppClass` | `app.jar`, through a `URLClassLoader` |
| `App.loadFxml`, so `FxmlApp` | `app.fxml`; `FXMLLoader` resolves `source=` and `@` against it |
| `App.appUrl` | any of the app's files, for scripts |

So the store could be swapped for any other source of URLs without touching the rest: [The app store](app-store.md#without-couchdb) shows a plain web server doing the job.
