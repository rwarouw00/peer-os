# Building your first app

This tutorial builds **Notes**, a small app that keeps one note per user. It shows the main parts of the [App API](app-api.md): reading and saving the user's files, notifications, and asking before doing something with a dialog. Every file below was tested in `site03`.

![The finished Notes app](images/notes.png)

You need Skylight OS running and a user to log in with; see [Getting started](getting-started.md).

## 1. Create the folder

An app is a folder of web files under `public/apps/`:

```
site03/public/apps/notes/
├── app.json
├── index.html
├── style.css
├── notes.js
└── icons/16.png, 32.png, 48.png, 64.png
```

For the icons, copy the four PNGs from `apps/settings/icons/` for now.

## 2. Describe the window

`app.json` is the [app manifest](app-manifest.md): title, size and icons. Icon paths are relative to the app's folder.

```json
{
	"title": "Notes",
	"position": "centered",
	"width": 420,
	"height": 320,
	"icons": { "16": "icons/16.png", "32": "icons/32.png", "48": "icons/48.png", "64": "icons/64.png" }
}
```

## 3. Write the page

`index.html` loads the shared styles, `core.js` and `app.js` (in that order), then your own files:

```html
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"/>
	<title>Notes</title>
	<link href="../../system/css/ui.css" rel="stylesheet"/>
	<link href="style.css" rel="stylesheet"/>
	<script src="../../system/js/core.js"></script>
	<script src="../../system/js/app.js"></script>
</head>
<body>
	<div class="toolbar">
		<button id="save" class="default">Save</button>
		<button id="clear">Clear</button>
		<span id="status"></span>
	</div>
	<textarea id="note"></textarea>
	<script src="notes.js"></script>
</body>
</html>
```

`style.css` makes the text box fill the window, whatever its size:

```css
body {
	margin: 0;
}

.toolbar {
	padding: 5px;
}

#status {
	color: #666;
	font-size: 9pt;
}

#note {
	position: absolute;
	top: 40px;
	left: 6px;
	width: calc(100% - 12px);
	height: calc(100% - 46px);
	box-sizing: border-box;
	font: 11pt Arial, sans-serif;
}
```

## 4. Add the code

`notes.js`:

```javascript
var note = document.getElementById('note');
var statusLabel = document.getElementById('status');
var FILE = 'notes.txt';

// 1. Load the user's saved note. readFile returns null if there isn't one yet.
note.value = App.readFile(FILE) || '';
statusLabel.textContent = 'Hello, ' + App.getUsername();

// 2. Save it. writeFile returns { ok: true } or { ok: false, error: '...' }.
document.getElementById('save').onclick = function () {
	var reply = App.writeFile(FILE, note.value);
	if (reply.ok) {
		statusLabel.textContent = 'Saved';
		App.postNotification('Notes', 'Your note was saved.');
	} else {
		App.openDialog('Could not save: ' + reply.error);
	}
};

// 3. Ask before throwing work away. The function runs only if the user clicks OK.
document.getElementById('clear').onclick = function () {
	App.openDialog('Clear the note? This can\'t be undone.', function () {
		note.value = '';
		statusLabel.textContent = 'Cleared (not saved yet)';
	});
};
```

1. [`App.readFile`](app-api.md#appreadfile) reads a file from the user's private storage on the server, returning `null` if it doesn't exist yet. Each user has their own `notes.txt`.
2. [`App.writeFile`](app-api.md#appwritefile) saves it and returns `{ ok: true }` or an error you can show. Any text can be saved as it is: no encoding needed.
3. [`App.openDialog`](app-api.md#appopendialog) asks first. The function runs in your app, only if the user clicks OK.

> **Watch out for browser globals.** Top-level `var`s in ES5 become properties of `window`, and some names are already taken. A variable called `status` (or `name`) silently turns whatever you assign into a string, which is why this app uses `statusLabel`. If a variable behaves strangely, rename it or wrap your code in a function.

## 5. Run it

Log in, open the **Terminal** from the home menu, and run:

```
App.openWindow('apps/notes/');
```

Type something, click **Save**, close the window and open it again: the note is still there.

## 6. Add it to the home menu

Add the folder to [`public/system/apps.json`](configuration.md#appsjson):

```json
[
	"apps/terminal/",
	"apps/settings/",
	"apps/web-browser/",
	"apps/notes/"
]
```

Reload the page. Notes now has an icon in the home menu, and you can put it on the desktop with `App.addShortcut('apps/notes/')`.

## Going further

* **Several notes:** [`App.listFiles`](app-api.md#applistfiles) returns the names of the user's files.
* **More than one page:** move between your pages with [`App.gotoLocation`](app-api.md#appgotolocation) so the window keeps its AppID and username.
* **Settings:** [`App.getSettings`](app-api.md#appgetsettings) and [`App.saveSettings`](app-api.md#appsavesettings), as in the Settings app (`apps/settings/index.html`).
* **Looks:** see [UI components](ui-components.md).

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| "Cannot open app" notification | `app.json` is missing or isn't valid JSON, or the path doesn't end in `/` |
| `App is not defined` or `Skylight is not defined` | Script paths in `index.html` are wrong, or `app.js` is loaded before `core.js` |
| `readFile` returns `null` and `writeFile` says you aren't logged in | The session ended; log in again |
| Missing icons | Icon paths in `app.json` must be relative to the app's folder |
