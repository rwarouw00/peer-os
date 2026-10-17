# App manifest (`app.json`)

Every app folder needs an `app.json` file:

```json
{
	"title": "Terminal",
	"class": "Skylight.Apps.TerminalApp",
	"width": 640,
	"height": 420,
	"icons": { "16": "icons/16.png", "32": "icons/32.png", "48": "icons/48.png", "64": "icons/64.png" }
}
```

| Key | Required | Description |
| --- | --- | --- |
| `title` | yes | Window title, window-list label, home-menu label, shortcut label |
| `class` | yes | The full name (namespace and class) of the class that extends `Skylight.App` |
| `assembly` | no | A DLL in the app's folder holding the class, such as `"app.dll"`. Leave it out for the built-in apps, which are in `Skylight.exe`. |
| `width`, `height` | no | Starting size of the window's contents in pixels (default 500 × 350), not counting the title bar. Users can resize. |
| `icons` | yes | PNGs relative to the app's folder, all four sizes: 16 (window list), 32 (window icon), 48 (home menu), 64 (desktop shortcut) |

App windows open centred over the desktop. The folder name may use only lowercase letters, digits and `-` (`apps/my-app/`); paths in Skylight always use `/`, even on Windows. If the manifest is missing or incomplete, or the class can't be loaded, the user gets a "Cannot open app" notification explaining why.

Save `app.json` as UTF-8. Manifests are read once per login; log out and in again after changing one.
