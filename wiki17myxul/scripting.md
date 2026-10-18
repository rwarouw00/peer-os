# Scripting

A XUL window's JavaScript runs in **Nashorn**, the ECMAScript 5.1 engine built into Java 8. Each window has its own engine, so two windows (two Leafpads, say) never share variables.

Scripts come from `<script>` elements, inline or with `src="file.js"` (relative to the .xul file), and from event attributes. All `<script>`s run once the window is built, in document order, so a script can find every element; then the window is shown and its `onload` runs.

## Globals

| Global | What it is |
| --- | --- |
| `document` | The window's elements: `getElementById(id)`, `getElementsByTagName(tag)` (`"*"` for all), `createElement(tag)`, `documentElement`, `title`, `popupNode` (the element a context menu was opened on) |
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
| `parentNode`, `childNodes`, `firstChild`, `lastChild` | The parent element, an array of the children, and the first and last (`null` if none) |
| `textContent` | The text inside the element in the XUL file |
| `node` | The JavaFX object (a `Node`, or a `MenuItem` or `Tab`) |
| `currentURI`, `contentTitle` | A `<browser>`'s page address and title (read only) |
| `state` | A `<panel>`'s or popup `<menupopup>`'s: `"open"` or `"closed"` |

| Method | |
| --- | --- |
| `getAttribute(name)` | The attribute's value, `""` if it isn't set. `value` and `checked` follow the control |
| `setAttribute(name, value)` | Sets it and applies it: `label`, `value`, `disabled`, `checked`, `hidden`, `collapsed`, `tooltiptext`, `style`, `flex`, `src`, `image`, `mode`, `left`, `top`, `context`, `id`, `title` (on the window). Others are kept for scripts to read. A whole number is written without a decimal point: `setAttribute("left", 100)` gives `"100"` |
| `removeAttribute(name)`, `hasAttribute(name)` | |
| `appendItem(label)`, `removeItemAt(index)`, `getRowCount()`, `clearItems()` | Rows of a listbox, menulist or tree (a tree's new rows are top-level). `appendItem`, `removeItemAt` and `getRowCount` are XUL's; `clearItems` is myxul's |
| `loadURI(uri)`, `goBack()`, `goForward()`, `reload()`, `stop()`, `canGoBack()`, `canGoForward()` | A `<browser>`'s, as in XUL. `loadURI` takes a URL, or a file relative to the .xul file |
| `focus()` | Gives the control the keyboard focus |
| `getElementsByTagName(tag)` | Elements inside this one, as an array |
| `appendChild(child)`, `insertBefore(child, before)`, `removeChild(child)` | See [Making elements](#making-elements) |
| `addEventListener(type, f)`, `removeEventListener(type, f)` | See [Making elements](#making-elements) |
| `openPopup(anchor, position, x, y)`, `openPopupAtScreen(x, y)`, `hidePopup()` | A `<panel>`'s or popup `<menupopup>`'s; see the [XUL reference](xul-reference.md#popups) |

The same element is always the same object: `document.getElementById("a") === document.getElementById("a")`. Calling a method that neither the element nor its JavaFX object has fails with Nashorn's own `TypeError: null is not a function`.

## Making elements

Scripts can make elements and add them to the window, as in XUL. An element made by `document.createElement` is built (given its JavaFX object, its style and its event attributes) when it's appended to an element already in the window, so set its attributes first:

```javascript
var button = document.createElement("button");
button.setAttribute("id", "hello");
button.setAttribute("label", "Hello");
button.setAttribute("oncommand", "alert('Hello')");
document.getElementById("row").appendChild(button);          // now it's shown, and getElementById("hello") finds it
button.addEventListener("command", function (event) {        // this is the element
    this.label = "Clicked";
});
```

* **`appendChild(child)`** adds it at the end; **`insertBefore(child, before)`** in front of one of the children (`before` `null` means the end); **`removeChild(child)`** takes it out (it can go back in later). Appending an element that's already somewhere moves it. A whole tree made before it's appended (a box holding labels, say) is built at once.
* Elements can go in boxes, `<stack>`s, `<toolbar>`s, `<scrollbox>`es and `<panel>`s; `<menuitem>`s in a `<menupopup>`. Anything else is refused, as are a second element with an id that's in use and an element inside itself.
* **`addEventListener(type, f)`** calls `f(event)`, with `this` the element, for `command`, `click`, `dblclick`, `mousedown`, `mousemove`, `mouseup`, `mouseover`, `mouseout`, `contextmenu`, `keydown`, `keyup`, `focus`, `blur`, `popupshown` and `popuphidden`; adding the same function twice adds it once. **`removeEventListener(type, f)`** stops it. The element must be in the window already.
* Event attributes and listeners both run: a button's `oncommand` and its `command` listeners.

The desktop (`ui/desktop.js`) builds its home menu, taskbar, shortcuts and notification tray this way.

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

### Settings (`myxul.service.SettingsService`)

myxul's own settings, shared by every app, in `~/.myxul/settings.properties` (or the folder `-Dmyxul.data` names).

| Method | |
| --- | --- |
| `getBackground()`, `setBackground(path)` | The launcher's and the desktop's picture: one of `getBackgrounds()`, such as `images/backgrounds/3.jpg` |
| `getBackgroundURL()` | The same, as a URL for `background-image: url(...)` |
| `getNotificationTime()`, `setNotificationTime(ms)` | How long notifications stay: 1000 to 60000 milliseconds, or it throws `Notifications can stay for 1 to 60 seconds.` |
| `getShortcuts()`, `setShortcuts(text)` | The desktop's shortcuts: `"app x y"` for each, separated by `;`, where `app` is a folder in `apps/` with an `app.properties` (`"terminal 24 116;settings 24 216"`). `setShortcuts` throws if one names no app or isn't written that way |
| `setOnChange(function)` | Calls the function whenever any window saves a setting (the launcher and the desktop use it to repaint); forgotten when the window closes |

### Notifications (`myxul.service.NotificationService`)

`post(header)` or `post(header, body)` shows a notification at the top right of the screen (of the desktop, while it's open) for the time set in Settings. `setOnPost(function)` calls `function(header, body)` for every notification any window posts: the desktop's tray uses it.

### Desktop (`myxul.service.DesktopService`)

The desktop's own service, used by `ui/desktop.xul`. A XUL file that asks for it becomes the desktop: from then on, until it closes, every window myxul opens is framed on it (see [Engine internals](engine-internals.md#the-desktop)). Only one can be open; asking for a second fails with `A desktop is open already.`

| Method | |
| --- | --- |
| `getApps()` | The apps in `apps/apps.txt`, in order, as a Java `List` (`.size()`, `.get(i)`). Each has `id` (its folder), `title`, `description`, `icon` (the 48-pixel icon's URL) and `smallIcon` (16 pixels) |
| `getApp(id)` | One app, or `null` |
| `open(id)` | Opens the app: `true`, or `false` if there's no such app or its window couldn't open |
| `getWindows()`, `getWindow(id)` | The windows on the desktop, in the order they opened, and one by its number. Each has `id`, `title`, `icon`, `app`, `minimized`, `maximized` and `active` (the window being worked in, unless it was minimized since), and `activate()`, `minimize()`, `toggle()` (a taskbar click), `toggleMaximized()` and `close()` (as its close button: it may ask first; returns whether it closed) |
| `setOnChange(function)` | Calls the function after windows open, close, change title, or are minimized or activated |
| `setArea(node)` | The element below the panel that windows are kept within and notifications appear in: `Desktop.setArea(document.getElementById("area").node)` |
| `getUserName()` | The name of the user myxul runs as, for the clock. There's no login |
| `quit()` | Asks every window to close; `true` if they all did, `false` if one stayed open (it's then brought to the front) |

An app on the desktop is a folder in `apps/` with an `app.properties`:

```properties
title=Terminal
xul=terminal.xul
icon=icons/48.png
icon.small=icons/16.png
description=Run JavaScript, as any myxul app can
```

and a line with its folder's name in `apps/apps.txt`.

### AbiCoreEngine (`myxul.service.PageView`)

AbiWord XUL's document engine: a document of paragraphs painted as pages on a `<canvas>`. `attach(canvasNode)`, `setOnChange(function)`, `setText`, `getText`, `type`, `backspace`, `clear`, `applyTextAttribute(name, value)` (`font-weight`, `font-style`, `font-size`, `font-family`), `isBold`, `isItalic`, `setParagraphStyle`, `getParagraphStyle`, `getHeadings`, `getPageCount`, `getParagraphCount`, `getWordCount`.

## Threads

Everything runs on JavaFX's application thread: scripts, events, timers and dialogs. A long loop in a script freezes its window, and the others.
