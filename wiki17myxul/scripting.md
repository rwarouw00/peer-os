# Scripting

A XUL window's JavaScript runs in **Nashorn**, the ECMAScript 5.1 engine built into Java 8. Each window has its own engine, so two windows (two Leafpads, say) never share variables.

Scripts come from `<script>` elements, inline or with `src="file.js"` (relative to the .xul file), and from event attributes. All `<script>`s run once the window is built, in document order, so a script can find every element; then the window is shown and its `onload` runs.

## Globals

| Global | What it is |
| --- | --- |
| `document` | The window's elements: `getElementById(id)`, `getElementsByTagName(tag)` (`"*"` for all), `documentElement`, `title` |
| `window` | The window: `title`, `setTitle()`, `getTitle()`, `close()`, `open(path, args...)`, `arguments`, `alert()`, `confirm()`, `prompt()`, `setTimeout()`, `setInterval()`, `clearTimeout()`, `clearInterval()` |
| `alert(message)` | A message with OK |
| `confirm(message)` | OK and Cancel: `true` for OK |
| `prompt(message, value)` | A text box: the text, or `null` for Cancel |
| `setTimeout(f, ms)`, `setInterval(f, ms)`, `clearTimeout(id)`, `clearInterval(id)` | Run a function later (on the JavaFX thread), once or repeatedly |
| `print(...)` | Nashorn's own: writes to the console |
| Services | Java objects the XUL file asked for, such as `NativeFileService` (see [below](#services)) |

`window.close()` closes without running `onclose`. `window.open("other.xul", "a", "b")` opens another XUL file, relative to this one, in a new window whose `window.arguments` is `["a", "b"]`. The first window's `window.arguments` are the words after the file name on the command line.

## Elements

`document.getElementById("note")` returns the element as XUL scripts expect it. It has XUL's own properties and methods; **everything else goes to the element's JavaFX object**, so the JavaFX API is there too:

```javascript
var note = document.getElementById("note");   // a <textbox multiline="true">: a JavaFX TextArea
note.value = "Hello";                           // XUL's property
note.setAttribute("disabled", true);            // XUL's method
note.selectAll();                               // TextArea's method
note.setWrapText(false);                        // TextArea's method
var area = note.node;                           // the TextArea itself
```

| Property | |
| --- | --- |
| `id`, `tagName`, `localName` | The element's id and tag |
| `value` | A textbox's text, a label's text, a menulist's choice, a progressmeter's value (0 to 100) |
| `label` | A button's, menu item's, tab's or status bar panel's text |
| `checked` | A checkbox, radio, toggle toolbarbutton or checkbox/radio menu item (`true` or `false`) |
| `disabled`, `hidden`, `collapsed` | `true` or `false`; setting them changes the control |
| `selectedIndex` | The selected row of a listbox, tree or menulist, or tab of a tabbox (−1 for none); can be set |
| `parentNode`, `childNodes` | The parent element, and an array of the children |
| `textContent` | The text inside the element in the XUL file |
| `node` | The JavaFX object (a `Node`, or a `MenuItem` or `Tab`) |

| Method | |
| --- | --- |
| `getAttribute(name)` | The attribute's value, `""` if it isn't set. `value` and `checked` follow the control |
| `setAttribute(name, value)` | Sets it and applies it: `label`, `value`, `disabled`, `checked`, `hidden`, `collapsed`, `tooltiptext`, `style`, `flex`, `src`, `mode`, `title` (on the window). Others are kept for scripts to read |
| `removeAttribute(name)`, `hasAttribute(name)` | |
| `appendItem(label)`, `removeItemAt(index)`, `getRowCount()`, `clearItems()` | Rows of a listbox, menulist or tree (a tree's new rows are top-level). `appendItem`, `removeItemAt` and `getRowCount` are XUL's; `clearItems` is myxul's |
| `focus()` | Gives the control the keyboard focus |
| `getElementsByTagName(tag)` | Elements inside this one |

The same element is always the same object: `document.getElementById("a") === document.getElementById("a")`. Calling a method that neither the element nor its JavaFX object has fails with Nashorn's own `TypeError: null is not a function`.

## Events

An event attribute's code is compiled when the window opens, as the body of `function (event) { ... }`, so a syntax error is reported at once (with its line), and the code sees the window's globals: the functions its `<script>`s defined, `document` and `window`.

* `this` is the element: `oncommand="Editor.setWrap(this.checked)"`.
* `event` is the JavaFX event (`event.target` is the control, `event.code` a key's code), or, for events JavaFX reports as a property change, a `XulEvent` with `type` and `target`. The [XUL reference](xul-reference.md#event-attributes) lists which.
* `onclose` can `return false` to keep the window open.

An error while the code runs is reported (file, line and message, in a dialog and on the console) and the window carries on.

## Commands and keys

A `<command>` is an action with an id. Buttons, menu items and keys use it with `command="..."`, and disabling the command disables all of them:

```xml
<commandset>
    <command id="cmd-save" oncommand="Editor.save()"/>
</commandset>
<keyset>
    <key id="key-save" key="S" modifiers="accel"/>
</keyset>
...
<menuitem label="Save" accesskey="S" key="key-save" command="cmd-save"/>
<toolbarbutton label="Save" command="cmd-save"/>
```

```javascript
document.getElementById("cmd-save").setAttribute("disabled", true);   // the menu item, the button and Ctrl+S
```

`modifiers="accel"` is Ctrl (Cmd on macOS). A `<key>` named by a menu item's `key` shows on that item and runs it; other keys work anywhere in the window. Keys without Ctrl, Alt or Meta (F3, Shift+F3) are caught before the focused control sees them, so they work while typing in a text box.

## Services

Scripts can't use Java's file, network or process classes. `Java.type` works only for JavaFX's UI classes (`javafx.scene.*` except `javafx.scene.web` and `javafx.scene.media`, `javafx.geometry`, `javafx.collections`, `javafx.beans`, `javafx.event`, `javafx.animation` and `javafx.util.Duration`), and Java reflection is turned off. Anything else comes from **services**: Java objects a XUL file asks for, by name, before its root element:

```xml
<?xul-service NativeFileService myxul.service.FileService?>
```

The window's scripts then have a global `NativeFileService`, whose public methods they can call. A service is a class in the `myxul.service` package that implements `myxul.service.XulService` and has a public constructor taking the `XulWindow`. Java exceptions from it arrive in scripts as JavaScript exceptions, with `e.message`.

### NativeFileService (`myxul.service.FileService`)

| Method | |
| --- | --- |
| `showOpenDialog()` | The system's open dialog: the chosen path, or `null` |
| `showSaveDialog(suggestedName)` | The save dialog, starting with that name: the path, or `null` |
| `readFile(path)` | The file's text (UTF-8) |
| `writeFile(path, text)` | Writes the text as UTF-8, replacing the file |
| `exists(path)` | Whether it's a file |
| `getFileName(path)` | The last part: `notes.txt` |
| `resolve(path)` | An absolute path, for one relative to the folder myxul was started in |
| `getDataFile(name)` | A file of this app's own: `~/.myxul/<app folder>/name` (the folder is made) |

`readFile` and `writeFile` throw with a readable reason: `the folder /x doesn't exist`, `there is no such file`, `permission denied`.

### AbiCoreEngine (`myxul.service.PageView`)

AbiWord XUL's document engine: a document of paragraphs painted as pages on a `<canvas>`. `attach(canvasNode)`, `setOnChange(function)`, `setText`, `getText`, `type`, `backspace`, `clear`, `applyTextAttribute(name, value)` (`font-weight`, `font-style`, `font-size`, `font-family`), `isBold`, `isItalic`, `setParagraphStyle`, `getParagraphStyle`, `getHeadings`, `getPageCount`, `getParagraphCount`, `getWordCount`.

## Threads

Everything runs on JavaFX's application thread: scripts, events, timers and dialogs. A long loop in a script freezes its window, and the others.
