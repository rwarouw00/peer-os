# Getting started

## Requirements

* **A Java 8 JDK that includes JavaFX 8**, on each computer that runs Skylight. Not every Java 8 does:
  * **Oracle JDK 8** includes JavaFX.
  * **OpenJDK 8** usually doesn't. Use a build that adds it, such as Azul **Zulu 8 "FX"** or BellSoft **Liberica 8 "Full"**, or add **OpenJFX 8** to an OpenJDK 8.

  Check with `java -version` (it should say 1.8) and by building: the build fails straight away with "package javafx... does not exist" if JavaFX is missing.
* **CouchDB 1.6 or later**, with an administrator account, for the app store. One server can serve any number of desktops. (To try Skylight without CouchDB, leave `appStore=` empty in `skylight.properties` and it uses the `apps/` folder, as the [FXML version](../wiki11fxml/README.md) does.)
* To fill the store: **bash and curl** (Linux, macOS, or Git Bash on Windows), or **PowerShell**.

## Build

From the `site12fxmlcouch` folder:

```
build.bat          (Windows)
./build.sh         (Linux, macOS)
```

This compiles `src/` into `skylight.jar`. It needs `javac` and `jar` from the JDK on your `PATH`.

## Add a user

```
java -cp skylight.jar skylight.AddUser admin choose-a-password
```

Run it again to change a password. Usernames may use letters, digits, `-` and `_`. Skylight's users are separate from CouchDB's: they're kept in `data/` on each computer.

## Fill the app store

With CouchDB running, push the apps in `apps/` to a new database, using a CouchDB administrator's name and password:

```
tools/push-apps.sh http://admin:password@127.0.0.1:5984/skylight-apps
```

On Windows:

```
powershell -ExecutionPolicy Bypass -File tools\push-apps.ps1 http://admin:password@127.0.0.1:5984/skylight-apps
```

```
Uploaded settings
Uploaded terminal
Uploaded web-browser
The app store is ready: http://127.0.0.1:5984/skylight-apps/
```

Open `http://127.0.0.1:5984/skylight-apps/terminal/app.fxml` in a web browser to see an app's file served by CouchDB. See [The app store](app-store.md) for what was created.

## Point Skylight at the store

`skylight.properties`, beside `skylight.jar`:

```properties
appStore=http://127.0.0.1:5984/skylight-apps/
```

Use the CouchDB server's address if it's on another computer. No password is needed: anyone may read the store. To try another store without editing the file, start Java with `-Dskylight.appStore=http://...`.

## Run

```
run.bat            (Windows: starts with javaw, no console window)
./run.sh           (Linux, macOS)
```

or `java -jar skylight.jar` from the `site12fxmlcouch` folder. Skylight looks for `skylight.properties`, `images/`, `ui/` and `data/` in the current folder; to start it from elsewhere, add `-Dskylight.home=C:\path\to\site12fxmlcouch`. The `apps/` folder isn't needed on computers that use a store.

## Log in

![The login window](images/login.png)

Enter your username and password and press Enter. **Quit** (or Escape) closes Skylight OS.

If the home menu is empty, Skylight couldn't read the store: check the address in `skylight.properties` by opening `<address>_design/store/apps.txt` in a web browser.

## Next steps

* Take the [user guide](user-guide.md) tour.
* Open the **Terminal** and run `app.postNotification('Hello', 'It works');`.
* [Build your first app](building-apps.md) and publish it to the store.
