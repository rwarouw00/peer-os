// AbiWord XUL: what the shell does. abiword.xul is the window; AbiCoreEngine
// (myxul.service.PageView) holds the document and paints its pages, and
// NativeFileService reads and writes files.
//
// Documents are saved as text: each paragraph a line, headings marked with
// "# " and "## " (so a saved file opens with its headings again).

var FileController = (function () {
	var path = null;
	var modified = false;

	function name() {
		return path == null ? "Untitled" : NativeFileService.getFileName(path);
	}

	function updateTitle() {
		window.setTitle((modified ? "*" : "") + name() + " - AbiWord XUL");
	}

	function canDiscard() {
		return !modified || confirm("\"" + name() + "\" has unsaved changes. Discard them?");
	}

	return {
		start: function () {
			AbiCoreEngine.attach(document.getElementById("document-canvas").node);
			AbiCoreEngine.setOnChange(function () {
				modified = true;
				updateTitle();
				StatusController.update();
			});
			if (window.arguments.length > 0) {
				this.load(NativeFileService.resolve(window.arguments[0]));
			} else {
				AbiCoreEngine.setText("# Welcome to AbiWord XUL\n" +
					"Click the page and type. Enter starts a new paragraph; Backspace removes the last character.\n" +
					"## What it is\n" +
					"The shell of a word processor, written in XUL and JavaScript and run by myxul: toolbars, " +
					"a page view, a sidebar with the document's headings, and a status bar.");
				modified = false;
				updateTitle();
			}
			StatusController.update();
			document.getElementById("document-canvas").focus();
		},

		load: function (file) {
			try {
				AbiCoreEngine.setText(NativeFileService.readFile(file));
				path = file;
				modified = false;
				updateTitle();
				StatusController.update();
			} catch (e) {
				alert("Can't open " + file + ": " + e.message);
			}
		},

		newDoc: function () {
			if (canDiscard()) {
				AbiCoreEngine.clear();
				path = null;
				modified = false;
				updateTitle();
				StatusController.update();
			}
		},

		openDoc: function () {
			if (canDiscard()) {
				var file = NativeFileService.showOpenDialog();
				if (file != null) {
					this.load(file);
				}
			}
		},

		saveDoc: function () {
			var file = path != null ? path : NativeFileService.showSaveDialog("untitled.txt");
			if (file == null) {
				return false;
			}
			try {
				NativeFileService.writeFile(file, AbiCoreEngine.getText());
				path = file;
				modified = false;
				updateTitle();
				return true;
			} catch (e) {
				alert("Can't save " + file + ": " + e.message);
				return false;
			}
		},

		canClose: function () {
			return canDiscard();
		},

		isModified: function () {
			return modified;
		}
	};
})();

var FormatController = {
	toggleBold: function () {
		// The toolbar button keeps its own checked state; Ctrl+B has to set it too.
		var on = !AbiCoreEngine.isBold();
		document.getElementById("cmd-bold").setAttribute("checked", on);
		AbiCoreEngine.applyTextAttribute("font-weight", on ? "bold" : "normal");
	},

	toggleItalic: function () {
		var on = !AbiCoreEngine.isItalic();
		document.getElementById("cmd-italic").setAttribute("checked", on);
		AbiCoreEngine.applyTextAttribute("font-style", on ? "italic" : "normal");
	},

	setSize: function (size) {
		AbiCoreEngine.applyTextAttribute("font-size", size);
	},

	toggleSidebar: function (show) {
		document.getElementById("sidebar").collapsed = !show;
	},

	styleChosen: function (list) {
		var index = list.selectedIndex;
		if (index >= 0) {
			AbiCoreEngine.setParagraphStyle(["Normal", "Heading 1", "Heading 2"][index]);
		}
	}
};

var StatusController = {
	update: function () {
		var pages = AbiCoreEngine.getPageCount();
		document.getElementById("status-page").label = "Page " + pages + " of " + pages;
		var words = AbiCoreEngine.getWordCount();
		document.getElementById("status-words").label = words + (words == 1 ? " word" : " words");
		document.getElementById("status-style").label = AbiCoreEngine.getParagraphStyle();

		var tree = document.getElementById("outline-tree");
		tree.clearItems();
		var headings = AbiCoreEngine.getHeadings();
		for (var i = 0; i < headings.size(); i++) {
			tree.appendItem(headings.get(i));
		}
	}
};
