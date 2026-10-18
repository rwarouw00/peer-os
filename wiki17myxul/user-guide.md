# User guide

## The launcher

![The launcher](images/launcher.png)

`./run.sh` opens the launcher. Its buttons open **Leafpad XUL**, **AbiWord XUL** and the **Gallery**, each in a window of its own; Alt with the underlined letter does the same. **Quit** closes the launcher. myxul exits when its last window closes, so the apps you opened stay open until you close them too.

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

## Gallery

![The Gallery's controls](images/gallery-controls.png)

Every XUL element myxul knows, each doing something, on four tabs: **Boxes** (flex weights, `pack`, `align`, `maxwidth`, a splitter), **Controls** (text boxes, a password box, a drop-down list, radio buttons, a progress meter, a checkbox, buttons sharing a command with Ctrl+G, dialogs), **Lists and trees**, and **Styles** (web CSS translated to JavaFX). What happens is shown in the status bar, with a clock that a script updates.

![The Gallery's boxes](images/gallery-boxes.png)
