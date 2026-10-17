# App manifest (`app.properties`)

Every app folder needs an `app.properties` file (Java's standard key=value format):

```properties
title=Terminal
xul=app.xul
script=app.hcl
width=600
height=400
icon.16=icons/16.png
icon.32=icons/32.png
icon.48=icons/48.png
icon.64=icons/64.png
```

| Key | Required | Description |
| --- | --- | --- |
| `title` | yes | Window title, window-list label, home-menu label, shortcut label, and the title of the app's dialogs |
| `xul` | no (default `app.xul`) | The XUL file in the app's folder that describes the window. Its first `<vbox>` or `<hbox>` is the window's contents. |
| `script` | no (default `app.hcl`) | The Hecl file that runs when the window opens, in a new interpreter for the window (see the [Hecl primer](hecl-primer.md)). Optional: an app with nothing to do needs no script. |
| `width`, `height` | no | Starting size of the window's contents in pixels (default 500 × 350), not counting the title bar. Users can resize. |
| `icon.16` … `icon.64` | yes | PNGs relative to the app's folder: 16 (window list), 16 to 64 (window icons; the system picks the best size), 48 (home menu), 64 (desktop shortcut, shown at 48) |

App windows open centred over the desktop. The folder name may use only lowercase letters, digits and `-` (`apps/my-app/`). If the manifest is missing or incomplete, or the XUL or script can't be loaded, the user gets a "Cannot open app" notification explaining why, with the file and line where there is one.

Manifests are read once per login. The XUL and the script are read each time the app opens, so changes to them show up when you next open the app.

Non-ASCII characters in `.properties` files must be written as `\u` escapes (for example `title=Caf\u00e9`), because Java 8 reads these files as ISO-8859-1. XUL files are UTF-8 (or whatever their `<?xml encoding?>` says). Hecl files are read as UTF-8.
