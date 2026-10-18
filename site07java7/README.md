# Skylight OS for Java 7 and JavaFX 2.2

Skylight OS rebuilt as a Java 7 desktop program with JavaFX 2.2, the version of JavaFX that comes inside Oracle's JDK 7. It's the [Java 8 version](../site05jfx) rewritten without lambdas and without anything JavaFX 2.2 lacks: one Java process draws the desktop, every app is a Java class with its own window, and the Web Browser shows real web pages with JavaFX's WebView. Documentation: [`../wiki07java7`](../wiki07java7/README.md).

## Quick start

You need **Oracle JDK 7 update 6 or later** (it includes JavaFX 2.2), with `JAVA_HOME` pointing at it:

```
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_80       (Linux: export JAVA_HOME=/usr/lib/jvm/jdk1.7.0_80)
build.bat                                              (or ./build.sh)
"%JAVA_HOME%\bin\java" -cp skylight.jar skylight.AddUser admin choose-a-password
run.bat                                                (or ./run.sh)
```

A Java 8 JDK that includes JavaFX also builds and runs it.

## Layout

```
site07java7/
├── src/skylight/          Main, Desktop, App, Settings, Storage, AddUser, skylight.css
│   └── apps/              TerminalApp, SettingsApp, WebBrowserApp
├── apps/                  apps.txt, and one folder per app (app.properties, icons/)
├── images/                backgrounds, home and power icons
├── data/                  users, settings and user files (created as needed)
├── build.sh / build.bat   compile src/ into skylight.jar
├── run.sh / run.bat       start Skylight OS
└── jfxrt.sh / jfxrt.bat   find JavaFX's jfxrt.jar for the two scripts above
```
