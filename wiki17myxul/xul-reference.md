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
| `<stack>` | `XulBox.Stack`, a `Pane` | Children on top of each other, later ones in front. A child with `left` and `top` (pixels) is placed there at its own size; the others fill the stack. The desktop's shortcuts are in one |
| `<scrollbox orient="vertical">` | A `ScrollPane` around a `XulBox` | A box whose children scroll when they don't fit; horizontal unless `orient="vertical"` |

### Controls

| Element | JavaFX | Attributes |
| --- | --- | --- |
| `<label>` | `Label` | `value` (or `label`, or text inside); `accesskey` |
| `<description>` | `Label`, wrapping | Text inside, or `value` |
| `<button>` | `Button` | `label`, `accesskey`, `image` (a file relative to the .xul file, or a URL), `orient="vertical"` (the image above the label), `oncommand`, `command` |
| `<toolbarbutton>` | `Button`; `ToggleButton` with `type="checkbox"` | As `<button>`; `checked` |
| `<checkbox>` | `CheckBox` | `label`, `checked`, `oncommand` |
| `<radiogroup>`, `<radio>` | `RadioButton`s in one `ToggleGroup` | `orient` on the group; `label`, `checked`, `oncommand` on each |
| `<textbox>` | `TextField`; `TextArea` with `multiline="true"`; `PasswordField` with `type="password"` | `value`, `readonly`, `placeholder`, `size` (columns); `rows`, `cols`, `wrap="off"` for multiline (they wrap otherwise); `oninput`, `onchange`, and `oncommand` for Enter in a one-line box |
| `<menulist>`, `<menupopup>`, `<menuitem label>` | `ComboBox` | `value`, `editable`; a `<menuitem selected="true">` starts selected; `oncommand` on a choice |
| `<listbox>`, `<listitem label>` | `ListView` | `rows`; `selected="true"` on an item; `onselect` |
| `<tree>`, `<treecols>`, `<treechildren>`, `<treeitem>`, `<treerow>`, `<treecell label>` | `TreeView` | Shows each row's first cell; `container="true" open="true"` starts expanded; `rows`; `onselect` |
| `<progressmeter>` | `ProgressBar` | `value` (0 to 100), `mode="undetermined"` |
| `<image>` | `ImageView` | `src` (a file relative to the .xul file, or a URL), `width`, `height` |
| `<canvas>` | `XulCanvas`: a resizable `Region` holding a `Canvas` | Flexes like any element; scripts draw on `.node.getCanvas()` |
| `<browser>`, `<iframe>` | `WebView` (WebKit) | `src`: a URL, or a file relative to the .xul file. Methods `loadURI`, `goBack`, `goForward`, `reload`, `stop`, `canGoBack`, `canGoForward`; properties `currentURI`, `contentTitle`; events `onload`, `onerror`, `onlocationchange` |

### Menus

| Element | JavaFX | Attributes |
| --- | --- | --- |
| `<menubar>` | `MenuBar` | Holds `<menu>`s |
| `<menu>`, `<menupopup>` | `Menu` | `label`, `accesskey`; menus can nest |
| `<menuitem>` | `MenuItem`; `CheckMenuItem` with `type="checkbox"`; `RadioMenuItem` with `type="radio"` | `label`, `accesskey`, `key`, `checked`, `name` (radio items with the same name are one group), `oncommand`, `command` |
| `<menuseparator>` | `SeparatorMenuItem` | |

### Popups

Popups go in a `<popupset>`, which shows nothing itself:

| Element | JavaFX | Attributes and methods |
| --- | --- | --- |
| `<popupset>` | Nothing | Holds `<panel>`s and `<menupopup>`s |
| `<panel>` | A `Popup` holding a vertical `XulBox` (`orient="horizontal"` for a row) | Any elements inside; `width`, `height`, `style`. Closes when the user clicks outside it or presses Escape, unless `noautohide="true"`. Methods `openPopup(anchor, position, x, y)`, `openPopupAtScreen(x, y)`, `hidePopup()`; property `state` (`"open"` or `"closed"`); events `onpopupshown`, `onpopuphidden`. The desktop's home menu is one |
| `<menupopup>` in a `<popupset>` | `ContextMenu` | `<menuitem>`s, `<menu>`s and `<menuseparator>`s, with access keys; the same methods and events as `<panel>`. Shown by `context="its-id"` on another element |

`openPopup`'s `position` is XUL's: `after_start` (below the anchor, left edges lined up; the default), `after_end` (below, right edges lined up), `before_start`, `before_end` (above), `end_before` (to the right, tops lined up), `start_before` (to the left), or `overlap`; then the popup is moved by `x` and `y`.

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
| `context` | The id of a `<menupopup>` or `<panel>` in a `<popupset>`, opened at the pointer on a right-click (or the menu key), with `document.popupNode` set to this element |
| `left`, `top` (in a `<stack>`) | Where the element goes, in pixels from the stack's top left; `setAttribute("left", 100)` moves it |
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
| `onmousedown`, `onmouseup` | A mouse button goes down or up | `MouseEvent` (`event.button` is `PRIMARY`, `SECONDARY` or `MIDDLE`; `event.screenX` and `event.screenY` the pointer) |
| `onmousemove` | The mouse moves over the element, or drags from it (JavaFX keeps sending a drag to the element the button went down on, even outside it) | `MouseEvent` (`event.eventType` is `MOUSE_MOVED` or `MOUSE_DRAGGED`) |
| `oncontextmenu` | A right-click or the menu key | `ContextMenuEvent` |
| `onpopupshown`, `onpopuphidden` (`<panel>`, `<menupopup>`) | The popup has opened; has closed | `WindowEvent` |
| `oninput` | A textbox's text changes | `XulEvent` (type `input`) |
| `onchange` | A textbox loses focus after its text changed | `XulEvent` (`change`) |
| `onselect` | A listbox, tree, menulist or tabbox selection changes | `XulEvent` (`select`) |
| `onfocus`, `onblur` | Focus arrives or leaves | `XulEvent` |
| `onload`, `onclose`, `onunload` (window) | The window has opened; is asked to close (return `false` to stay); has closed | `XulEvent` |
| `onlocationchange`, `onload`, `onerror` (`<browser>`) | A page starts loading; has loaded; couldn't be loaded. These are myxul's: Mozilla's `<browser>` used progress listeners instead | `XulEvent` |

In each, `this` is the element (`this.value`, `this.checked`), and `event.target` is the JavaFX control. Scripts can also listen with `addEventListener`, for these events without the `on`; see [Scripting](scripting.md#making-elements).
