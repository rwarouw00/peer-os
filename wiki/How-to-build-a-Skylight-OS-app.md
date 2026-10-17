## How to Build a Skylight OS App

To start out, we need to explain a bit about how Skylight OS works.  Skylight OS uses a window manager, so there are windows.  These windows have two parts, the title bar and the content frame.  Everything about the window (height width, icon, title, etc...) is set in [appData.json](appData.json.md).  The content frame is just an iframe, and it holds you app.  Your app is stored in the directory apps/installed.  Then there is the name of your app.  So the complete path is apps/installed/myapp/.  That is what we need to know.  So, go to that path (replacing myapp for whatever you call your app) and create some folders.  The folders are: js, css, and icons.  You also need to create some files: css/custom.css, js/default.js, appData.json, and index.html.  Now we've set up the apps files.

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

This template will work just fine for us, so copy and paste this into you appData.json file, again replacing myapp for whatever you named your app.

#### Creating Your index.html File

This is the most important file in your app.  This is where you set up your app.  It's a standard HTML5 document, so it should be pretty familiar.  Still, there are some dependencies we must import.  Here is the default index.html file.  Just copy and paste this into your index.html file:

```
<!DOCTYPE html>
<html>
<head>
<link href="css/custom.css" rel="stylesheet"/>
<link href="../../../css/ui.css" rel="stylesheet"/>
<script src="../../../js/app.js"></script>
</head>
<body>
</body>
</html>
```

#### Adding Content

Now, lets add some content.  Add this code inside the &lt;body&gt; tag:

```
<button id="mybutton">Click Me!</button>
```

Now save it, and your ready to go!  Open the Terminal and type "App.openWindow('apps/installed/myapp/');".  That should open your window, with the same dimensions as the Terminal.

That's about it!  Check out our [JavaScript API's](App-Object.md) and our [other tutorials](Tutorials.md) to learn how to create more advanced apps.  Have fun coding!

---

_Source: [How to build a Skylight OS app](https://sourceforge.net/p/skylightos/wiki/How%20to%20build%20a%20Skylight%20OS%20app/) on the SourceForge wiki · Labels: building apps · Last modified 2014-03-13_
