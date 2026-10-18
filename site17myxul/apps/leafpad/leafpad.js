// Leafpad XUL: what the editor does. leafpad.xul is the window.
// NativeFileService is a Java service (myxul.service.FileService) given to
// this window by the <?xul-service?> line in leafpad.xul.

var EditorController = (function () {
	var path = null;        // the file being edited, or null for Untitled
	var modified = false;
	var loading = false;    // true while the editor sets the text itself
	var lastSearch = "";

	function text() {
		return document.getElementById("editor-textarea");
	}

	function name() {
		return path == null ? "Untitled" : NativeFileService.getFileName(path);
	}

	function updateTitle() {
		window.setTitle((modified ? "*" : "") + name() + " - Leafpad XUL");
	}

	function show(content, newPath) {
		loading = true;
		text().setText(content);
		loading = false;
		path = newPath;
		modified = false;
		text().positionCaret(0);
		updateTitle();
	}

	// True if it's fine to throw away the current text: nothing changed, or the user said so.
	function canDiscard() {
		return !modified || confirm("\"" + name() + "\" has unsaved changes. Discard them?");
	}

	function open(file) {
		try {
			show(NativeFileService.readFile(file), file);
			return true;
		} catch (e) {
			alert("Can't open " + file + ": " + e.message);
			return false;
		}
	}

	function writeTo(file) {
		try {
			NativeFileService.writeFile(file, text().getText());
			path = file;
			modified = false;
			updateTitle();
			return true;
		} catch (e) {
			alert("Can't save " + file + ": " + e.message);
			return false;
		}
	}

	return {
		text: text,

		// onload: open the file named on the command line, if there is one.
		start: function () {
			if (window.arguments.length > 0) {
				var file = NativeFileService.resolve(window.arguments[0]);
				if (NativeFileService.exists(file)) {
					open(file);
				} else {
					path = file;   // a new file: Save writes it
					updateTitle();
				}
			}
			text().requestFocus();
		},

		markModified: function () {
			if (!loading && !modified) {
				modified = true;
				updateTitle();
			}
		},

		isModified: function () {
			return modified;
		},

		getPath: function () {
			return path;
		},

		newFile: function () {
			if (canDiscard()) {
				show("", null);
			}
		},

		openFile: function () {
			if (!canDiscard()) {
				return;
			}
			var file = NativeFileService.showOpenDialog();
			if (file != null) {
				open(file);
			}
		},

		saveFile: function () {
			return path == null ? this.saveFileAs() : writeTo(path);
		},

		saveFileAs: function () {
			var file = NativeFileService.showSaveDialog(path == null ? "untitled.txt" : name());
			return file != null && writeTo(file);
		},

		// onclose (the window's close button): returning false keeps the window open.
		canClose: function () {
			return canDiscard();
		},

		quit: function () {
			if (canDiscard()) {
				window.close();
			}
		},

		find: function () {
			var wanted = prompt("Find:", lastSearch);
			if (wanted != null && wanted !== "") {
				lastSearch = wanted;
				this.findNext(true);
			}
		},

		// Selects the next match after the caret, wrapping round to the top.
		findNext: function (fromFind) {
			if (lastSearch === "") {
				this.find();
				return;
			}
			var all = String(text().getText());
			var from = text().getCaretPosition();
			var at = all.indexOf(lastSearch, from);
			if (at < 0) {
				at = all.indexOf(lastSearch);
			}
			if (at < 0) {
				alert("\"" + lastSearch + "\" wasn't found.");
				return;
			}
			text().selectRange(at, at + lastSearch.length);
		},

		jumpTo: function () {
			var answer = prompt("Jump to line:", "1");
			if (answer == null) {
				return;
			}
			var line = parseInt(answer, 10);
			var lines = String(text().getText()).split("\n");
			if (isNaN(line) || line < 1 || line > lines.length) {
				alert("There is no line " + answer + " (the file has " + lines.length + ").");
				return;
			}
			var at = 0;
			for (var i = 0; i < line - 1; i++) {
				at += lines[i].length + 1;
			}
			text().positionCaret(at);
			text().requestFocus();
		},

		setWrap: function (on) {
			text().setAttribute("wrap", on ? "true" : "off");
			text().setWrapText(on);
		},

		setMonospace: function (on) {
			text().setAttribute("style", on ? "font-family: monospace; font-size: 14px;" : "font-family: sans-serif; font-size: 14px;");
		},

		about: function () {
			alert("Leafpad XUL\n\nA plain-text editor in the spirit of Lubuntu's Leafpad, written in XUL and JavaScript and run by myxul on Java 8, JavaFX and Nashorn.");
		}
	};
})();
