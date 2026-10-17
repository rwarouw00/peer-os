## appData.json

appData.json is where the attributes of an app's window is set.  it is a <abbr title="JavaScript Object Notation">JSON</abbr> file.  Here is the Terminal's appData.json file:

```
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
	},
	"UI": {
		"opacity": "0.9"
	},
	"permissions": {
		"FILESYSTEM_ACCESS_FULL": "true",
		"CHANGE_WALLPAPER": "true",
		"ALLOW_NOTIFICATIONS": "true",
		"OPEN_WINDOWS": "true",
		"ALLOW_TTS": "true",
		"ALLOW_OS_UPDATE": "true",
		"LOAD_THEME": "true",
		"ACCESS_USERNAME": "true"
	}
}
```

> **In `siteroot`:** the Terminal's file has no `UI` or `permissions` sections, and the desktop ignores them if present. These are the fields that are used:
>
> | Field | Required | Used for |
> | --- | --- | --- |
> | `title` | yes | Window title bar, tooltip on the taskbar and home-menu icons, label under a desktop shortcut. Also used to build element ids, so keep it unique. |
> | `position` | no | `"centered"` centers the window on screen. Any other value (or none) puts it 100px from the top and left. |
> | `width` | yes | Window width in pixels, written as a string or number without `px`. |
> | `height` | yes | Window height in pixels, including the 30px title bar. |
> | `icons.small` | yes | 16px icon in the window's title bar. |
> | `icons.medium` | yes | 32px icon on the taskbar while the window is open. |
> | `icons.large` | yes | 48px icon in the home menu. |
> | `icons.Xlarge` | for shortcuts | 64px icon for a [desktop shortcut](App.addShortcut.md). |
>
> Icon paths are **relative to the site root**, not to the app's folder, because the desktop page loads them. Note the capital `X` in `Xlarge`.
>
> The file must be valid JSON (double quotes, no trailing commas). If it isn't, the window never appears, a JavaScript error is thrown, and an empty slot is left on the taskbar.

---

_Source: [appData.json](https://sourceforge.net/p/skylightos/wiki/appData.json/) on the SourceForge wiki · Last modified 2014-03-14_

_Updated after reviewing the `siteroot` code._
