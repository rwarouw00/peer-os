# Engine internals

For people changing myxul itself. The classes are described in [Architecture](architecture.md).

## The box layout

JavaFX's `HBox` and `VBox` can make children grow, but only equally: `Priority.ALWAYS` shares the spare room evenly among the children that have it. XUL shares it by **weight**, so `flex="1"` next to `flex="3"` gets a quarter and three quarters of the spare room. myxul's `XulBox` (a `Pane`) lays out its children the XUL way, in `layoutChildren()`:

1. **Across the box** (height in an `<hbox>`): with `align="stretch"` (XUL's default) each child gets the box's full size; with `start`, `center` or `end`, its preferred size, placed accordingly.
2. **Along the box**: every child starts at its preferred size. The spare room (the box's size, less the children's and the gaps) is handed out in proportion to `flex`. A child that reaches its maximum keeps it, and what it couldn't take is shared again among the rest. If there's too little room, flexing children shrink first, down to their minimum sizes, then the others do.
3. **Packing**: with room to spare and nothing flexing, `pack="center"` or `"end"` moves the children along.

`spacing` puts a gap between children, and a `margin` style (stored on the child) is kept around it.

**Maximum sizes.** A JavaFX `Button`'s maximum size is its preferred size, so a button would never flex or stretch. XUL buttons do. `XulBox` therefore ignores a control's own maximum unless the XUL set one, with `maxwidth`/`maxheight` or `max-width`/`max-height` in a style. Those hold even a flexing child, and clamp its preferred size too.

The box's own preferred and minimum sizes are the children's, added along the box and the largest across it, plus gaps and insets.

## Splitters

A box with `<splitter>` children isn't built as a `XulBox`: it becomes a JavaFX `SplitPane`, in the box's orientation. The children between splitters are its panes (several children between two splitters are wrapped in a box). The dividers start in proportion to each pane's `flex`, and a pane without flex doesn't grow when the window does.

A `SplitPane` can't hide a pane: it leaves an empty space and a divider. So when a pane is given `hidden` or `collapsed`, myxul takes it out of the split pane, remembering the dividers, and puts it back in its place when it's shown again. That's how AbiWord XUL's **Sidebar** button works.

## Commands

`<command>` elements are found before the window is built, so any element can refer to one. An element with `command="cmd-save"` runs the command's `oncommand` when used, unless it has an `oncommand` of its own. Setting `disabled` on the command (in the XUL, or with `setAttribute`) sets it on every element that uses it.

## Keys

Each `<key>` becomes a JavaFX `KeyCombination`: `key="S"` or `keycode="VK_F3"` (`VK_RETURN` and `VK_BACK` are translated to JavaFX's names), and the `modifiers` (`accel` is JavaFX's Shortcut: Ctrl, or Cmd on macOS).

* A menu item with `key="..."` gets it as its **accelerator**, shown in the menu. Pressing it runs the menu item's command, or the key's own if the item has none.
* Other keys go into the window's **scene accelerators**.
* **Keys without Ctrl, Alt or Meta** (F3, Shift+F3) are also caught by an event filter on the scene, before the focused control sees them. A JavaFX `TextArea` uses up key presses without modifiers, so without the filter F3 never reached the accelerators while typing in Leafpad XUL, which testing showed. A menu item's filter fires the item, so it doesn't run twice.

## Menus and access keys

`accesskey="N"` becomes a JavaFX mnemonic: myxul puts `_` before the letter in the label (and doubles any `_` already there). JavaFX then underlines it and handles Alt+N for buttons, labels and the menu bar's menus.

JavaFX 8 doesn't handle access keys for the items of an open menu: pressing the letter did nothing in the menu, and typed into the text box behind it. So myxul adds them. An event filter watches for a letter while one of the window's menus is open, and chooses the item with that access key (toggling a checkbox item first, as a click would). Submenus are checked before their parents. A letter that matches nothing in the open menu is swallowed, and so is the typed character that follows the key press.

## Scripts

Each window's `XulScriptManager` makes a Nashorn engine with `NashornScriptEngineFactory.getScriptEngine(args, loader, classFilter)` (available since Java 8u40), with `--language=es5`. The class filter lets `Java.type` see only JavaFX's UI packages, and with a class filter present Nashorn also refuses Java reflection.

**Event attributes** are compiled once each, when the window is built, by evaluating `(function (event) {\n` + code + `\n})` with the file name set to `leafpad.xul (oncommand on line 23)`. The design spec's first idea was to evaluate the code with fresh `Bindings` holding `event`. That runs against a new, empty set of globals, so the window's own functions aren't defined; the spec's review found this, and testing confirmed it. Errors in the compiled code report their line within the attribute, which myxul adds to the element's line.

**Inline `<script>` text** is padded with blank lines so that error line numbers are the .xul file's.

**Element wrappers** are Nashorn `JSAdapter` objects (made in `bootstrap.js`): `__get__`, `__put__` and `__call__` hooks that send XUL property and method names to the `XulElement` and other names to its JavaFX object. Each element keeps its wrapper, so `===` works. Calls to Java methods go through a small switch on the number of arguments, since Java methods in Nashorn have no `apply`.

## Errors

Everything that can go wrong in a XUL file is a `XulException` with the file and line: XML errors (from SAX's line), unknown tags and bad attributes (from the element's line), script syntax errors (from Nashorn's line within the script or attribute). `XulEngine.report` puts it in its error list, prints it as `myxul: file:line: message`, and shows it in a dialog. Warnings (styles that were ignored, a missing XUL namespace) are printed and listed, but don't stop anything.

Nashorn's messages are tidied: the location it appends (`in file at line number n`) and the source line and caret of syntax errors are dropped, since myxul gives the file and line itself. A Java exception from a service keeps its class name (`NoSuchFileException: ...`) unless the service gave it a readable message.

## Extending myxul

* **A new tag:** add a builder in `XulRegistry`'s constructor (`builders.put("mytag", (e, w) -> ...)`) returning the JavaFX object; attributes every element shares are applied for you. If the tag has attributes scripts should be able to change, handle them in `applyAttribute`.
* **A new event attribute:** wire it in `XulEvents.wire`, calling the compiled handler with the element and an event.
* **A new service:** a class in `myxul.service` implementing `XulService`, with a public constructor taking the `XulWindow`; its public methods are what scripts can call.
