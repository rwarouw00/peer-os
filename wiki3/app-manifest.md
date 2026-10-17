# App manifest (`app.json`)

Every app folder needs an `app.json`. The desktop reads it to build the home menu and each time the app's window opens.

```json
{
	"title": "Terminal",
	"position": "centered",
	"width": 500,
	"height": 300,
	"icons": { "16": "icons/16.png", "32": "icons/32.png", "48": "icons/48.png", "64": "icons/64.png" }
}
```

| Field | Required | Description |
| --- | --- | --- |
| `title` | yes | Shown in the title bar, as the tooltip on the taskbar and home-menu icons, and under a desktop shortcut. Shown as text, so any characters are safe. |
| `width`, `height` | yes | Window size in pixels. `height` includes the 30px title bar. Numbers or numeric strings. |
| `position` | no | `"centered"` centers the window; otherwise it opens 100px from the top and left. |
| `icons` | yes | PNG paths **relative to the app's folder**, keyed by size: `16` (title bar), `32` (taskbar), `48` (home menu), `64` (desktop shortcut). |

The file must be valid JSON. If it can't be read, the window doesn't open and the user gets a "Cannot open app" notification.

The app's folder name is used in its path (`apps/my-app/`), which may contain only letters, digits, `_`, `-` and `/`. Use lowercase with hyphens.
