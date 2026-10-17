# Java 7 and JavaFX 2.2 primer

A short guide to the Java 7 and JavaFX 2.2 ideas Skylight uses, and to what's missing compared with the [Java 8 version](../wiki5jfx/javafx-primer.md). Oracle's JavaFX 2.2 documentation and the `javafx.*` Javadoc for JavaFX 2.2 cover the rest.

## The scene graph

A window is a `Stage`; its contents are a `Scene` holding a tree of **nodes**: layout panes containing controls.

| Need | JavaFX 2.2 |
| --- | --- |
| Stack things vertically / horizontally | `VBox`, `HBox` (`HBox.setHgrow(node, Priority.ALWAYS)` to let one grow) |
| Top bar plus a main area | `BorderPane` (`setTop`, `setCenter`) |
| Grid | `GridPane`, or `TilePane` for equal tiles |
| Free positioning (the desktop) | `Pane` with `node.relocate(x, y)` |
| Text input | `TextField`, `PasswordField`, `TextArea` |
| Buttons | `Button`, `ToggleButton`; `setDefaultButton(true)` answers Enter, `setCancelButton(true)` answers Escape |
| Numbers | `Slider` (there's no `Spinner`) |
| Images | `ImageView` of an `Image` |
| Toolbars | `ToolBar` (the desktop panel is one) |
| Dialogs | None built in: a small `Stage` with `initOwner` and `initModality` (see `Desktop.openDialog`) |
| Floating windows without decoration | `Popup` (the home menu and toasts); `setAutoHide(true)` closes it on outside clicks |
| Web pages | `WebView` and its `WebEngine` |

In JavaFX 2.2, panes are filled through their child lists:

```java
HBox buttons = new HBox(8);                 // spacing only
buttons.getChildren().addAll(quit, logIn);
BorderPane root = new BorderPane();
root.setCenter(note);
```

## No lambdas: anonymous classes

Java 7 has no lambdas, so every event handler, listener and callback is an **anonymous class** implementing a one-method interface:

```java
button.setOnAction(new EventHandler<ActionEvent>() {
	@Override
	public void handle(ActionEvent e) {
		save();
	}
});

note.textProperty().addListener(new ChangeListener<String>() {
	@Override
	public void changed(ObservableValue<? extends String> property, String oldText, String newText) {
		changed = true;
	}
});

openDialog("Clear the note?", new Runnable() {
	@Override
	public void run() {
		note.clear();
	}
});
```

A local variable used inside an anonymous class must be declared **`final`**; Java 8 relaxed this to "effectively final", Java 7 didn't. Fields of the enclosing class (like `changed` above) can be used and changed freely.

Java 7 does have the **diamond** (`new HashMap<>()`), **try-with-resources**, **multi-catch** (`catch (Exception | LinkageError e)`), strings in `switch`, and the `java.nio.file` API (`Path`, `Files`), all of which Skylight uses.

## Missing from JavaFX 2.2

What Skylight's Java 8 version uses that JavaFX 2.2 doesn't have, and what this version does instead:

| JavaFX 8 | JavaFX 2.2 replacement in Skylight |
| --- | --- |
| `Alert`, `Dialog`, `ButtonType` (8u40) | A small window built in `Desktop.openDialog` |
| `Spinner` (8u40) | `Slider` with `setSnapToTicks(true)` |
| `Stage.setMaximized` | Size the window to `Screen.getPrimary().getVisualBounds()` after showing it |
| `Node.localToScreen` | `Desktop.toScreen`: `localToScene`, plus the scene's and window's positions |
| `new HBox(spacing, nodes...)`, `new VBox(...)`, `new StackPane(nodes...)` | `Desktop.hbox()` / `vbox()` helpers, or `getChildren().addAll(...)` |
| `new BorderPane(center)`, `new ScrollPane(content)`, `new SplitPane(items...)` | `setCenter`, `setContent`, `getItems().addAll(...)` |
| `PBKDF2WithHmacSHA256` (a Java 8 addition) | PBKDF2 built from `Mac.getInstance("HmacSHA256")` in `Storage` |
| `java.util.Base64` | `javax.xml.bind.DatatypeConverter` |
| `Files.lines`, `Files.list`, streams | `Files.readAllLines`, `Files.newDirectoryStream`, loops |

## Timers and animation

```java
Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
	@Override
	public void handle(ActionEvent e) {
		tickClock();
	}
}));
clock.setCycleCount(Timeline.INDEFINITE);
clock.play();
```

Toasts fade in with a `FadeTransition` and leave after a `PauseTransition`.

## Styling with CSS

JavaFX styles nodes with CSS much like the web, with `-fx-` property names. JavaFX 2.2's default theme is **Caspian** (glossy gradients and rounded buttons); Skylight adds [`skylight.css`](../site7java7/src/skylight/skylight.css) to every window:

```java
node.getStyleClass().add("toast");
```

```css
.toast {
	-fx-background-color: rgba(50, 50, 50, 0.92);
	-fx-background-radius: 6;
}
```

The desktop background is pure CSS: `-fx-background-image` plus `-fx-background-size: cover`. Caspian's colour variables, such as `-fx-background` and `-fx-accent`, keep custom styles consistent with the theme. The same stylesheet works unchanged with JavaFX 8's Modena theme.

## The application thread

All UI work happens on the **JavaFX application thread**. Event handlers and timers already run there. Code running on another thread must hand UI work back with `Platform.runLater(new Runnable() { ... })`.

## Rhino

Java 7 includes **Rhino**, Mozilla's JavaScript engine, through `javax.script`. The Terminal asks for the engine by its general name, so it gets Rhino on Java 7 and Nashorn on Java 8:

```java
ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
engine.put("app", this);          // JavaScript sees the Java object as `app`
Object result = engine.eval(code);
```

Rhino converts a JavaScript `function` into a Java interface with one method, so `app.openDialog('Sure?', function () { ... })` arrives as a `Runnable`. Rhino returns JavaScript numbers as `Double`, so `6 * 7` gives `42.0`.

## Getting JavaFX onto the class path

Java 7 includes JavaFX 2.2 as `jre/lib/jfxrt.jar` but doesn't load it automatically (Java 8 does, from `jre/lib/ext`). Skylight's scripts add it with `-cp`; see [Getting started](getting-started.md#run).
