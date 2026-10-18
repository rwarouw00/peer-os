# myxul: XUL applications on Java 8, JavaFX and Nashorn

myxul is a XUL engine written from scratch for Java 8, following the design spec in [`../myxul/designspec.txt`](../myxul/designspec.txt). An application's windows are Mozilla-style XUL files, JavaFX draws them, and the JavaScript in them runs in Nashorn, Java 8's own JavaScript engine. It needs nothing but a Java 8 JDK that includes JavaFX: no libraries to download, no other runtime.

```xml
<window title="Hello" xmlns="http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul">
    <script>
        function greet() {
            document.getElementById("greeting").value = "Hello, " + document.getElementById("name").value + "!";
        }
    </script>
    <hbox align="center" spacing="6" style="padding: 10px;">
        <textbox id="name" flex="1" oncommand="greet()"/>
        <button label="Greet" accesskey="G" oncommand="greet()"/>
    </hbox>
    <label id="greeting" value="" style="padding: 0 10px; font-weight: bold;"/>
</window>
```

It comes with:

* **Leafpad XUL** (`apps/leafpad/`), the design spec's first target: a plain-text editor in the spirit of Lubuntu's Leafpad, with menus, keyboard shortcuts, find, jump to line, word wrap and the system's own file dialogs;
* **AbiWord XUL** (`apps/abiword/`), the spec's larger target: a word processor's shell with toolbars, a page view painted on a `<canvas>`, a sidebar with tabs, a tree and a list, a splitter, and a status bar;
* **Terminal**, **Settings** and **Web Browser** (`apps/terminal/`, `apps/settings/`, `apps/web-browser/`), ported from Skylight OS: JavaScript run in the window's own engine; the background and how long notifications stay; and web pages in JavaFX's WebKit, through XUL's `<browser>` element;
* **Gallery** (`apps/gallery/`): every element myxul knows, each doing something;
* **the launcher** (`ui/launcher.xul`), itself a XUL file, in front of the background chosen in Settings;
* **the desktop** (`ui/desktop.xul`), Skylight OS's desktop written in XUL: a home menu of the apps, a taskbar, shortcuts, a notification tray and a clock, with every app's window framed on it. It has no login: it runs as the user who starts it.

Documentation: [`../wiki17myxul`](../wiki17myxul/README.md).

## Quick start

You need a Java 8 JDK with JavaFX: Oracle JDK 8, or an OpenJDK 8 that bundles OpenJFX 8 (such as Azul Zulu FX 8 or BellSoft Liberica Full JDK 8; on Ubuntu, `openjdk-8-jdk` with `openjfx` 8).

```
./build.sh                                   (or build.bat)
./run.sh                                     the launcher (or run.bat)
./run.sh ui/desktop.xul                      the desktop
./run.sh apps/leafpad/leafpad.xul notes.txt  Leafpad XUL, opening notes.txt
```

## Layout

```
site17myxul/
├── src/myxul/            the engine: parser, tag registry, XulBox layout, styles, scripts, events, windows
│   ├── bootstrap.js      document, window, alert ... and the element wrappers, for every window's scripts
│   ├── myxul.css         default look for elements JavaFX has no control for (status bars, panels), and the desktop's
│   └── service/          Java services XUL files can ask for: FileService, PageView, SettingsService, NotificationService,
│                         DesktopService
├── ui/launcher.xul       the launcher
├── ui/desktop.xul, .js   the desktop
├── apps/                 leafpad/, abiword/, terminal/, settings/, web-browser/, gallery/: each a .xul file and its .js,
│                         an app.properties and icons/ for the desktop; apps.txt lists them for the home menu
├── images/backgrounds/   the six pictures Settings offers (Skylight OS's), and their thumbnails
├── images/desktop/       the desktop's home, power and window buttons (Skylight OS's), and a plain window icon
├── tests/xul/            test XUL in the style of real applications, and files with mistakes in them
├── build.sh / build.bat  compile src/ into myxul.jar
└── run.sh / run.bat      start myxul
```

myxul is new code written for this repository; it uses only Java 8 and the JavaFX and Nashorn that come with it.
