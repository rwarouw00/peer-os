# Building your first app

This tutorial builds **Notes**, an app that keeps one note per user. Like the built-in apps, it's made of plain files: an FXML file for the window and a JavaScript file for what it does. There's no Java and nothing to compile. It covers the parts of the [App API](app-api.md) most apps need: reading and saving the user's files, notifications, a confirmation dialog, and stopping the window closing while there are unsaved changes. Every step was tested on Java 8 and JavaFX 8.

![The finished Notes app](images/notes.png)

You need Skylight OS built; see [Getting started](getting-started.md). You'll write the app in the `apps/` folder and try it there, then publish it to the [app store](app-store.md) for everyone.

## 1. Create the folder

```
site12fxmlcouch/apps/notes/
├── app.properties
├── app.fxml
├── notes.js
└── icons/16.png, 32.png, 48.png, 64.png
```

For the icons, copy the four PNGs from `apps/settings/icons/` for now.

## 2. Describe the app

`app.properties` (see [App manifest](app-manifest.md)). `fxml=` names the window's file, in place of the Java version's `class=`:

```properties
title=Notes
fxml=app.fxml
width=440
height=320
icon.16=icons/16.png
icon.32=icons/32.png
icon.48=icons/48.png
icon.64=icons/64.png
```

## 3. Lay out the window

`app.fxml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?language javascript?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<BorderPane xmlns:fx="http://javafx.com/fxml">
	<top>
		<ToolBar>
			<Button text="Save" onAction="save()"/>
			<Button text="Clear" onAction="askToClear()"/>
			<Region HBox.hgrow="ALWAYS"/>
			<Label fx:id="status"/>
		</ToolBar>
	</top>
	<center>
		<TextArea fx:id="note" wrapText="true"/>
	</center>
	<fx:script source="notes.js"/>
</BorderPane>
```

* `<?language javascript?>` says the scripts and event handlers in this file are JavaScript.
* Each `<?import ...?>` makes JavaFX classes available as elements, like Java's `import`.
* The elements build the window: a `BorderPane` with a `ToolBar` on top and a `TextArea` filling the rest. Attributes set properties: `text="Save"` calls `setText("Save")`.
* `fx:id="note"` gives the text area a name that scripts can use.
* `onAction="save()"` is a line of JavaScript, run when the button is clicked.
* `<fx:script source="notes.js"/>` runs the script file, after everything above it has been created.

## 4. Write the script

`notes.js`:

```javascript
// 1. Show the saved note. readFile() returns null if there isn't one yet.
var changed = false;
note.setText(app.readFile('notes.txt') || '');
status.setText('Hello, ' + app.getUsername());
note.textProperty().addListener(new javafx.beans.InvalidationListener(function () {
	changed = true;   // the text was edited
}));

// 2. Save. writeFile() reports success, or an error to show the user.
function save() {
	var result = app.writeFile('notes.txt', note.getText());
	if (result.ok) {
		changed = false;
		status.setText('Saved');
		app.postNotification('Notes', 'Your note was saved.');
	} else {
		app.openDialog('Could not save: ' + result.error);
	}
}

// 3. Ask before throwing work away. The function runs only if the user clicks OK.
function askToClear() {
	app.openDialog("Clear the note? This can't be undone.", function () {
		note.clear();
		status.setText('Cleared (not saved yet)');
	});
}

// 4. Don't lose unsaved work when the window is closed.
app.setCanClose(function () {
	if (!changed) {
		return true;
	}
	app.openDialog('Close without saving your changes?', function () {
		changed = false;
		app.closeWindow();
	});
	return false;
});
```

How it works:

1. **The script runs once**, when the window is being built. It can use every `fx:id` as a variable (`note`, `status`), and **`app`**, which is the [App API](app-api.md). It loads the note and listens for edits.
2. **Functions** defined here (`save`, `askToClear`) are what the buttons' `onAction` attributes call. Variables defined here (`changed`) keep their values between calls.
3. **Files:** `app.readFile` and `app.writeFile` use the user's private storage, so each user has their own `notes.txt`.
4. **Dialogs:** the function given to `app.openDialog` runs only if the user clicks OK.
5. **Closing:** `app.setCanClose` takes a function that Skylight asks before the window closes. Returning `false` keeps it open.

