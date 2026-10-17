# JavaFX primer

A short guide to the JavaFX 8 ideas Skylight uses. Oracle's JavaFX 8 documentation and the `javafx.*` Javadoc cover the rest.

## The scene graph

A window is a `Stage`; its contents are a `Scene` holding a tree of **nodes**: layout panes containing controls.

| Need | JavaFX 8 |
| --- | --- |
| Stack things vertically / horizontally | `VBox`, `HBox` (`HBox.setHgrow(node, Priority.ALWAYS)` to let one grow) |
| Top bar plus a main area | `BorderPane` (`setTop`, centre) |
| Grid | `GridPane`, or `TilePane` for equal tiles |
| Free positioning (the desktop) | `Pane` with `node.relocate(x, y)` |
| Text input | `TextField`, `PasswordField`, `TextArea` |
| Buttons | `Button`, `ToggleButton`; `setDefaultButton(true)` answers Enter, `setCancelButton(true)` answers Escape |
| Numbers | `Spinner<Integer>` |
| Images | `ImageView` of an `Image` |
| Toolbars | `ToolBar` (the desktop panel is one) |
| Dialogs | `Alert` (JavaFX 8u40 and later) |
| Floating windows without decoration | `Popup` (the home menu and toasts); `setAutoHide(true)` closes it on outside clicks |
| Web pages | `WebView` and its `WebEngine` |

## Events and properties

```java
button.setOnAction(e -> save());                                   // clicks, Enter in a text field
area.setOnMouseDragged(e -> item.relocate(e.getX(), e.getY()));    // mouse events
note.textProperty().addListener((property, oldText, newText) -> changed = true);
```

Most state in JavaFX is a **property** you can listen to (`textProperty()`, `selectedProperty()`, `locationProperty()`), which is how the Web Browser keeps its address bar in step with the page.

## Timers and animation

```java
Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
clock.setCycleCount(Timeline.INDEFINITE);
clock.play();

PauseTransition wait = new PauseTransition(Duration.seconds(7));
wait.setOnFinished(e -> hide());
wait.play();
```

Toasts fade in with a `FadeTransition`.

## Styling with CSS

JavaFX styles nodes with CSS much like the web, with `-fx-` property names. Skylight uses the default **Modena** theme and adds [`skylight.css`](../site5jfx/src/skylight/skylight.css) to every window:

```java
node.getStyleClass().add("toast");
```

```css
.toast {
	-fx-background-color: rgba(50, 50, 50, 0.92);
	-fx-background-radius: 6;
}
.toast .label {
	-fx-text-fill: white;
}
```

The desktop background is pure CSS: `-fx-background-image` plus `-fx-background-size: cover`. Modena's colour variables, such as `-fx-background` and `-fx-accent`, keep custom styles consistent with the theme.

## The application thread

All UI work happens on the **JavaFX application thread**. Event handlers and timers already run there. Code running on another thread must hand UI work back with `Platform.runLater(() -> ...)`.

## Nashorn

Java 8 includes **Nashorn**, a JavaScript (ES5.1) engine, through `javax.script`. The Terminal uses it:

```java
ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
engine.put("app", this);          // JavaScript sees the Java object as `app`
Object result = engine.eval(code);
```

Nashorn converts a JavaScript `function` into any Java interface with one method, so `app.openDialog('Sure?', function () { ... })` arrives as a `Runnable`. Nashorn was removed from Java in version 15, so this is one reason Skylight targets Java 8.
