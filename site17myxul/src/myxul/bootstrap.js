// myxul's script bootstrap: run first in every window's Nashorn engine.
// It defines what XUL scripts expect to find: document, window, alert,
// confirm, prompt and setTimeout, and the wrappers that make elements
// behave like XUL elements.
//
// __window and __document are the Java objects (XulWindow, XulDocument).

var __wrap = (function () {
	var doc = __document;

	function toScript(value) {
		if (value == null) {
			return value;
		}
		if (doc.isElement(value)) {
			return __wrap(value);
		}
		if (doc.isElementList(value)) {
			var out = [];
			for (var i = 0; i < value.size(); i++) {
				out.push(__wrap(value.get(i)));
			}
			return out;
		}
		return value;
	}

	// A Java list of elements as a JS array, even when it's empty.
	function toArray(list) {
		var out = [];
		for (var i = 0; i < list.size(); i++) {
			out.push(__wrap(list.get(i)));
		}
		return out;
	}

	// An element's wrapper, passed to a Java method, becomes the element itself.
	function toJava(value) {
		if (value != null && typeof value === "object" && value.__element !== undefined) {
			return value.__element;
		}
		return value;
	}

	// Calls target[name](...) with the arguments given. (Java methods have no .apply.)
	function invoke(target, name, args) {
		var a = [];
		for (var i = 0; i < args.length; i++) {
			a.push(toJava(args[i]));
		}
		switch (a.length) {
		case 0: return target[name]();
		case 1: return target[name](a[0]);
		case 2: return target[name](a[0], a[1]);
		case 3: return target[name](a[0], a[1], a[2]);
		case 4: return target[name](a[0], a[1], a[2], a[3]);
		case 5: return target[name](a[0], a[1], a[2], a[3], a[4]);
		case 6: return target[name](a[0], a[1], a[2], a[3], a[4], a[5]);
		default: throw new Error(name + "(): myxul passes at most 6 arguments to a Java method");
		}
	}

	// An element as scripts see it. XUL's own properties and methods (value,
	// checked, getAttribute, appendItem, ...) are handled by the element;
	// anything else goes to its JavaFX object, so el.setText("x") and
	// el.cut() work on a <textbox>.
	function wrapper(el) {
		return new JSAdapter({
			__get__: function (name) {
				if (name === "toString") {
					return function () { return String(el.toString()); };
				}
				if (name === "__element") {
					return el;
				}
				if (name === "childNodes") {
					return toArray(el.getProperty(name));
				}
				if (el.isProperty(name)) {
					return toScript(el.getProperty(name));
				}
				if (name === "getElementsByTagName") {
					return function (tag) { return toArray(el.getElementsByTagName(String(tag))); };
				}
				if (el.isMethod(name)) {
					return function () { return toScript(invoke(el, name, arguments)); };
				}
				var node = el.getNode();
				if (node == null) {
					return undefined;
				}
				return node[name];
			},
			__put__: function (name, value) {
				if (el.isProperty(name)) {
					el.setProperty(name, value);
				} else {
					var node = el.getNode();
					if (node == null) {
						throw new TypeError("<" + el.getTagName() + "> has no " + name + " to set");
					}
					node[name] = value;
				}
			},
			__has__: function (name) {
				return el.isProperty(name) || el.isMethod(name);
			},
			__call__: function (name) {
				var args = Array.prototype.slice.call(arguments, 1);
				if (name === "toString") {
					return String(el.toString());
				}
				if (name === "getElementsByTagName") {
					return toArray(el.getElementsByTagName(String(args[0])));
				}
				if (el.isMethod(name)) {
					return toScript(invoke(el, name, args));
				}
				var node = el.getNode();
				if (node == null || node[name] === undefined) {
					throw new TypeError("<" + el.getTagName() + "> has no method " + name + "()");
				}
				return toScript(invoke(node, name, args));
			}
		});
	}

	return function (el) {
		if (el == null) {
			return null;
		}
		var cached = doc.wrapperOf(el);
		if (cached != null) {
			return cached;
		}
		var w = wrapper(el);
		doc.remember(el, w);
		return w;
	};
})();

var document = {
	getElementById: function (id) {
		return __wrap(__document.getElementById(String(id)));
	},
	getElementsByTagName: function (tag) {
		var list = __document.getElementsByTagName(String(tag));
		var out = [];
		for (var i = 0; i < list.size(); i++) {
			out.push(__wrap(list.get(i)));
		}
		return out;
	},
	get documentElement() {
		return __wrap(__document.getDocumentElement());
	},
	// A new element, shown once it's appended to one in the window: document.createElement("button").
	createElement: function (tag) {
		return __wrap(__document.createElement(String(tag)));
	},
	// The element a context menu was opened on.
	get popupNode() {
		return __wrap(__document.getPopupNode());
	},
	get title() {
		return String(__window.getTitle());
	},
	set title(value) {
		__window.setTitle(String(value));
	}
};

var window = {
	document: document,
	arguments: (function () {
		var list = __window.getArguments();
		var out = [];
		for (var i = 0; i < list.size(); i++) {
			out.push(String(list.get(i)));
		}
		return out;
	})(),
	get title() {
		return String(__window.getTitle());
	},
	set title(value) {
		__window.setTitle(String(value));
	},
	setTitle: function (value) {
		__window.setTitle(String(value));
	},
	getTitle: function () {
		return String(__window.getTitle());
	},
	// Closes the window without running its onclose.
	close: function () {
		__window.close();
	},
	// Opens another XUL file, relative to this one. Extra arguments become the new window's window.arguments.
	open: function (path) {
		var extra = [];
		for (var i = 1; i < arguments.length; i++) {
			extra.push(String(arguments[i]));
		}
		__window.open(String(path), extra.join("\u0000"));
	},
	alert: function (message) {
		__window.alert(String(message));
	},
	confirm: function (message) {
		return __window.confirm(String(message));
	},
	prompt: function (message, value) {
		var answer = __window.prompt(String(message), value == null ? "" : String(value));
		return answer == null ? null : String(answer);
	},
	setTimeout: function (f, ms) {
		return __window.setTimeout(f, ms == null ? 0 : Number(ms), false);
	},
	setInterval: function (f, ms) {
		return __window.setTimeout(f, ms == null ? 0 : Number(ms), true);
	},
	clearTimeout: function (id) {
		__window.clearTimeout(id);
	},
	clearInterval: function (id) {
		__window.clearTimeout(id);
	}
};

function alert(message) { window.alert(message); }
function confirm(message) { return window.confirm(message); }
function prompt(message, value) { return window.prompt(message, value); }
function setTimeout(f, ms) { return window.setTimeout(f, ms); }
function setInterval(f, ms) { return window.setInterval(f, ms); }
function clearTimeout(id) { window.clearTimeout(id); }
function clearInterval(id) { window.clearInterval(id); }
