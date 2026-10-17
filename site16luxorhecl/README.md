# Skylight OS with Luxor XUL, improved, and Hecl

[site15luxorplus](../site15luxorplus) with a different Tcl-style language for the apps, still on Java 8:

* **Hecl instead of Jacl.** Apps are an `app.xul` and an `app.hcl`, run by [Hecl](https://github.com/davidw/hecl), a small scripting language written in Java that looks like Tcl: commands and words, `$variables`, `[commands]` and `{braces}`. Hecl's source is included in [`hecl/`](hecl/README.md), with Skylight's fixes to how it calls Java, and is compiled into `skylight.jar`, so only Luxor is downloaded. Jacl and its Java 8 stand-in classes are gone.
* **The same improved Luxor** as site15: Luxor XUL 1.0 beta 8 built from its source with Skylight's changes (hex colours, padding, margin, spacing, Java font names, tooltips, labels that grow with their text, `max-width` that works, every control findable by its `id`). See [`luxor/`](luxor/README.md).

Documentation: [`../wiki16luxorhecl`](../wiki16luxorhecl/README.md).

## Quick start

You need a Java 8 JDK (any; JavaFX isn't needed) and, once, an internet connection to download Luxor from SourceForge.

```
tools/get-libs.sh                                      (Windows: powershell -ExecutionPolicy Bypass -File tools\get-libs.ps1)
./build.sh                                             (or build.bat)
java -cp skylight.jar skylight.AddUser admin choose-a-password
./run.sh                                               (or run.bat)
```

`tools/get-libs` also compiles Luxor, so it needs `javac` too. `build.sh` compiles Skylight and Hecl together. `tools/preview.sh` (or `tools\preview.bat`) shows every window's XUL layout in Luxilla.

## Layout

```
site16luxorhecl/
├── src/skylight/          Main, Desktop, Xul, Script (Hecl), App, Settings, Storage, AddUser
├── hecl/                  Hecl's source (core and Java modules), with Skylight's fixes, as files and as a patch
├── ui/                    the desktop's windows: login, panel, menu, notification (.xul)
├── apps/                  apps.txt, and one folder per app: app.properties, app.xul, app.hcl, icons/
├── luxor/                 Skylight's changed Luxor source files, and the same changes as a patch
├── images/                backgrounds (and their thumbnails), home and power icons
├── lib/                   Luxor and its libraries (made by tools/get-libs, not in git)
├── data/                  users, settings and user files (created as needed)
├── tools/                 get-libs (.sh, .ps1), preview (.sh, .bat)
├── build.sh / build.bat   compile src/ and hecl/src/ into skylight.jar
└── run.sh / run.bat       start Skylight OS
```

Luxor is Gerald Bauer's, under the GNU GPL v2, and so are the changed Luxor files in `luxor/`; Luxor is downloaded, not included. Hecl is David N. Welton's and Wolfgang Kechel's, under the Apache License 2.0 ([`hecl/LICENSE`](hecl/LICENSE), [`hecl/NOTICE`](hecl/NOTICE)); its source is included, with Skylight's changes marked.
