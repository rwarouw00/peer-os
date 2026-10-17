# App manifest (`app.json`)

Every app folder holds an `app.json` file and an `app.py` file with the app's class:

```json
{
	"title": "Terminal",
	"class": "TerminalApp",
	"width": 600,
	"height": 400,
	"icons": { "16": "icons/16.gif", "32": "icons/32.gif", "48": "icons/48.gif", "64": "icons/64.gif" }
}
```

| Key | Required | Description |
| --- | --- | --- |
| `title` | yes | Window title, window-list label, home-menu label, shortcut label |
| `class` | yes | The name of the class in the folder's `app.py` that extends `App` |
| `width`, `height` | no | Starting size of the window in pixels (default 500 × 350), not counting the title bar. Users can resize. |
| `icons` | yes | **GIF** files relative to the app's folder, all four sizes: 16 (window list), 16 to 64 (window icons), 48 (home menu and desktop shortcut) |

Icons must be GIFs (or PPM files): the Tk 8.5 that comes with Python 2.7 on Windows can't read PNG. To convert a PNG, open it in an image editor and save as GIF, or use ImageMagick: `convert 48.png 48.gif`.

App windows open centred over the desktop. The folder name may use only lowercase letters, digits and `-` (`apps/my-app/`). If the manifest is missing or incomplete, or `app.py` can't be loaded, the user gets a "Cannot open app" notification explaining why.

Save `app.json` as UTF-8. Manifests are read once per login, and `app.py` once per run of Skylight.
