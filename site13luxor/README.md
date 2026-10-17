# Skylight OS with Luxor XUL

Skylight OS built with [Luxor XUL](https://luxor-xul.sourceforge.net) 1.0 beta 8, a Java library (2001 to 2005) that builds Swing windows from XUL, Mozilla's XML user-interface language, with Python scripting through Jython 2.1; and [Luxilla](https://luxilla.sourceforge.net), its XUL runner, for previewing layouts. An app is an `app.xul` for the window and an `app.py` for what it does, with nothing to compile. Documentation: [`../wiki13luxor`](../wiki13luxor/README.md).

## Quick start

You need a Java 8 JDK (any; JavaFX isn't needed) and, once, an internet connection to download Luxor from SourceForge.

```
tools/get-libs.sh                                      (Windows: powershell -ExecutionPolicy Bypass -File tools\get-libs.ps1)
./build.sh                                             (or build.bat)
java -cp skylight.jar skylight.AddUser admin choose-a-password
./run.sh                                               (or run.bat)
```

`tools/preview.sh` (or `tools\preview.bat`) shows every window's XUL layout in Luxilla.

## Layout

```
site13luxor/
├── src/skylight/          Main, Desktop, Xul, Script, App, Settings, Storage, AddUser
├── ui/                    the desktop's windows: login, panel, menu, notification (.xul)
├── apps/                  apps.txt, and one folder per app: app.properties, app.xul, app.py, icons/
├── images/                backgrounds (and their thumbnails), home and power icons
├── lib/                   Luxor, Jython and the other libraries (downloaded by tools/get-libs, not in git)
├── data/                  users, settings and user files (created as needed)
├── tools/                 get-libs (.sh, .ps1), preview (.sh, .bat)
├── build.sh / build.bat   compile src/ into skylight.jar
└── run.sh / run.bat       start Skylight OS
```

Luxor and Luxilla are GPL v2 software by Gerald Bauer; they're downloaded, not included.
