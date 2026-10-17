# Architecture

## One Java process

```mermaid
flowchart TB
    Main["Main<br/>(JavaFX Application)"] --> Desktop
    subgraph JVM["One Java 8 process"]
        Desktop["Desktop<br/>login, panel, menu, notifications, windows"]
        Storage["Storage + Settings<br/>users, settings, files"]
        T["TerminalApp extends App"]
        W["WebBrowserApp extends App"]
        N["notes.NotesApp extends App<br/>(loaded from apps/notes/app.jar)"]
        Desktop -- "creates, owns" --> T
        Desktop -- "creates, owns" --> W
        Desktop -- "creates, owns" --> N
        T -- "method calls" --> Desktop
        Desktop --> Storage
        T --> Storage
    end
    Storage --> Data[("data/<br/>.properties files")]
    Desktop --> FX["JavaFX 8<br/>Stages, controls, CSS"]
```

| Class | File | Job |
| --- | --- | --- |
| `Main` | [`Main.java`](../site5jfx/src/skylight/Main.java) | The JavaFX `Application`: keeps JavaFX running after windows close, and shows the login window |
| `Desktop` | [`Desktop.java`](../site5jfx/src/skylight/Desktop.java) | Login, panel, background, shortcuts, home menu, notifications, dialogs, app windows. See [Desktop internals](desktop-internals.md). |
| `App` | [`App.java`](../site5jfx/src/skylight/App.java) | Base class for apps; the [App API](app-api.md) |
| `Settings` | [`Settings.java`](../site5jfx/src/skylight/Settings.java) | A user's background, notification time and shortcuts |
| `Storage` | [`Storage.java`](../site5jfx/src/skylight/Storage.java) | Users and passwords, settings, app list, user files under `data/` |
| `AddUser` | [`AddUser.java`](../site5jfx/src/skylight/AddUser.java) | Command-line tool to add users |
| Built-in apps | [`apps/`](../site5jfx/src/skylight/apps/) | `TerminalApp`, `SettingsApp`, `WebBrowserApp` |
| Styles | [`skylight.css`](../site5jfx/src/skylight/skylight.css) | The few styles Skylight adds to JavaFX's default Modena theme |

Everything is compiled into one `skylight.jar`. The `apps/`, `images/` and `data/` folders sit beside it.

## How it runs

1. `Main.start()` calls `Platform.setImplicitExit(false)`, so logging out (which closes every window) doesn't end the program, then shows the login window.
2. A correct password calls `Desktop.showDesktop()`, which builds a maximized `Stage` (panel and desktop area), the home menu (a `Popup`), and starts a one-second clock `Timeline`.
3. From here, JavaFX calls Skylight's code on its application thread when something happens: a click, a timer, a window being closed.
4. Logging out closes everything and shows the login window again. **Quit** calls `Platform.exit()`.

## Apps are classes

`Desktop.openWindow("apps/terminal/", args)`:

1. reads `apps/terminal/app.properties` (see [App manifest](app-manifest.md));
2. loads the class it names: from `skylight.jar` for built-in apps, or from the folder's own `app.jar` for others (using a `URLClassLoader`);
3. creates a `Stage` owned by the desktop (`initOwner`), so it stays in front of it;
4. gives the app its context (`App.init`) and calls `build()`, which returns the window's contents;
5. adds a toggle button to the window list, shows the window and brings it to the front.

AppIDs are numbers, as strings, counting up from 1.

Apps call the desktop through the methods they inherit from `App`. Callbacks are lambdas (`() -> save()`), and Nashorn passes JavaScript functions as the same `Runnable`.

## Real windows

Each app window is a real top-level window, so the operating system moves, resizes, stacks and minimizes it. The desktop only keeps the window list in step.

## Data

There's no server. `Storage` reads and writes `.properties` files in `data/`, Java's built-in format for settings (see [Data and configuration](data-and-configuration.md)).
