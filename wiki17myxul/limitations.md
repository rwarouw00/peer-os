# Limitations

## The XUL it understands

* **A subset of XUL.** The elements in the [XUL reference](xul-reference.md), and their main attributes. Not there: overlays, XBL bindings, DTD entities (`&label;`), `chrome://` URLs, `<stringbundle>`, `<deck>`, `<scale>`, `<tooltip>` elements, `<menupopup>` popups shown other than by `context` or `openPopup`, drag and drop between elements, templates and RDF, and XPCOM (`Components.classes`).
* **Trees show one column.** A `<tree>` is a `TreeView` of each row's first cell; extra `<treecol>`s and cells are ignored.
* **Grid columns grow equally.** `flex` on `<column>` and `<row>` makes them grow, but not in proportion (unlike boxes).
* **Inline styles only.** No stylesheets for XUL files, and only the CSS properties in [Styles](styles.md); the others are ignored with a warning.
* **Scripts run after the window is built**, not as the file is read (see [From the design spec](design-spec.md#settled-along-the-way)).
* **ECMAScript 5.1**, as Nashorn runs it: no `let`, arrow functions or classes.
* **Elements made by scripts** can go in boxes, stacks, toolbars, scroll boxes, panels and menupopups, but not in grids, tab boxes, split boxes or lists (`<listbox>` and `<tree>` rows are added with `appendItem`). `<command>`, `<key>` and `<script>` elements made by a script do nothing. `addEventListener` doesn't take XUL's third (capturing) argument, and has no `input`, `change` or `select` events (the `on` attributes do).
* **A missing method** on an element gives Nashorn's `TypeError: null is not a function`, not a message naming the method.

## The apps

* **AbiWord XUL is a shell.** Typing adds to the end of the document; there's no cursor, selection, clipboard or undo, and bold and italic apply to the whole document. It saves plain text with `#` headings, not AbiWord's own format.
* **Leafpad XUL** has no printing, no character encoding choice (it reads and writes UTF-8), and no recent-files list. Find is case-sensitive.
* **The Web Browser** has no tabs, bookmarks, downloads or history list, and its WebKit is JavaFX 8's, older than today's browsers.
* **Notifications** are kept in a list only on the desktop (its tray), and only until the desktop closes. Opened from the launcher, apps' notifications are only on screen.

## The desktop

* **Its windows are windows.** Each app window is a window of its own, drawn without the system's frame, so the window manager still knows about it: Alt+Tab may list it, and Alt+F4 closes it (asking first, as its close button does). That's what keeps every app's keys and menus working unchanged (see [Engine internals](engine-internals.md#the-desktop)), but a window can be dragged partly off the desktop, and the desktop can't draw windows behind its own home menu's shadow, or with see-through title bars as Skylight's web version did.
* **Resizing** is by the left, right and bottom edges and the bottom corners, not the top. There's no snapping, no keyboard shortcut to switch windows, and no way to close a frozen app other than its close button.
* **One desktop at a time**, for one person: there's no login, by design. Settings, shortcuts and the background are shared by everyone who runs myxul as the same user, and the desktop can't be locked.
* **Apps are found only through `apps/apps.txt`.** A window opened from elsewhere (`window.open`, say) is framed, but gets a plain icon.
* While the desktop is open, every window myxul opens is framed on it, including those opened from a launcher that was already open.

## Java

* **Java 8 with JavaFX 8 only.** Everything was built and tested for Java 8. Java 11 and later have neither JavaFX nor (from 15) Nashorn built in, and myxul wasn't tried on them.
* **One thread.** Scripts, events and timers all run on JavaFX's application thread, so a long-running script freezes every window.

## How it was tested

myxul was built and tested on Linux with:

* **OpenJDK 1.8.0_504**, with **OpenJFX 8** (Ubuntu 18.04's 8u161 packages, unpacked into it), and Java 8's own Nashorn;
* a virtual X display with **metacity** (GNOME's window manager).

The design spec's two Nashorn claims were checked first, in small Java programs: that evaluating an event's code with fresh `Bindings` hides the window's functions (it fails with `"validateAndSave" is not defined`), and that compiling it as `function (event) { ... }` fixes it, with syntax errors found at load. So was what a class filter allows: calls on objects given to scripts work, `Java.type` and reflection don't, and Nashorn's `JSAdapter` handles property reads, writes and calls (but `'name' in javaObject` is false, so the wrappers don't use it).

The final tests ran on a fresh copy of the `site17myxul` folder, built with `build.sh`, with the Notes app (and its step 6, putting it on the desktop) copied out of [Building your first app](building-apps.md) and the example out of `site17myxul/README.md` by a script:

* **An automated driver** working the real JavaFX controls in real windows, with the dialogs and file choosers answered by the test. It made 200 checks:
  * the launcher (mnemonics, a grid column holding its width, opening an app);
  * Leafpad XUL: typing marks the title modified; Ctrl+S on an untitled file asks for a name and writes UTF-8; New with unsaved changes asks, and Cancel and OK both do the right thing; Ctrl+O reopens a file with accents; Find, and F3 finding the next match while typing in the text box; a search with no match; Jump To a line that exists and one that doesn't; Word Wrap and the font option; Delete removes only the selection; Undo, Copy, Paste, Select All; About; the close button with unsaved changes (Cancel stays, OK closes); a file named on the command line, and a new one that Save writes without asking; a save into a folder that doesn't exist, which says so;
  * AbiWord XUL: the welcome document and its headings in the Navigator; the splitter at flex 3 : 1; the canvas filling its pane; typing and Backspace on the canvas; bold from the toolbar and Ctrl+B, with the button following; Ctrl+I; a paragraph style from the Styles list, updating the Navigator and status bar; the font size; a long document running onto more pages; hiding and showing the sidebar (the pane leaves the split and comes back with its divider); saving with headings marked;
  * the Gallery: `flex` 1 : 2 : 3 sharing the spare width in proportion; `pack="center"`, `align="center"`, `maxwidth` holding a flexing text box to 90 pixels; the splitter starting at a third; buttons stretching to a row's height; `onselect` on tabs; `oninput`, and `onchange` when focus leaves; a `<command>` shared by a button, a menu item and Ctrl+G, disabled and enabled for all three at once; a checkbox enabling a button with `setAttribute`; the progress meter; the menulist; radio buttons; prompt, confirm and alert; `appendItem` and `removeItemAt` on a listbox; the tree's rows; `box-shadow` as a `DropShadow`; `text-shadow` warned about with its line; restyling from a script; radio menu items; `setInterval`; closing from a menu;
  * the Terminal: the sample code (print to the output, the result, a notification posted); Ctrl+Enter, with a variable kept from the run before; an error; the services; Ctrl+L;
  * Settings: the launcher showing the default background; the current settings shown; thumbnails with their names as tooltips; choosing a picture saving it, pressing its button and not the old one, and repainting the launcher at once; choosing it again; 3 seconds saved, its notification on screen and gone after 3 seconds; 99 seconds refused by `SettingsService`; a non-number refused; **Try one**; a second Settings window still repainting the launcher after the first had closed;
  * the Web Browser: the start page loading, its own JavaScript running in WebKit, the address bar; an address without `http://` from a local web server; Back enabled, the status; following a link; Back, Forward, F5; a page that can't load; Home;
  * the Composer-style and Komodo-style test files (see [Testing against real-world XUL](real-world-xul.md));
  * the rest of the reference: `<dialog>`, `<image>` and button images, separators' orientation, text inside labels, `onclick`, `ondblclick`, `onmouseover`, `onmouseout`, `onkeydown`, `onkeyup`, `onfocus`, `onblur`, an editable menulist, an undetermined progress meter, `class`, `hidden` and setting it from a script, `parentNode`, `childNodes`, `textContent`, `getElementsByTagName` on an element, `documentElement`, `document.title`, `===` between lookups, a missing id, `setTimeout` and `clearTimeout`, a cancelled prompt, `window.open` with arguments, `window.title`, `onunload`;
  * the class filter refusing `java.lang.System`, `Packages.java.io.File`, `java.lang.Runtime`, reflection, `javafx.stage` and `javafx.scene.web`, and allowing JavaFX's UI classes;
  * twelve mistakes, each refused with its file, line and reason: bad XML, an unknown tag, a syntax error in an event attribute and in a script, a missing script file, a duplicate id, a DOCTYPE with an external entity, a service outside `myxul.service`, an `<overlay>`, a misplaced `<splitter>`, a flex that isn't a number, a missing file; and errors while running (a missing function, and `Java.type("java.io.File")`), reported with their lines while the window carried on;
  * two Leafpads not sharing script variables; the Notes tutorial (save, reopen, Clear with Cancel and OK, closing with and without changes, the access key and tooltip); and the README's example.
* **A second driver for the desktop and the XUL it needed**, the same way. It made 140 checks:
  * `tests/xul/dom.xul`: `createElement` (not built until appended), `appendChild` building it with its attributes and `oncommand`, `getElementById` finding it, `insertBefore`, `childNodes`, `firstChild` and `lastChild`, `removeChild` and appending again elsewhere, moving an element, a duplicate id, an element inside itself and a menuitem in a box refused; a tree made before it's appended; renaming by `setAttribute("id")`; appending to a toolbar (a flexing spacer), a scrollbox (twenty labels, scrolling), a panel and a context menu (with an access key); `addEventListener` for `command` (once when added twice, `this` the element), `removeEventListener`, the mouse events (including a drag), keys, focus, blur and `contextmenu`; an unknown event, a listener added too early, `command` on a label; an error in a listener reported with `dom.xul:21`; `<stack>` filling and placing children and `setAttribute("left")` moving one; `<panel>` at `after_start`, `after_end` and `end_before` with offsets, its size and style, `state`, `onpopupshown` and `onpopuphidden`, `hidePopup` from a button inside, `openPopupAtScreen`, an unknown position; context menus by right-click with `document.popupNode`, an access key choosing an item, a context set by a script, and one naming no menu warned about; a vertical button with an image, an image by URL and taken away; a wrapped description in a narrow column; empty `childNodes` and `getElementsByTagName`; minimizing a window without closing it, and `onunload` running once when it's closed while minimized;
  * the desktop: maximized, the engine's window host, with the system's frame on its own window; the apps in `apps/apps.txt` order (the tutorial's Notes last); the background from Settings; the clock with the user's name; no login or logout; the home menu below its button, a tile per app four to a row with its icon and tooltip, long titles wrapping, closing it again, Alt+H, the Apps and Notifications pages;
  * windows on it: a tile opening its app with no system frame, owned by the desktop, in a frame with its icon, title and three buttons, drawn active, inside the desktop below the panel, at its XUL size plus the frame; its taskbar button pressed, with its 16-pixel icon; `Desktop.open`, the second window a step down and right; taskbar clicks bringing a window forward, minimizing the front one (the next one becoming active), and bringing it back; maximize filling the desktop and dropping the edges, and back to the same place; double-clicking the title bar; the minimize button; dragging the title bar, and not above the panel; the right edge's pointer and resizing; the bottom-left corner resizing both ways; the app's content following; a new title in the title bar and on the button; the close button asking Leafpad about unsaved changes (Cancel keeps it, OK closes it and its button goes); the taskbar's Close;
  * notifications: placed at the desktop's top right below the panel; in the tray with header and body, counted, newest first, one without a body, a long one wrapping, × and Clear; a window opened with `window.open` from an app, framed with a plain icon;
  * shortcuts: the defaults where Settings puts them, long names wrapping, double-click opening, dragging and saving (not rebuilt), kept on the desktop; Add to desktop from the home menu, Remove from desktop, Open; bad shortcuts refused by Settings; a new background showing at once; a second desktop refused; the Notes tutorial's app in the menu, opening framed with its own icon;
  * the power button: Leafpad's unsaved changes asking, Cancel keeping everything and bringing Leafpad forward, OK closing every window and the desktop, and windows plain again afterwards; closing the desktop's own window; a desktop whose XUL has a syntax error, and the next window being a plain one.
* **Real mouse and keyboard**, through xdotool, with the real GTK file dialogs and JavaFX dialogs:
  * opening Leafpad XUL from the launcher with Alt+L; typing; File > Save with the mouse, then typing a name into GTK's Save As dialog; F3 asking what to find, then replacing the match by typing; Edit > Select All with Alt+E then A; Alt+F4 with unsaved changes (Escape for Cancel, then Enter for OK);
  * AbiWord XUL with Alt+A: clicking the page, typing, Ctrl+B, the Sidebar button, Ctrl+S into GTK's dialog;
  * the Gallery with Alt+G: dragging the splitter (checked by the colour under the pointer), clicking the Controls tab, typing a name, Tab, Ctrl+G;
  * the Terminal with Alt+T: code typed and run with Ctrl+Enter (it wrote a file through `NativeFileService`);
  * Settings with Alt+S: clicking a picture (the setting saved, and the launcher's colour under the pointer changed); **Try one** with Alt+T, the notification's window appearing and closing itself;
  * the Web Browser with Alt+W: Ctrl+L, an address typed and Enter (the window's title became the page's); Home and Back with the mouse;
  * the Notes tutorial's Save with Alt+S, and its close question;
  * the launcher's Quit button, after which myxul exited;
  * the desktop, started with `./run.sh ui/desktop.xul`: the home button opening the menu; Leafpad XUL's tile opening it in a frame; typing (the title gaining its `*`); dragging its title bar 200 pixels right and 100 down (it moved exactly that); dragging its right edge 100 pixels (100 wider); its minimize button and its taskbar button; double-clicking its title bar to maximize (filling the desktop below the panel) and again to restore; double-clicking the Terminal's shortcut, Ctrl+Enter, and the notification appearing at the desktop's top right below the panel; the tray; right-clicking the Terminal's taskbar button and choosing Close; Settings from the menu, a picture clicked and the desktop's background changing under the pointer, Settings closed by its frame's close button; dragging a shortcut 300 pixels (saved 300 pixels further right); the tutorial's Notes from the menu, Alt+S saving, its close button; the power button, Leafpad asking, Escape for Cancel (everything stayed), then OK, after which myxul exited.

Testing found these problems, all fixed:

* in the engine: JavaFX buttons not flexing or stretching (their maximum is their preferred size); `maxwidth` not holding a flexing text box; grid columns without flex squeezed below their content; `checked="true"` ignored on checkboxes, radios and toggle buttons; a tree's row count off by one; F3 never reaching its menu item while typing (a `TextArea` uses up keys without modifiers); access keys not working inside open menus, and the letter then typing into the text box; a SplitPane leaving a gap for a hidden pane; syntax-error messages repeating their source line; the class filter's refusal reading as a bare `ClassNotFoundException`;
* in the apps: Java's file errors giving only the path as their message (now `the folder ... doesn't exist`); an unclear question before discarding changes; the Web Browser's address bar empty on the start page (it starts loading before `onlocationchange` is listening, so `start` fills it in); the launcher's panel hiding its background;
* in the test files: a `<splitter>` placed directly in a `<window>`, which is a box, so it was valid XUL and not the mistake intended;
* while building the desktop: descriptions and wrapping labels cut off after one line in some boxes (the box measured them without their width); `setAttribute("left", Math.round(x))` writing `174.0`, which Settings then refused as a shortcut position; a `Popup` showing its content at no size; and, in the mouse test, a click on the launcher's Quit button, which the new Desktop row had moved down.

What wasn't tested:

* **Windows and macOS**, `build.bat` and `run.bat`.
* **Java builds other than OpenJDK 8u504 with OpenJFX 8u161**, such as Oracle JDK 8, Zulu FX or Liberica.
* **Real XUL from Mozilla applications** (see [Testing against real-world XUL](real-world-xul.md)).
* **High-DPI screens, other GTK themes and screen readers.**
* **The desktop with other window managers** than metacity (undecorated windows and their positions are up to the window manager), or on more than one screen.
