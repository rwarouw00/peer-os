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

---

_Source: [appData.json](https://sourceforge.net/p/skylightos/wiki/appData.json/) on the SourceForge wiki · Last modified 2014-03-14_
