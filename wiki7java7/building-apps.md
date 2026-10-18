# Building your first app

This tutorial builds **Notes**, an app that keeps one note per user, as a separate app with its own `app.jar`, the way any app that isn't built into Skylight is packaged. It covers the parts of the [App API](app-api.md) most apps need: building a window, reading and saving the user's files, notifications, a confirmation dialog, and stopping the window closing while there are unsaved changes. Every step was tested (see [Limitations](limitations.md#how-it-was-tested)).

![The finished Notes app](images/notes.png)

You need Skylight OS built and running; see [Getting started](getting-started.md).

## 1. Create the folder

```
site7java7/apps/notes/
├── app.properties
├── src/notes/NotesApp.java
└── icons/16.png, 32.png, 48.png, 64.png
```

For the icons, copy the four PNGs from `apps/settings/icons/` for now.

## 2. Describe the app

`app.properties` (see [App manifest](app-manifest.md)):

```properties
title=Notes
class=notes.NotesApp
width=440
height=320
icon.16=icons/16.png
icon.32=icons/32.png
icon.48=icons/48.png
icon.64=icons/64.png
```

## 3. Write the class

`src/notes/NotesApp.java`:

```java
package notes;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import skylight.App;

/** Notes: one note per user, saved in their private files. */
public class NotesApp extends App {

	private final TextArea note = new TextArea();
	private final Label status = new Label();
	private boolean changed = false;

	// 1. Build the window: a toolbar above a text area.
	@Override
	protected Parent build() {
		Button save = new Button("Save");
		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				save();
			}
		});
		Button clear = new Button("Clear");
		clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				askToClear();
			}
		});
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		status.setText("Hello, " + getUsername());

		// 2. Load the saved note. readFile() returns null if there isn't one yet.
		String text = readFile("notes.txt");
		note.setText(text == null ? "" : text);
		note.setWrapText(true);
		note.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> property, String oldText, String newText) {
				changed = true;
			}
		});

		BorderPane root = new BorderPane();
		root.setCenter(note);
		root.setTop(new ToolBar(save, clear, spacer, status));
		return root;
	}

	// 3. Save. writeFile() reports success, or an error to show the user.
	private void save() {
		Result result = writeFile("notes.txt", note.getText());
		if (result.ok) {
			changed = false;
			status.setText("Saved");
			postNotification("Notes", "Your note was saved.");
		} else {
			openDialog("Could not save: " + result.error);
		}
	}

	// 4. Ask before throwing work away. run() is called only if the user clicks OK.
	private void askToClear() {
		openDialog("Clear the note? This can't be undone.", new Runnable() {
			@Override
			public void run() {
				note.clear();
				status.setText("Cleared (not saved yet)");
			}
		});
	}

	// 5. Don't lose unsaved work when the window is closed.
	@Override
	protected boolean canClose() {
		if (!changed) {
			return true;
		}
		openDialog("Close without saving your changes?", new Runnable() {
			@Override
			public void run() {
				changed = false;
				closeWindow();
			}
		});
		return false;
	}
}
```

How it works:

1. **`build()`** returns the window's contents: a `BorderPane` with a `ToolBar` on top and a `TextArea` filling the rest. JavaFX 2.2's `BorderPane` has no constructor that takes the centre node, so it's set with `setCenter`.
2. **Events** use anonymous classes, since Java 7 has no lambdas: `new EventHandler<ActionEvent>() { public void handle(ActionEvent e) { save(); } }`. The `ChangeListener` on `note.textProperty()` notices every edit.
3. **Files:** `readFile` and `writeFile` use the user's private storage, so each user has their own `notes.txt`.
4. **Dialogs:** `openDialog` takes a `Runnable`, whose `run()` is called only if the user clicks OK.
5. **`canClose()`** keeps the window open while there are unsaved changes, and closes it with `closeWindow()` if the user agrees.

## 4. Compile it into app.jar

From `apps/notes/`:

```
mkdir classes
"%JAVA_HOME%\bin\javac" -encoding UTF-8 -source 7 -target 7 -cp "..\..\skylight.jar;%JAVA_HOME%\jre\lib\jfxrt.jar" -d classes src\notes\NotesApp.java
"%JAVA_HOME%\bin\jar" cf app.jar -C classes .
```

On Linux and macOS, use `/` in paths, `:` between class-path entries, and `$JAVA_HOME`. The class path lets the compiler see `skylight.App` and JavaFX. Skylight loads `app.jar` when the app opens; you can delete `classes/` afterwards. After changing the code, rebuild `app.jar` and restart Skylight.

## 5. Run it

Open the **Terminal** and run:

```javascript
app.openWindow('apps/notes/');
```

Type something, click **Save**, close the window and open it again: the note is still there. Change it and close the window without saving: you'll be asked first.

## 6. Add it to the home menu

Add the folder to `apps/apps.txt`:

```
apps/terminal/
apps/settings/
apps/web-browser/
apps/notes/
```

Log out and in again: Notes appears in the home menu. `app.addShortcut('apps/notes/')` in the Terminal puts it on the desktop.

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| "Cannot open app" with `ClassNotFoundException` | `class` in `app.properties` doesn't match the package and class name, or `app.jar` is missing |
| "Cannot open app" mentioning the manifest | A key is missing from `app.properties` |
| `javac` can't find `skylight.App` | Build Skylight first (`build.bat`), and check the `-cp` path |
| `javac` can't find `javafx...` | `jfxrt.jar` isn't on the class path, or your JDK doesn't include JavaFX; see [Getting started](getting-started.md#requirements) |
| "illegal start of expression" at `->` (Java 7's compiler), or "lambda expressions are not supported in -source 1.7" (Java 8's) | Use an anonymous class instead (see the [primer](javafx-primer.md#no-lambdas-anonymous-classes)) |
| "no suitable constructor found" for something like `new HBox(8, a, b)` | That's JavaFX 8; see the [primer](javafx-primer.md#missing-from-javafx-22) for the JavaFX 2.2 way |
| Changes don't show up | Rebuild `app.jar` and restart Skylight; loaded classes stay loaded |
