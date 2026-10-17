# Skylight OS with FXML apps from CouchDB

The [FXML version](../site11fxml) of Skylight OS, with its apps delivered from a **CouchDB app store** instead of a folder on each computer. An app is still plain files (an `app.properties`, an `app.fxml`, JavaScript and icons), now kept as one CouchDB document per app with the files attached. Every desktop that points at the store gets the same apps, and an app pushed to the store reaches them all the next time it's opened, with nothing installed or compiled. Documentation: [`../wiki12fxmlcouch`](../wiki12fxmlcouch/README.md).

## Quick start

You need a Java 8 JDK that includes JavaFX (Oracle JDK 8, or an OpenJDK 8 build with JavaFX such as Zulu "FX" or Liberica "Full"), and CouchDB 1.6 or later with an administrator account.

```
build.bat                                              (or ./build.sh)
java -cp skylight.jar skylight.AddUser admin choose-a-password
tools/push-apps.sh http://admin:password@127.0.0.1:5984/skylight-apps
                                                       (Windows: powershell -File tools\push-apps.ps1 http://...)
run.bat                                                (or ./run.sh)
```

The store's address is in `skylight.properties`. Leave `appStore=` empty to use the local `apps/` folder, as site11fxml does.

## Layout

```
site12fxmlcouch/
├── src/skylight/          Main, Desktop, App, FxmlApp, Settings, Storage, AddUser
├── ui/                    the desktop's windows: login, desktop, menu, notification (.fxml), skylight.css
├── apps/                  the apps' source: apps.txt, and one folder per app (pushed to the store)
├── store/                 validate_doc_update.js: only CouchDB administrators may change the store
├── tools/                 push-apps.sh and push-apps.ps1 (with couch.ps1): upload apps to the store
├── skylight.properties    appStore=, the store's address
├── images/                backgrounds, home and power icons
├── data/                  users, settings and user files (created as needed, stay on this computer)
├── build.sh / build.bat   compile src/ into skylight.jar
└── run.sh / run.bat       start Skylight OS
```
