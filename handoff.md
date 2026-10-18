# Session handoff

Written at the end of a working session on 18 October 2026, for whoever picks up this repository next.

## Where things stand

* **Branch:** `claude/skylight-docs`, pushed and in step with `origin`. The last commit is `f9136f2` ("Add Skylight OS's desktop to myxul, without a login"), on top of `a0e6ddf` (`apachejelly/designdoc.txt`, added outside this session and not yet looked at).
* **Working tree:** clean, apart from this file.
* **Tests:** all passing on the last run (details [below](#testing)).
* **No pull request** has been opened for the branch.

## What the repository is

Skylight OS, a 2013 web desktop (its original code is in `siteroot/`, its SourceForge wiki in `wiki/` and a revised copy in `wiki2/`), rebuilt many times on different platforms. Each version is a `siteNN...` folder with its documentation in the matching `wikiNN...` folder:

| Version | Platform |
| --- | --- |
| `site03` | Browser, with PHP on the server |
| `site04phpgtk` | PHP-GTK2 |
| `site05jfx` | Java 8, JavaFX 8 |
| `site06cs` | C# 5, Windows Forms |
| `site07java7` | Java 7, JavaFX 2.2 |
| `site08py27tk` | Python 2.7, Tkinter |
| `site09couchdb`, `site10couchdb16`, `site14couchsky` | CouchDB couchapps |
| `site11fxml`, `site12fxmlcouch` | JavaFX FXML with Nashorn JavaScript |
| `site13luxor`, `site15luxorplus`, `site16luxorhecl` | Luxor XUL on Swing, with Jython, Tcl (Jacl) and Hecl |
| `site17myxul` | **myxul**: a XUL engine written from scratch for Java 8, JavaFX 8 and Nashorn |

`wikicompare/` compares the same Notes tutorial across all the versions. `myxul/designspec.txt` is the design spec `site17myxul` was built from (`myxul/designbase.txt` is its original).

## What this session did

In order:

1. **Cut `myxul/designspec.txt` down** to a spec for building XUL apps from scratch on Java 8, JavaFX and Nashorn, in our own code. All references to OpenXUL, Luxor, Luxilla, the Luxor family of Java components, LibreOffice 7.6.7.2 and 24.8, and all databases (HSQLDB, Firebird) were removed.
2. **Built `site17myxul` and `wiki17myxul`** from that spec. They include Leafpad XUL, an AbiWord XUL shell, a Gallery of every element, and a launcher.
3. **Ported Skylight OS's Terminal, Settings and Web Browser** to myxul (from `site11fxml`). This added the `<browser>` element and the `SettingsService` and `NotificationService` services.
4. **Renamed** `site3`–`site9` and `wiki3`–`wiki9` to two digits (`site03`, `site04phpgtk`, ...), so folders list in order, and updated every reference. `wiki`, `wiki2` and `siteroot` kept their names.
5. **wiki2's "broken links":** there were none. The ten links to `siteroot` folders with spaces in their names were correct; the session's own link checker failed to decode `%20`. Nothing in the repository changed.
6. **Added Skylight OS's desktop to myxul** (`ui/desktop.xul`, `ui/desktop.js`, `DesktopService`), with **no login, logout or users**: it runs as whoever starts it. It has:
   * a panel with a home button, a taskbar and a clock;
   * a home menu with the apps, a notification tray and a power button;
   * shortcuts on the background;
   * Skylight-style frames on every app window.

   The XUL engine gained what the desktop needed:
   * `createElement`, `appendChild`, `insertBefore`, `removeChild`, `addEventListener` and `removeEventListener`;
   * `<stack>`, `<scrollbox>`, `<panel>`/`<popupset>` and context menus;
   * mouse down, move and up events;
   * a window host.

   It also fixed two bugs: wrapped text being cut off in some boxes, and `setAttribute` writing `100.0`.

## Rules the user set (keep to them)

* **Everything stays at Java 8.** Don't use a Java that needs the standalone Nashorn jar (Java 15 and later).
* **Don't touch `myxul/designspec.txt`** (or `myxul/designbase.txt`).
* **Leave `wiki`, `wiki2` and `siteroot` names as they are.**
* `site17myxul` and `wiki17myxul` must not mention OpenXUL, Luxor, Luxilla, LibreOffice or the databases removed from the spec.
* The desktop has **no login, logout or authentication**, by request.
* Commits end with:

  ```
  Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
  Claude-Session: https://claude.ai/code/session_01DWbvDJMJYNWctzqPaX4GmB
  ```

* Push with `git push -u origin claude/skylight-docs`. Don't open a pull request unless asked.

## The pattern each version follows

Working code, then real tests (an automated driver, real mouse and keyboard through xdotool, screenshots), then a full wiki with an honest "How it was tested" section, then link and Mermaid checks, then commit and push.

## myxul in brief

* **Build and run** from `site17myxul/` with a Java 8 JDK that includes JavaFX:

  ```
  ./build.sh
  ./run.sh                    # the launcher
  ./run.sh ui/desktop.xul     # the desktop
  ```

  `myxul.jar` and `build/` are git-ignored.
* **Engine:** `src/myxul/`. There are 12 core classes (15 with `Main`, `XulEvent` and `XulException`), plus `bootstrap.js` and `myxul.css`. The services in `src/myxul/service/` are:
  * `FileService`;
  * `PageView`;
  * `SettingsService`;
  * `NotificationService`;
  * `DesktopService`.

  Together they make about 5,200 non-blank lines.
* **Apps:** `apps/<name>/`, each with a `.xul` file, its `.js`, and an `app.properties` and `icons/` for the desktop. `apps/apps.txt` sets the home menu's order.
* **Desktop design choice:** each app window is a real `Stage`, undecorated and owned by the desktop, rather than drawn inside the desktop's scene. That keeps each app's keys, menus and dialogs separate. The cost is that the window manager still sees each window (Alt+Tab, for example), which the wiki's limitations page explains.
* **Docs:** `wiki17myxul/README.md` is the entry point. `engine-internals.md#the-desktop` explains the window host, and `limitations.md#how-it-was-tested` lists every check.

## Testing

The last full run used a fresh copy of `site17myxul`, built with OpenJDK 8u504 and OpenJFX 8u161, under Xvfb (1280x800) with metacity. It also copied in the code from the Notes tutorial and the README.

| Test | Result |
| --- | --- |
| Original automated driver | 200 checks, 0 failures |
| Desktop and new-XUL driver | 140 checks, 0 failures |
| Original xdotool mouse-and-keyboard script | All steps as expected; myxul exited on Quit |
| Desktop xdotool script | All steps as expected; myxul exited from the power button |
| Link check, every wiki | 0 problems |
| Mermaid diagrams in `wiki17myxul` | 2 rendered, 0 errors |

**Important: the test tooling isn't in the repository.** The drivers (`TestDriver.java`, `DesktopDriver.java`), the xdotool scripts (`s17mouse.sh`, `s17desk.sh`, `s17final.sh`), the tutorial copier (`tut17.py`) and the link checker (`linkcheck.py`) were kept in the session's temporary scratch folder. That folder goes when the session's container does. The Web Browser tests also need a small page served on `http://127.0.0.1:8092/`. To keep the tests for future sessions, commit them (for example into `site17myxul/tests/`) before this container is reclaimed. The user hasn't asked for that yet.

## Known limitations and loose ends

* The desktop was tested only with metacity, on one screen. Other window managers may place undecorated windows differently.
* Windows, macOS, `build.bat` and `run.bat` are untested for `site17myxul`, as are Java 8 builds other than OpenJDK 8u504 with OpenJFX 8u161.
* myxul's XUL subset is listed in `wiki17myxul/limitations.md`. Not supported: overlays, XBL, DTD entities, `<deck>` and multi-column trees. Elements made by scripts can't go in grids, tab boxes, split boxes or lists.
* The new `apachejelly/designdoc.txt` (941 lines) arrived from outside this session and hasn't been reviewed or acted on.
* `wikicompare/README.md` still covers fourteen versions and doesn't yet include `site17myxul`'s Notes.

## Suggested next steps

1. Ask the user whether to commit the test tooling into the repository so later sessions can re-run it.
2. Read `apachejelly/designdoc.txt` and ask what the user wants built from it.
3. Optionally add myxul to `wikicompare` (Notes in XUL with Nashorn), as the fifteenth version.
