# From the design spec

myxul was built from [`myxul/designspec.txt`](../myxul/designspec.txt). This page goes through the spec section by section: what was built, and where the build differs from the spec or had to settle something the spec left open.

| Spec | Built | Notes |
| --- | --- | --- |
| **1. Goal and constraints**: XUL on JavaFX 8 with Nashorn, Java 8 only, our own code | Yes | No libraries at all: myxul uses only Java 8, JavaFX 8 and Nashorn |
| **2. XUL rather than FXML** | Yes | XUL's own tags, mapped to JavaFX by the registry, not JavaFX class names. See [Compared with FXML](comparison.md) |
| **3. JavaFX as renderer, Nashorn for events** | Yes | The tag mappings in section 3 are the ones used, plus several more |
| **4. Engine architecture**: XulParser, XulRegistry, XulScriptManager, XulEventBus, XulDocument | Yes, as `XulParser`, `XulRegistry`, `XulScriptManager`, `XulEvents` and `XulDocument`, with `XulWindow`, `XulEngine`, `XulElement` and `XulBox` | |
| 4.1 The toolkit behind an interface (optional) | No | The spec called it optional. The registry's builders return JavaFX objects directly. A second toolkit would need a builder table of its own; the XUL files wouldn't change |
| 4.2 Parsing: namespace-aware, no external entities | Yes | SAX instead of `DocumentBuilder`, to keep each element's line for error messages; DOCTYPEs are refused outright |
| 4.3 Registry: unknown tags reported, not null | Yes | With the file, line, and the list of known tags |
| 4.4 Proportional flex: GridPane or a custom pane | A custom pane, `XulBox` | Also does `align`, `pack`, `spacing` and margins. `<grid>` uses `GridPane`, where flex columns grow equally |
| 4.4 accesskey to mnemonic, `<key>` to accelerator | Yes | Plus access keys inside open menus, which JavaFX 8 doesn't do |
| 4.5 JavaFX application thread | Yes | Everything runs on it; there are no background threads |
| 4.6 Properties and binding | Not used | Attributes are applied with setters; binding was left for later, as the spec suggests |
| **5.1 Embedded scripts**, alert() provided | Yes | Also `confirm`, `prompt`, timers, `window.open` and `window.arguments` |
| 5.2 Script manager, `document`, invokeFunction | Yes | `document` is a script object over `XulDocument` |
| 5.3 The event bug (eval with fresh Bindings) and its fix | The fix, as specified | Each attribute compiled once as `function (event) { ... }`; syntax errors at load, with lines |
| 5.4 One script context per window | One engine per window | |
| 5.5 ClassFilter | Yes, always on | It allows JavaFX's UI packages rather than nothing, so scripts can make `TreeItem`s and the like; files come only through services |
| Review in 8.2: raw JavaFX nodes or XUL element wrappers? | **Both** | Elements are wrappers with XUL's properties and methods, passing anything else to the JavaFX object; `.node` gives the object itself |
| **6. Under 15 core classes** | 12 core classes, 15 with `Main`, `XulEvent` and `XulException` | About 3,800 non-blank lines with comments, including `bootstrap.js` and the services |
| **7. Testing against real-world XUL** | In stages, with test files in the style of SeaMonkey Composer and Komodo | See [Testing against real-world XUL](real-world-xul.md) |
| **8.1 Leafpad XUL** | Yes, and more: Save As, Find, Find Next, Jump To, Word Wrap, a font option, unsaved-changes checks, a file on the command line | The spec's Delete bug (it cleared the whole text) and wrong `xmlns` were fixed as its review said |
| **8.2 AbiWord XUL shell** | Yes | The spec's layout, with a page engine (`PageView`) painting Letter-sized pages on the canvas, a heading outline, paragraph styles, and bold and italic for the whole document. The spec styled the canvas as the page (white, 816 px wide, with a shadow). Here the canvas is the whole grey viewport, and `PageView` paints each page, with its shadow, inside it, so the canvas's style is just its grey background |
| **9. Build order** | Steps 1 to 7, in order | Step 5's style translator covers the properties in [Styles](styles.md) |

## Settled along the way

* **Services.** The spec has scripts call Java objects (`NativeFileService`, `AbiCoreEngine`) without saying how they're given. A XUL file asks for them by name, with `<?xul-service Name myxul.service.Class?>`; only classes in `myxul.service` that implement `XulService` can be named.
* **Where scripts run.** XUL runs `<script>`s as it reads them, so a script can't see elements below it. myxul runs them after the whole window is built, so `getElementById` always works, and then runs `onload`.
* **F3 in a text box.** The spec's Leafpad binds F3 to Find Next. A JavaFX `TextArea` uses up keys without modifiers, so myxul catches those keys before the focused control (see [Engine internals](engine-internals.md#keys)).
* **Hiding a split pane.** The AbiWord shell's sidebar is in a `SplitPane`, which can't hide a pane; myxul takes the pane out and puts it back.

## Beyond the spec

Skylight OS's **Terminal**, **Settings** and **Web Browser** were ported to myxul afterwards (from site11fxml's FXML versions; see [Compared with FXML](comparison.md#skylight-oss-apps-from-fxml-to-xul)). They needed a few things the spec didn't cover:

* **`<browser>`** (and `<iframe>`), XUL's own element for web content, made a JavaFX `WebView`, with XUL's `loadURI`, `goBack`, `goForward`, `reload` and `stop`, and `onload`, `onerror` and `onlocationchange` events;
* **two services**: `SettingsService`, myxul's shared settings (the launcher's background and the notification time), which tells open windows when they change, and `NotificationService`, Skylight-style notifications at the top right of the screen;
* **`background-image`**, `background-size`, `background-position` and `background-repeat` in the style translator, for the launcher's background;
* **`XulWindow.addCloseListener`**, so services can forget a window when it closes.
