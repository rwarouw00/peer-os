# Getting started

## Requirements

* **A Java 8 JDK** (`java`, `javac` and `jar` on your `PATH`). Any Java 8 works: this version needs only Swing, which every Java has. Check with `java -version` (it should say 1.8).
* **An internet connection once**, to download Luxor from SourceForge. It isn't in the repository: it's other people's software, and it's built from its source with Skylight's changes. Hecl, the scripting language, *is* in the repository, as source, so there's nothing to download for it.

## 1. Download and build Luxor

From the `site16luxorhecl` folder:

```
tools/get-libs.sh                                               (Linux, macOS: needs curl and unzip)
powershell -ExecutionPolicy Bypass -File tools\get-libs.ps1     (Windows)
```

This takes a few seconds, and:

1. downloads `luxor-libs-1.0-b8.zip` (Luxor's libraries) and `luxor-1.0-b8.zip` (Luxor's source);
2. copies Skylight's changed Luxor files from [`luxor/`](../site16luxorhecl/luxor/README.md) over Luxor's source and compiles it into `lib/luxor-1.0-b8-skylight.jar`.

## 2. Build

```
build.bat          (Windows)
./build.sh         (Linux, macOS)
```

This compiles Skylight (`src/`) and Hecl (`hecl/src/`) together into `skylight.jar`. Its manifest lists the jars in `lib/`, so `java -jar skylight.jar` finds them; keep `lib/` next to it.

## 3. Add a user

```
java -cp skylight.jar skylight.AddUser admin choose-a-password
```

Run it again to change a password. Usernames may use letters, digits, `-` and `_`.

## 4. Run

```
run.bat            (Windows: starts with javaw, no console window)
./run.sh           (Linux, macOS)
```

or `java -jar skylight.jar` from the `site16luxorhecl` folder. Skylight looks for `apps/`, `images/`, `ui/` and `data/` in the current folder; to start it from elsewhere, add `-Dskylight.home=C:\path\to\site16luxorhecl`.

## Log in

![The login window](images/login.png)

Type your username, press Enter, type your password and press Enter. **Quit** (or Escape) closes Skylight OS.

## Next steps

* Take the [user guide](user-guide.md) tour.
* Open the **Terminal** and run `$app postNotification Hello {It works}`.
* [Build your first app](building-apps.md).
* Run `tools/preview.sh` (or `tools\preview.bat`) to see every window's XUL layout in Luxilla.
