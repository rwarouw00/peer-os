# App manifest (`app.properties`)

Every app needs an `app.properties` file (Java's standard key=value format), in its folder and so, once pushed, as an attachment of its document in the [app store](app-store.md):

```properties
title=Terminal
fxml=app.fxml
width=600
height=400
icon.16=icons/16.png
icon.32=icons/32.png
icon.48=icons/48.png
icon.64=icons/64.png
```

| Key | Required | Description |
| --- | --- | --- |
| `title` | yes | Window title, window-list label, home-menu label, shortcut label |
| `fxml` | one of `fxml` and `class` | The FXML file in the app's folder that describes the window. Its scripts use the App API through `app`. No Java needed. |
| `class` | one of `fxml` and `class` | The fully qualified name of a Java class that extends `skylight.App`, in an `app.jar` in the app's folder or store document (or in `skylight.jar`). If both are given, `class` is used; the class can load FXML itself with `loadFxml`. |
| `width`, `height` | no | Starting size of the window's contents in pixels (default 500 × 350), not counting the title bar. Users can resize. |
| `icon.16` … `icon.64` | yes | PNGs relative to the app's folder (or document): 16 (window list), 16 to 64 (window icons; the system picks the best size), 48 (home menu), 64 (desktop shortcut) |

App windows open centred over the desktop. The folder name may use only lowercase letters, digits and `-` (`apps/my-app/`); it's also the app's document name in the store (`my-app`). If the manifest is missing or incomplete, or the FXML or class can't be loaded, the user gets a "Cannot open app" notification explaining why; for FXML, it names the file and line.

Manifests are read once per login. FXML and script files are read each time the app opens, so changes pushed to the store show up when you next open the app.

Non-ASCII characters in `.properties` files must be written as `\u` escapes (for example `title=Café`), because Java 8 reads these files as ISO-8859-1. FXML and `.js` files are UTF-8.
