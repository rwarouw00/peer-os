# Skylight OS with JavaFX FXML

The [Java 8 and JavaFX 8 version](../site05jfx) rebuilt around FXML, JavaFX's XML format for user interfaces. Apps are files: an `app.fxml` for the window and JavaScript for what it does, with nothing to compile. The desktop's own windows are FXML files in `ui/` that can be changed without rebuilding. Documentation: [`../wiki11fxml`](../wiki11fxml/README.md).

## Quick start

You need a Java 8 JDK that includes JavaFX (Oracle JDK 8, or an OpenJDK 8 build with JavaFX such as Zulu "FX" or Liberica "Full"). Nashorn, the JavaScript engine the apps use, is part of Java 8.

```
build.bat                                              (or ./build.sh)
java -cp skylight.jar skylight.AddUser admin choose-a-password
run.bat                                                (or ./run.sh)
```

## Layout

```
site11fxml/
├── src/skylight/          Main, Desktop, App, FxmlApp, Settings, Storage, AddUser
├── ui/                    the desktop's windows: login, desktop, menu, notification (.fxml), skylight.css
├── apps/                  apps.txt, and one folder per app: app.properties, app.fxml (+ .js), icons/
├── images/                backgrounds, home and power icons
├── data/                  users, settings and user files (created as needed)
├── build.sh / build.bat   compile src/ into skylight.jar
└── run.sh / run.bat       start Skylight OS
```
