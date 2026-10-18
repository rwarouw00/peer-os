# User guide

## The launcher

![The launcher](images/launcher.png)

`./run.sh` opens the launcher. Its buttons open **Leafpad XUL**, **AbiWord XUL**, the **Terminal**, the **Web Browser**, **Settings** and the **Gallery**, each in a window of its own; Alt with the underlined letter does the same. Behind it is the background chosen in Settings, which changes as soon as a new one is chosen. **Quit** closes the launcher. myxul exits when its last window closes, so the apps you opened stay open until you close them too.

The launcher is `ui/launcher.xul`, a XUL file like the apps; add your own apps to it (see [Building your first app](building-apps.md#5-add-it-to-the-launcher)).

## Leafpad XUL

![Leafpad XUL](images/leafpad.png)

A plain-text editor in the spirit of Lubuntu's Leafpad. The title shows the file's name, with a `*` when it has unsaved changes.

| Menu | Item | Key |
| --- | --- | --- |
| File | New, Open..., Save, Save As..., Quit | Ctrl+N, Ctrl+O, Ctrl+S, Ctrl+Shift+S, Ctrl+Q |
| Edit | Undo, Redo, Cut, Copy, Paste, Delete, Select All | The text box's own keys work too, such as Ctrl+Z, Ctrl+X, Ctrl+C, Ctrl+V and Ctrl+A |
| Search | Find..., Find Next, Jump To... | Ctrl+F, F3 |
| Options | Word Wrap, Monospace Font | |
| Help | About | |

* **Open** and **Save As** use the system's file dialogs (on Linux, GTK's). Files are read and written as UTF-8.
* **New**, **Open**, **Quit** and the window's close button ask before throwing away unsaved changes.
* **Find** asks for the text and selects the next match after the cursor, going back to the top when it reaches the end; **Find Next** (F3) looks for the same text again. Matches are case-sensitive.
* **Jump To** moves the cursor to the start of a line.
* `./run.sh apps/leafpad/leafpad.xul notes.txt` opens `notes.txt`, or starts a new file of that name, which **Save** writes without asking.
* With a menu open, the underlined letter chooses an item, and the arrow keys and Enter work as usual.

## AbiWord XUL

![AbiWord XUL](images/abiword.png)

The shell of a word processor: the layout of a real one, around a simple document engine.

* **Click the page and type.** Enter starts a new paragraph; Backspace removes the last character. Typing always goes to the end of the document: there's no cursor to move and no selection.
* **B** and **I** (or Ctrl+B and Ctrl+I) make the whole document bold or italic; the size list sets its size.
* **Styles** (in the sidebar) sets the style of the paragraph you're typing: Normal, Heading 1 or Heading 2. **Navigator** lists the document's headings.
* **Sidebar** hides and shows the sidebar; the splitter between the page and the sidebar can be dragged.
* The **status bar** shows the number of pages, words, and the current paragraph's style.
* **New**, **Open** and **Save** (or Ctrl+N, Ctrl+O, Ctrl+S) work with text files: each paragraph a line, headings starting with `# ` or `## `, so a saved document opens with its headings again.

## Terminal

![The Terminal](images/terminal.png)

Ported from Skylight OS. Type JavaScript and click **Run** (or press Ctrl+Enter). The code runs in the Terminal window's own Nashorn engine, the same as any myxul app's scripts, so it can try anything an app can:

```javascript
Notifications.post('Hello', 'Sent from the Terminal');
print('This window is called ' + window.title);
6 * 7
```

`print` writes to the output box, the result of the last line is shown (`=> 42`), and errors are shown as `Error: ReferenceError: "nope" is not defined`. Variables you set stay for later runs. The Terminal has the `Notifications`, `Settings` and `NativeFileService` services, and `document` and `window` are its own. **Clear output** (Ctrl+L) empties the output box; the splitter between the boxes can be dragged.

## Settings

![Settings](images/settings.png)

Ported from Skylight OS.

* **Background**: click a picture (hover to see its name). The launcher's background changes at once, and is remembered; the picture in use stays pressed.
* **Notifications**: how many seconds notifications stay on screen (1 to 60). Click **Save**, or press Enter; **Try one** shows one.

Settings are kept in `~/.myxul/settings.properties`, for every app.

### Notifications

![A notification](images/notification.png)

Apps can post notifications (the Terminal's sample does, and so does Settings when it saves). Each appears at the top right of the screen with a header and a body, and closes itself after the time set in Settings, or when its **×** is clicked. Several stack downwards.

## Web Browser

![The Web Browser](images/web-browser.png)

Ported from Skylight OS. Type an address and press Enter or click **Go** (`http://` is added if you leave it out). **◀** and **▶** (Alt+Left and Alt+Right) go back and forward, **↻** (F5) reloads, **✕** stops a page loading, **Home** goes back to the start page, and Ctrl+L puts the cursor in the address bar. The status bar says when a page is loading, done, or couldn't be loaded, and the title is the page's.

Pages are drawn by JavaFX's `WebView`, a WebKit browser that runs JavaScript and CSS. It is JavaFX 8's WebKit (its user agent says `AppleWebKit/604.1`, the WebKit of Safari 11, from 2017), so some current websites may not work fully.

![A page from a web server](images/web-browser-test.png)

## Gallery

![The Gallery's controls](images/gallery-controls.png)

Every XUL element myxul knows, each doing something, on four tabs: **Boxes** (flex weights, `pack`, `align`, `maxwidth`, a splitter), **Controls** (text boxes, a password box, a drop-down list, radio buttons, a progress meter, a checkbox, buttons sharing a command with Ctrl+G, dialogs), **Lists and trees**, and **Styles** (web CSS translated to JavaFX). What happens is shown in the status bar, with a clock that a script updates.

![The Gallery's boxes](images/gallery-boxes.png)
