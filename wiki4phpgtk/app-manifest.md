# App manifest (`app.json`)

Every app folder needs an `app.json`:

```json
{
	"title": "Terminal",
	"class": "TerminalApp",
	"position": "centered",
	"width": 560,
	"height": 360,
	"icons": { "16": "icons/16.png", "32": "icons/32.png", "48": "icons/48.png", "64": "icons/64.png" }
}
```

| Field | Required | Description |
| --- | --- | --- |
| `title` | yes | Window title, window-list label, home-menu label, shortcut label |
| `class` | yes | The PHP class in the folder's `app.php` that extends `App`. Class names must be unique across all apps, since they share one PHP process. |
| `width`, `height` | yes | The window's starting size in pixels, **not** counting the title bar, which belongs to the operating system. Users can resize it. |
| `position` | no | `"centered"` opens the window over the middle of the desktop; otherwise the window manager chooses. |
| `icons` | yes | PNGs relative to the app's folder: `16` (window list), `32` (window icon), `48` (home menu), `64` (desktop shortcut) |

The folder must contain `app.json`, `app.php` and the icons, and its name may use only lowercase letters, digits and `-` (`apps/my-app/`). If `app.json` is missing or invalid, the user gets a "Cannot open app" notification.