Two lines need a word of explanation; the [FXML primer](fxml-primer.md#javascript-and-java) has more:

* `new javafx.beans.InvalidationListener(function () { ... })`: `addListener` has two versions, so the script has to say which kind of listener its function is.
* `note.getText()` and `status.setText(...)` are ordinary JavaFX methods, called from JavaScript.

## 5. Run it

While you're writing an app, run Skylight from the `apps/` folder instead of the store: set `appStore=` (empty) in `skylight.properties`, or start it with

```
java -Dskylight.appStore= -jar skylight.jar
```

Open the **Terminal** and run:

```javascript
app.openWindow('apps/notes/');
```

Type something, click **Save**, close the window and open it again: the note is still there. Change it and close the window without saving: you'll be asked first.

To change the app, edit `app.fxml` or `notes.js` and open the window again; the files are read each time an app opens. (Only `app.properties` is read once per login.)

## 6. Add it to the home menu

Add the folder to `apps/apps.txt`:

```
apps/terminal/
apps/settings/
apps/web-browser/
apps/notes/
```

Log out and in again: Notes appears in the home menu. `app.addShortcut('apps/notes/')` in the Terminal puts it on the desktop.

## 7. Publish it

Push the app, and the new `apps.txt`, to the store:

```
tools/push-apps.sh http://admin:password@127.0.0.1:5984/skylight-apps notes
```

```
Uploaded notes
The app store is ready: http://127.0.0.1:5984/skylight-apps/
```

That's the whole installation. Every desktop using the store shows Notes in its home menu at its next login, and the app's files are at `http://127.0.0.1:5984/skylight-apps/notes/app.fxml` and so on. (`push-apps` always updates the home menu's list; naming `notes` only limits which apps are uploaded.)

To change the app later, edit it, try it locally, and push it again. Users get the new version the next time they open Notes.

## Writing an app in Java instead

For apps with a lot of logic, Java may be clearer. Set `class=` instead of `fxml=` in `app.properties`, and extend `skylight.App` as in the [Java 8 version's tutorial](../wiki5jfx/building-apps.md). Compile it into `app.jar` in the app's folder; `push-apps` uploads the jar (but not `src/` or `classes/`), and Skylight loads the classes from the store. The class can still use FXML for its layout: `loadFxml("app.fxml")` loads a file from the app's folder with the class as its controller, so `onAction="#save"` calls a Java method `save()` and `fx:id="note"` fills an `@FXML` field called `note` (see [App API](app-api.md#loadfxml)).

## Troubleshooting

When an FXML app can't open, the "Cannot open app" notification gives the file and, where JavaFX knows it, the line.

| Symptom | Likely cause |
| --- | --- |
| "... is not a valid type" | A class is used without an `<?import ...?>` for it |
| "Cannot determine type for property" in a line of script | The script contains `<` or `&`; put inline scripts in `<![CDATA[ ... ]]>` (see the [primer](fxml-primer.md#scripts-inside-the-fxml)) |
| "Can't unambiguously select between ... addListener" | Wrap the function: `new javafx.beans.value.ChangeListener(function (p, old, value) { ... })` |
| `ClassCastException: Double cannot be cast to Integer` | A JavaScript number went to Java where a whole number was needed; write `x \| 0` |
| A button does something strange, such as loading a file | Its function has the same name as a built-in one, such as `load` or `print`; rename it |
| The app isn't in the home menu | It isn't in `apps.txt` in the store (push again), or you haven't logged in again since |
| Users still see the old version | They opened it before you pushed: it updates the next time they open it. Changes to `app.properties` need a new login |
| "isn't in the app store at ..." | The app wasn't pushed, its `app.properties` is incomplete, or the store can't be reached |
