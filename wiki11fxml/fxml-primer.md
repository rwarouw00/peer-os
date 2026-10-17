# FXML primer

A short guide to FXML, JavaFX's XML format for user interfaces, and to its scripts, as Skylight uses them. Oracle's "Introduction to FXML" for JavaFX 8 covers the rest. For the JavaFX controls themselves, see the [Java 8 version's primer](../wiki5jfx/javafx-primer.md).

## Elements, attributes and imports

An FXML file is a tree of JavaFX objects. Each element creates an object; each attribute sets one of its properties:

```xml
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<HBox xmlns:fx="http://javafx.com/fxml" spacing="8">
	<Label text="Show notifications for"/>
	<Button text="Save" defaultButton="true"/>
</HBox>
```

is the same as this Java:

```java
HBox box = new HBox();
box.setSpacing(8);
Label label = new Label(); label.setText("Show notifications for");
Button save = new Button(); save.setText("Save"); save.setDefaultButton(true);
box.getChildren().addAll(label, save);
```

* **`<?import ...?>`** works like Java's `import`. A class used without one gives "... is not a valid type".
* **Child elements** go into the parent's default list (a pane's children, a toolbar's items). Other properties are set with an element named after them: `<top>...</top>` for a `BorderPane`, `<graphic>...</graphic>` for a button, `<padding><Insets top="14" .../></padding>`.
* **Static properties** such as `HBox.hgrow="ALWAYS"` or `GridPane.rowIndex="2"` are attributes too.
* **Constructor arguments** become attributes when the class marks them: `<Image url="..." requestedWidth="128"/>`, `<SpinnerValueFactory.IntegerSpinnerValueFactory min="1" max="60"/>`.
* **`@` in a URL** means "relative to this FXML file": `<Image url="@../../images/backgrounds/1.jpg"/>`.
* **`fx:define`** creates objects that aren't part of the tree, such as a `ToggleGroup`, and **`$name`** refers to them: `toggleGroup="$backgrounds"`.
* **`fx:id="note"`** names an object, for scripts (as a variable) and for Java controllers (as an `@FXML` field). It also becomes the node's CSS id, so `scene.lookup("#note")` finds it.

## Event handlers

There are two kinds:

| Attribute | Meaning | Used by |
| --- | --- | --- |
| `onAction="save()"` | A line of script, run on each click. The variable `event` holds the event. | Script apps (`fxml=`) |
| `onAction="#save"` | Call the controller's method `save()` (with or without an `ActionEvent` parameter) | Java apps using `loadFxml`, and the desktop's `ui/` files |

## Scripts

`<?language javascript?>` at the top of the file turns on scripts, run by **Nashorn**, the JavaScript engine in Java 8 (ES5.1). An `<fx:script>` element runs once, while the file loads, at the point where it appears, so put it at the end, after the elements it uses:

```xml
<fx:script source="notes.js"/>        <!-- a file beside the FXML -->
<fx:script> ... </fx:script>          <!-- or inline -->
```

In a script, every `fx:id` is a variable, and so is **`app`**, which Skylight adds: the [App API](app-api.md). Functions and variables defined with `var` at the top level stay available to the event handlers, and keep their values. Each window has its own script engine, so two Notes windows don't share variables.

### Scripts inside the FXML

The file is still XML, so `<` and `&` in a script would be read as markup. Wrap inline scripts in a CDATA section:

```xml
<fx:script><![CDATA[
	if (target >= 0 && target < history.getEntries().size()) { ... }
]]></fx:script>
```

Without it, the error ("Cannot determine type for property", or a parse error) points at the script's line, not at the cause. Script files (`source="..."`) don't need this.

### JavaScript and Java

Scripts call Java objects directly (`note.getText()`, `app.writeFile(...)`), and Nashorn converts between the two languages. Three conversions need care:

* **Functions as Java interfaces.** A JavaScript function is accepted wherever Java expects an interface with one method: `app.openDialog(message, function () { ... })`, `app.setCanClose(function () { return true; })`. But when a method has several versions that each take such an interface, Nashorn can't choose, and says "Can't unambiguously select between...". `addListener` is the common case; name the interface:

  ```javascript
  engine.locationProperty().addListener(new javafx.beans.value.ChangeListener(function (property, old, value) { ... }));
  note.textProperty().addListener(new javafx.beans.InvalidationListener(function () { ... }));
  ```

* **Numbers.** JavaScript has one kind of number. When a Java method takes an `Object` that must be an `Integer`, as a `Spinner`'s value does, `60000 / 1000` arrives as a `Double` and fails with a `ClassCastException`. `x | 0` makes it a whole number, which Nashorn passes as an `Integer`. Java fields and parameters declared as `int` convert by themselves.
* **Strings.** Java strings work as JavaScript strings, so `==` and `+` behave as expected.

### Names to avoid

Nashorn has built-in functions called `load`, `print`, `quit`, `exit` and `eval`. A function of yours with one of those names may be hidden by the built-in one in event handlers: a button with `onAction="load(...)"` ends up trying to load a script file. Skylight's Web Browser calls its function `openAddress` for that reason. Also avoid naming an `fx:id` `app`.

## Controllers

Instead of scripts, an FXML file can have a **controller**, a Java object whose `@FXML` fields receive the `fx:id` objects and whose methods handle `#name` events. Skylight's desktop works this way: `Desktop` is the controller of every file in `ui/`. A Java app does the same with `loadFxml`, which makes the app object the controller:

```java
public class NotesApp extends App {
	@FXML private TextArea note;

	@Override
	protected Parent build() {
		try {
			return loadFxml("app.fxml");     // fx:id="note" fills the field above
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@FXML
	private void save() {                 // onAction="#save"
		writeFile("notes.txt", note.getText());
	}
}
```

Don't put `fx:controller="..."` in a file that Skylight loads; Skylight sets the controller itself.

## Styles

FXML and CSS fit together: `styleClass="terminal-code"` on an element, and a rule for `.terminal-code` in [`ui/skylight.css`](../site11fxml/ui/skylight.css), which Skylight adds to every window. A `style="..."` attribute sets CSS on one element.
