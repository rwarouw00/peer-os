# Building your first app

This tutorial builds **Notes**, a small app that keeps one note per user. It shows the main parts of the [App API](app-api.md): reading and saving the user's files, notifications, and asking before doing something with a dialog. Every file below is the same as in the PHP version's tutorial, and was tested in CouchDB 2.3.1 (see [Limitations](limitations.md#how-it-was-tested)).

![The finished Notes app](images/notes.png)

You need Skylight OS installed in CouchDB and a user to log in with; see [Getting started](getting-started.md). Each app is its own document in the `skylight` database, so you install or update it on its own with `tools/push-app.sh` (or `tools\push-app.ps1`), without touching Skylight or the other apps.

## 1. Create the folder

An app is a folder of web files under `apps/`. When it's pushed, the folder becomes one [app document](app-store.md) named after it (`notes`), and each file except `app.json` becomes an attachment of that document:

```
site14couchsky/apps/notes/
├── app.json
├── index.html
├── style.css
├── notes.js
└── icons/16.png, 32.png, 48.png, 64.png
```

For the icons, copy the four PNGs from `apps/settings/icons/` for now.

## 2. Describe the window

`app.json` is the [app manifest](app-manifest.md): title, place in the home menu, size and icons. Icon paths are relative to the app's folder. It's stored as the document's `manifest` field.

```json
{
	"title": "Notes",
	"menu": 4,
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

1. [`App.readFile`](app-api.md#appreadfile) reads the document `file:notes.txt` from the user's own CouchDB database, returning `null` if it doesn't exist yet. Each user has their own `notes.txt`.
2. [`App.writeFile`](app-api.md#appwritefile) saves it (creating the document, or replacing it with its current revision) and returns `{ ok: true }` or an error you can show. Any text can be saved as it is: no encoding needed.
3. [`App.openDialog`](app-api.md#appopendialog) asks first. The function runs in your app, only if the user clicks OK.

> **Watch out for browser globals.** Top-level `var`s in ES5 become properties of `window`, and some names are already taken. A variable called `status` (or `name`) silently turns whatever you assign into a string, which is why this app uses `statusLabel`. If a variable behaves strangely, rename it or wrap your code in a function.

## 5. Run it

Upload the app to CouchDB, as a CouchDB administrator:

```
tools/push-app.sh http://admin:password@127.0.0.1:5984 notes
```

```
Pushed notes
```

That's the whole installation. Log in (or reload the page if you're logged in already): Notes is in the home menu, fourth, because its `app.json` says `"menu": 4`. There's no list of apps to edit; the home menu is a [view](app-store.md#the-home-menu) of every app document with a `menu` number. You can also put it on the desktop with `App.addShortcut('apps/notes/')`, or open it from the **Terminal**:

```
App.openWindow('apps/notes/');
```

Type something, click **Save**, close the window and open it again: the note is still there.

## 6. Change it

Edit any of the files and push the app again:

```
tools/push-app.sh http://admin:password@127.0.0.1:5984 notes
```

The document gets a new revision with the new files. Windows opened after that get the new version; one that's already open keeps the version it loaded until it's closed and opened again. To take an app out of the home menu but keep it installed, remove its `menu` line and push it again.

You can look at the result in Fauxton: <http://127.0.0.1:5984/_utils/#database/skylight/notes> shows the document, with its manifest and attachments.

## Going further

* **Several notes:** [`App.listFiles`](app-api.md#applistfiles) returns the names of the user's files.
* **More than one page:** move between your pages with [`App.gotoLocation`](app-api.md#appgotolocation) so the window keeps its AppID and username.
* **Settings:** [`App.getSettings`](app-api.md#appgetsettings) and [`App.saveSettings`](app-api.md#appsavesettings), as in the Settings app (`apps/settings/index.html`).
* **Looks:** see [UI components](ui-components.md).

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| "Cannot open app" notification | The app hasn't been pushed yet, or the path doesn't end in `/` |
| `push-app` says `CouchDB said: {"error":"bad_request"...}` | `app.json` isn't valid JSON |
| `push-app` says `forbidden` | The manifest has no `title`, or the folder name uses something other than lowercase letters, digits and `-` |
| Not in the home menu | `app.json` has no `"menu"` number, or you haven't reloaded since pushing |
| The window opens but shows `{"error":"not_found"...}` | The folder has no `index.html` (CouchDB has no folder index pages) |
| `App is not defined` or `Skylight is not defined` | Script paths in `index.html` are wrong, or `app.js` is loaded before `core.js` |
| `readFile` returns `null` and `writeFile` says you aren't logged in | The CouchDB session cookie expired (after 10 minutes without a request, by default); log in again |
| Missing icons | Icon paths in `app.json` must be relative to the app's folder |
