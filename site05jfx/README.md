# Skylight OS for Java 8 and JavaFX 8

Skylight OS rebuilt as a Java desktop program with JavaFX's default look. One Java process draws the desktop, every app is a Java class with its own window, and the Web Browser shows real web pages with JavaFX's WebView. Documentation: [`../wiki05jfx`](../wiki05jfx/README.md).

## Quick start

You need a Java 8 JDK that includes JavaFX (Oracle JDK 8, or an OpenJDK 8 build with JavaFX such as Zulu "FX" or Liberica "Full").

```
build.bat                                              (or ./build.sh)
java -cp skylight.jar skylight.AddUser admin choose-a-password
run.bat                                                (or ./run.sh)
```

## Layout

```
site05jfx/
├── src/skylight/          Main, Desktop, App, Settings, Storage, AddUser, skylight.css
│   └── apps/              TerminalApp, SettingsApp, WebBrowserApp
├── apps/                  apps.txt, and one folder per app (app.properties, icons/)
├── images/                backgrounds, home and power icons
├── data/                  users, settings and user files (created as needed)
├── build.sh / build.bat   compile src/ into skylight.jar
└── run.sh / run.bat       start Skylight OS
```
