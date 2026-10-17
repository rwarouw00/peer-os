## How to Build a Skylight OS App

> **In `siteroot`:** this tutorial has been corrected to match the Arctic Eagle code. The original told you to load `../../../js/app.js` and `../../../css/ui.css`, which don't exist in this layout, and to use `apps/installed/` while the icon paths pointed at `apps/default/`. It also didn't mention `system/apps.xml`, which is what puts an app in the home menu.

To start out, we need to explain a bit about how Skylight OS works.  Skylight OS uses a window manager, so there are windows.  These windows have two parts, the title bar and the content frame.  Everything about the window (height width, icon, title, etc...) is set in [appData.json](appData.json.md).  The content frame is just an iframe, and it holds your app.  (For the full picture, see [Architecture](Architecture.md).)

Your app is a folder of ordinary web files. Put it three folders below the site root, next to the built-in apps, for example `apps/default/myapp/`. (The depth matters: [App.saveFile](App.saveFile.md) finds the server by going up exactly three folders.) Inside that folder, create the folders `css` and `icons`, and these files: `css/custom.css`, `appData.json`, and `index.html`.

```
apps/default/myapp/
├── appData.json
├── index.html
├── css/custom.css
└── icons/icon@16.png, icon@32.png, icon@48.png, icon@64.png
```

#### Creating Your appData.json File

Now lets add some code to your appData.json file.  appData.json sets up everything about the window.  Here is the default template:

```
{
	"title": "My App",
	"position": "centered",
	"height": "300",
	"width": "500",
	"icons": {
		"small": "apps/default/myapp/icons/icon@16.png",
		"medium": "apps/default/myapp/icons/icon@32.png",
		"large": "apps/default/myapp/icons/icon@48.png",
		"Xlarge": "apps/default/myapp/icons/icon@64.png"
	}
}
```

Copy and paste this into your appData.json file, replacing myapp with whatever you named your app's folder. The icon paths are relative to the site root, not to your app. Icons are PNGs at 16, 32, 48 and 64 pixels square; until you draw your own, you can copy the Terminal's from `apps/default/terminal/icons/`.

#### Creating Your index.html File

This is the most important file in your app.  This is where you set up your app.  It's a standard HTML5 document, so it should be pretty familiar.  Still, there are some dependencies we must import: the shared styles (`ui.css`) and the [App object](App-Object.md) (`app.js`).  Here is the default index.html file.  Just copy and paste this into your index.html file:

```
<!DOCTYPE html>
<html>
<head>
<link href="../../../system/css/ui.css" rel="stylesheet"/>
<link href="css/custom.css" rel="stylesheet"/>
<script src="../../../system/js/app.js"></script>
</head>
<body>
</body>
</html>
```

#### Adding Content

Now, lets add some content.  Add this code inside the &lt;body&gt; tag:

```
<button id="mybutton">Click Me!</button>

<script>
document.getElementById('mybutton').onclick = function () {
	App.postNotification('My App', 'Hello, ' + App.getUsername() + '!');
};
</script>
```

#### Running Your App

Now save it, and you're ready to go!  Open the Terminal from the home menu, type this and click **Run**:

```
App.openWindow('apps/default/myapp/');
```

That should open your window. Click the button and a notification appears in the top-right corner. (The path ends in `/` and is relative to the site root.)

#### Adding Your App to the Home Menu

To give your app an icon in the home menu, add a line for it to `system/apps.xml` (see [Configuration Files](Configuration-Files.md)) and log in again:

```
<apps>
	<app path="apps/default/terminal/"/>
	...
	<app path="apps/default/myapp/"/>
</apps>
```

#### Things to Know

* If your app has more than one page, switch between them with [App.gotoLocation](App.gotoLocation.md), not links or `location.assign`, or the app loses its [AppID](AppID.md) and username.
* Files are read with [App.openFile](App.openFile.md), using paths relative to your page; saving uses [App.saveFile](App.saveFile.md), which needs the server scripts (see [Server Endpoints](Server-Endpoints.md)).
* The window can't be resized by the user, so design for the size in `appData.json`. If you want your content to fill the window, size it from `window.innerWidth` and `window.innerHeight` like the Terminal does.

That's about it!  Check out our [JavaScript API's](App-Object.md), the [built-in apps](Built-in-Apps.md) for working examples, and our [other tutorials](Tutorials.md) to learn how to create more advanced apps.  Have fun coding!

---

_Source: [How to build a Skylight OS app](https://sourceforge.net/p/skylightos/wiki/How%20to%20build%20a%20Skylight%20OS%20app/) on the SourceForge wiki · Labels: building apps · Last modified 2014-03-13_

_Updated after reviewing the `siteroot` code._
