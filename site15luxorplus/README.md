# Skylight OS with Luxor XUL, improved, and Tcl

[site13luxor](../site13luxor) taken further, staying on Java 8:

* **A better Luxor.** Luxor XUL 1.0 beta 8 with Skylight's changes, built from Luxor's source: hex colours, padding, margin, spacing, Java font names, tooltips, labels that grow with their text, `max-width` that works, and every control findable by its `id`. See [`luxor/`](luxor/README.md).
* **Tcl instead of Python.** Apps are an `app.xul` and an `app.tcl`, run by [Jacl](https://sourceforge.net/projects/tcljava/) 1.3.3, a Tcl interpreter written in Java. Jacl needs five small classes that Java 8 removed, rebuilt in [`compat/`](compat/README.md).

Documentation: [`../wiki15luxorplus`](../wiki15luxorplus/README.md).

## Quick start

You need a Java 8 JDK (any; JavaFX isn't needed) and, once, an internet connection to download Luxor and Jacl from SourceForge.

```
tools/get-libs.sh                                      (Windows: powershell -ExecutionPolicy Bypass -File tools\get-libs.ps1)
./build.sh                                             (or build.bat)
java -cp skylight.jar skylight.AddUser admin choose-a-password
./run.sh                                               (or run.bat)
```

`tools/get-libs` also compiles Luxor and the Jacl compatibility classes, so it needs `javac` too. `tools/preview.sh` (or `tools\preview.bat`) shows every window's XUL layout in Luxilla.

## Layout

```
site15luxorplus/
├── src/skylight/          Main, Desktop, Xul, Script (Tcl), App, Settings, Storage, AddUser
├── ui/                    the desktop's windows: login, panel, menu, notification (.xul)
├── apps/                  apps.txt, and one folder per app: app.properties, app.xul, app.tcl, icons/
├── luxor/                 Skylight's changed Luxor source files, and the same changes as a patch
├── compat/                sun.io stand-ins so Jacl 1.3.3 runs on Java 8
├── images/                backgrounds (and their thumbnails), home and power icons
├── lib/                   Luxor, Jacl and the other libraries (made by tools/get-libs, not in git)
├── data/                  users, settings and user files (created as needed)
├── tools/                 get-libs (.sh, .ps1), preview (.sh, .bat)
├── build.sh / build.bat   compile src/ into skylight.jar
└── run.sh / run.bat       start Skylight OS
```

Luxor is Gerald Bauer's, under the GNU GPL v2, and so are the changed Luxor files in `luxor/`. Jacl is under Sun Microsystems' and the University of California's licence terms, which allow use, changes and redistribution. Both are downloaded, not included.
