# App manifest (`app.json`)

Every app folder needs an `app.json`. When the app is pushed, it becomes the `manifest` field of the [app's document](app-store.md), where the home menu's view and the desktop read it.

```json
{
	"title": "Terminal",
	"menu": 1,
	"position": "centered",
	"width": 500,
	"height": 300,
	"icons": { "16": "icons/16.png", "32": "icons/32.png", "48": "icons/48.png", "64": "icons/64.png" }
}
```

| Field | Required | Description |
| --- | --- | --- |
| `title` | yes | Shown in the title bar, as the tooltip on the taskbar and home-menu icons, and under a desktop shortcut. Shown as text, so any characters are safe. |
| `menu` | no | A number: the app's place in the home menu, lowest first. Without one, the app is installed but only opens by path (`App.openWindow`, a shortcut). |
| `width`, `height` | yes | Window size in pixels. `height` includes the 30px title bar. Numbers or numeric strings. |
| `position` | no | `"centered"` centers the window; otherwise it opens 100px from the top and left. |
| `icons` | yes | PNG paths **relative to the app's folder**, keyed by size: `16` (title bar), `32` (taskbar), `48` (home menu), `64` (desktop shortcut). |

The file must be valid JSON (`push-app` stops with CouchDB's error if it isn't), and `title` is required: the database's validation function refuses an app without one. If the app's document can't be read, the window doesn't open and the user gets a "Cannot open app" notification.

The app's folder name is its document's name and part of its path (`apps/my-app/` is the document `my-app`). It may contain only lowercase letters, digits and `-`.

Each app folder also needs an `index.html`: the desktop opens `apps/<name>/index.html` in the window, because CouchDB, unlike a web server, doesn't turn a folder address into its index page. After adding or changing an app, run `tools/push-app.sh <address> <app>` (or `tools\push-app.ps1`) to upload it.
