# App manifest (`app.properties`)

Every app folder needs an `app.properties` file (Java's standard key=value format):

```properties
title=Terminal
class=skylight.apps.TerminalApp
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
| `class` | yes | The fully qualified name of the class that extends `skylight.App`. Built-in apps are in `skylight.jar`; any other app puts its classes in an `app.jar` in its folder. |
| `width`, `height` | no | Starting size of the window's contents in pixels (default 500 × 350), not counting the title bar. Users can resize. |
| `icon.16` … `icon.64` | yes | PNGs relative to the app's folder: 16 (window list), 16 to 64 (window icons; the system picks the best size), 48 (home menu), 64 (desktop shortcut) |

App windows open centred over the desktop. The folder name may use only lowercase letters, digits and `-` (`apps/my-app/`). If the manifest is missing or incomplete, or the class can't be loaded, the user gets a "Cannot open app" notification explaining why.

Non-ASCII characters in `.properties` files must be written as `\u` escapes (for example `title=Café`), because Java 8 reads these files as ISO-8859-1.
