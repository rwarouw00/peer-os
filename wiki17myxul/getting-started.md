# Getting started

## Requirements

**A Java 8 JDK that includes JavaFX 8.** myxul uses JavaFX for its windows and Nashorn for its scripts. Nashorn is part of every Java 8; JavaFX is part of some:

| Java 8 | JavaFX |
| --- | --- |
| Oracle JDK 8 | Included |
| Azul Zulu 8 "FX" builds, BellSoft Liberica "Full" JDK 8 | Included |
| OpenJDK 8 on Ubuntu, Debian and Lubuntu | Not included, and today's `openjfx` packages are for newer Javas. Java 8 needs OpenJFX 8: Ubuntu 18.04's `openjfx`, `libopenjfx-java` and `libopenjfx-jni` packages (version 8u161) install it into Java 8's own folders. The test machine used those, unpacked into `openjdk-8-jdk` 8u504 |
| Other OpenJDK 8 builds | Usually not included |

Check with `java -version` (it should say 1.8). Java 9 and later don't include Nashorn and JavaFX in the same way, and myxul wasn't tried on them.

If your computer has several Javas, put Java 8 first on the `PATH` when building and running, or `java` may be a newer one: then running fails with `NoClassDefFoundError: javafx/application/Application`.

## Build

From the `site17myxul` folder:

```
./build.sh         (Linux, macOS)
build.bat          (Windows)
```

This compiles `src/` into `myxul.jar`, in a second or two. There is nothing to download.

## Run

```
./run.sh                                       the launcher
./run.sh ui/desktop.xul                        the desktop, with every app on it
./run.sh apps/leafpad/leafpad.xul              Leafpad XUL
./run.sh apps/leafpad/leafpad.xul notes.txt    Leafpad XUL, opening notes.txt
./run.sh apps/abiword/abiword.xul              AbiWord XUL
./run.sh apps/gallery/gallery.xul              the Gallery
```

On Windows, `run.bat` does the same. Underneath it is `java -jar myxul.jar [file.xul [arguments...]]`: with no file, myxul opens the launcher, `ui/launcher.xul`, from the folder `myxul.jar` is in. Arguments after the file are the window's `window.arguments`. File paths are relative to the folder you're in.

myxul exits when its last window closes.

The launcher and the desktop are two ways in to the same apps. The launcher is a small window that opens each app in a window of its own; the desktop fills the screen and keeps the apps' windows on it, with a taskbar, as Skylight OS does (its **Desktop** button opens it too). There's no login for either: myxul runs as you.

![The launcher](images/launcher.png)

## Next steps

* Take the [user guide](user-guide.md) tour of the apps.
* [Build your first app](building-apps.md).
* Look up an element in the [XUL reference](xul-reference.md).
