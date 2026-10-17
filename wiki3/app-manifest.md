# App manifest (`appData.json`)

Every app folder must contain an `appData.json` file. The desktop reads it each time the app's window opens, and at login to build the home menu.

## Example

```json
{
	"title": "Terminal",
	"position": "centered",
	"height": "300",
	"width": "500",
	"icons": {
		"small": "apps/default/terminal/icons/icon@16.png",
		"medium": "apps/default/terminal/icons/icon@32.png",
		"large": "apps/default/terminal/icons/icon@48.png",
		"Xlarge": "apps/default/terminal/icons/icon@64.png"
	}
}
```

## Fields

| Field | Required | Type | Description |
| --- | --- | --- | --- |
| `title` | yes | string | Shown in the title bar, as the tooltip on the taskbar and home-menu icons, and under a desktop shortcut. Also used in HTML element ids, so it should be unique and is best kept to letters, digits and spaces. |
| `width` | yes | number or numeric string | Window width in pixels, without `px`. |
| `height` | yes | number or numeric string | Window height in pixels, **including** the 30px title bar. The app's page gets `height - 30`. |
| `position` | no | string | `"centered"` centers the window on the screen. Anything else, or leaving it out, opens it 100px from the top and left. |
| `icons.small` | yes | path | 16×16 icon in the window's title bar. |
| `icons.medium` | yes | path | 32×32 icon on the taskbar. |
| `icons.large` | yes | path | 48×48 icon in the home menu. |
| `icons.Xlarge` | if you use shortcuts | path | 64×64 icon for a desktop shortcut. Note the capital `X`. |

Icon paths are relative to the **site root** (for example `apps/default/myapp/icons/icon@16.png`), because the desktop page loads them, not your app.

Other fields are ignored. Manifests written for later Skylight OS releases may contain `UI` and `permissions` sections; this release doesn't read them (see [Version notes](version-notes.md)).

## Rules

* The file must be strict JSON: double quotes, no comments, no trailing commas. If it can't be parsed, the window doesn't appear and an empty slot is left on the taskbar.
* The user can't resize windows, so choose a size that fits your content.
