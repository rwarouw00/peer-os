# XUL reference

Every element and attribute myxul knows, and what it becomes in JavaFX. A XUL file's root is `<window>` (or `<dialog>`), in XUL's namespace:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<window title="My app" width="640" height="480"
        xmlns="http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul">
    ...
</window>
```

An element myxul doesn't know is an error, with the file and line, and the list of elements it does know.

## Elements

### Boxes and layout

| Element | JavaFX | Notes |
| --- | --- | --- |
| `<window>`, `<dialog>` | A `Stage` holding a vertical `XulBox` | `title`, `width`, `height`; `onload`, `onclose` (return `false` to stay open), `onunload` |
| `<vbox>`, `<hbox>` | `XulBox`, vertical or horizontal | myxul's own pane: see [Engine internals](engine-internals.md#the-box-layout) |
| `<box orient="vertical">` | `XulBox` | Horizontal unless `orient="vertical"` |
| `<spacer>` | `Region` | Empty; with `flex`, it pushes its neighbours apart |
| `<splitter>` | Turns its box into a `SplitPane` | The children between splitters become the panes, starting in proportion to their `flex` |
| `<grid>`, `<columns>`, `<column>`, `<rows>`, `<row>` | `GridPane` | A `<column flex>` or `<row flex>` grows (equally, as `GridPane` does); others keep their content's size |
| `<groupbox>`, `<caption label>` | `TitledPane` (not collapsible) | Its children are in a vertical box |
| `<tabbox>`, `<tabs>`, `<tab label>`, `<tabpanels>`, `<tabpanel>` | `TabPane`, `Tab` | Each `<tab>` pairs with the `<tabpanel>` in the same place; a tabpanel is a vertical box (`orient="horizontal"` for a row); `selectedIndex` |
| `<toolbox>` | Vertical `XulBox` | |
| `<toolbar>` | `ToolBar` | A `<spacer>` or `flex` child grows |
| `<statusbar>`, `<statusbarpanel label>` | Horizontal `XulBox` of `Label`s | Styled as a status bar by `myxul.css` |
| `<separator>`, `<toolbarseparator>` | `Separator` | Vertical in a row, horizontal in a column; or `orient` |

### Controls

| Element | JavaFX | Attributes |
| --- | --- | --- |
| `<label>` | `Label` | `value` (or `label`, or text inside); `accesskey` |
| `<description>` | `Label`, wrapping | Text inside, or `value` |
| `<button>` | `Button` | `label`, `accesskey`, `image`, `oncommand`, `command` |
| `<toolbarbutton>` | `Button`; `ToggleButton` with `type="checkbox"` | As `<button>`; `checked` |
| `<checkbox>` | `CheckBox` | `label`, `checked`, `oncommand` |
| `<radiogroup>`, `<radio>` | `RadioButton`s in one `ToggleGroup` | `orient` on the group; `label`, `checked`, `oncommand` on each |
| `<textbox>` | `TextField`; `TextArea` with `multiline="true"`; `PasswordField` with `type="password"` | `value`, `readonly`, `placeholder`, `size` (columns); `rows`, `cols`, `wrap="off"` for multiline (they wrap otherwise); `oninput`, `onchange`, and `oncommand` for Enter in a one-line box |
| `<menulist>`, `<menupopup>`, `<menuitem label>` | `ComboBox` | `value`, `editable`; a `<menuitem selected="true">` starts selected; `oncommand` on a choice |
| `<listbox>`, `<listitem label>` | `ListView` | `rows`; `selected="true"` on an item; `onselect` |
| `<tree>`, `<treecols>`, `<treechildren>`, `<treeitem>`, `<treerow>`, `<treecell label>` | `TreeView` | Shows each row's first cell; `container="true" open="true"` starts expanded; `rows`; `onselect` |
| `<progressmeter>` | `ProgressBar` | `value` (0 to 100), `mode="undetermined"` |
| `<image>` | `ImageView` | `src` (relative to the .xul file), `width`, `height` |
| `<canvas>` | `XulCanvas`: a resizable `Region` holding a `Canvas` | Flexes like any element; scripts draw on `.node.getCanvas()` |

### Menus

| Element | JavaFX | Attributes |
| --- | --- | --- |
| `<menubar>` | `MenuBar` | Holds `<menu>`s |
| `<menu>`, `<menupopup>` | `Menu` | `label`, `accesskey`; menus can nest |
| `<menuitem>` | `MenuItem`; `CheckMenuItem` with `type="checkbox"`; `RadioMenuItem` with `type="radio"` | `label`, `accesskey`, `key`, `checked`, `name` (radio items with the same name are one group), `oncommand`, `command` |
| `<menuseparator>` | `SeparatorMenuItem` | |

### Not drawn

| Element | What it does |
| --- | --- |
| `<script>` | JavaScript, inside or in `src="file.js"`; run once the window is built, in document order |
| `<commandset>`, `<command id oncommand>` | An action that elements share with `command="id"`; `disabled` on the command disables them all |
| `<keyset>`, `<key>` | A keyboard shortcut: `key="S"` or `keycode="VK_F3"`, `modifiers` (`accel`, `control`, `shift`, `alt`, `meta`), and `oncommand` or `command`. A `<menuitem key="id">` shows it and runs it |
| `<broadcasterset>`, `<stringbundleset>` | Accepted and ignored |

`<?xul-service Name myxul.service.Class?>`, before the root, gives the window's scripts a Java service; see [Scripting](scripting.md#services).

## Attributes every element has

| Attribute | Effect |
| --- | --- |
| `id` | The element's name for `document.getElementById`, and the JavaFX node's id. Must be unique in the file |
| `flex` | Share of the spare room in its box (a number) |
| `style` | Inline web CSS, translated to JavaFX; see [Styles](styles.md) |
| `class` | JavaFX style classes |
| `width`, `height`, `minwidth`, `minheight`, `maxwidth`, `maxheight` | Sizes in pixels. A maximum holds even a flexing element |
| `disabled`, `hidden`, `collapsed` | `"true"` or not. A hidden or collapsed pane of a split box is taken out of it until shown again |
| `tooltiptext` | A tooltip |
| `accesskey` | One letter: underlined, and Alt+letter (or the letter, in an open menu) chooses it |
| `command` | The `<command>` it runs and follows |
| `align` (boxes) | Across the box: `stretch` (the default), `start`, `center`, `end` |
| `pack` (boxes) | Along the box, when nothing flexes: `start` (the default), `center`, `end` |
| `spacing` (boxes, grids) | Pixels between children: myxul's own attribute, not Mozilla's |

## Event attributes

| Attribute | When | `event` is |
| --- | --- | --- |
| `oncommand` | A button, checkbox, radio, toolbarbutton or menu item is used (a click, Space on a focused button, its access key or shortcut); a menulist choice; Enter in a one-line textbox | The JavaFX `ActionEvent` |
| `onclick`, `ondblclick` | A click (left button) | `MouseEvent` |
| `onkeydown`, `onkeypress`, `onkeyup` | Keys | `KeyEvent` |
| `onmouseover`, `onmouseout` | The mouse enters or leaves | `MouseEvent` |
| `oninput` | A textbox's text changes | `XulEvent` (type `input`) |
| `onchange` | A textbox loses focus after its text changed | `XulEvent` (`change`) |
| `onselect` | A listbox, tree, menulist or tabbox selection changes | `XulEvent` (`select`) |
| `onfocus`, `onblur` | Focus arrives or leaves | `XulEvent` |
| `onload`, `onclose`, `onunload` (window) | The window has opened; is asked to close (return `false` to stay); has closed | `XulEvent` |

In each, `this` is the element (`this.value`, `this.checked`), and `event.target` is the JavaFX control.
