/*
 * Skylight OS core: helpers shared by the desktop and by apps, and the
 * calls to CouchDB that replace the PHP version's server.
 * Plain ES5. Load it before desktop.js or app.js.
 */
var Skylight = (function () {
	// While this file loads it is the last <script> on the page, so its address
	// tells us where the site root is, wherever the page itself lives.
	var scripts = document.getElementsByTagName('script');
	var root = scripts[scripts.length - 1].src.replace(/system\/js\/core\.js.*$/, '');
	var origin = location.protocol + '//' + location.host;

	function toQuery(fields) {
		var parts = [];
		for (var name in fields) {
			if (fields.hasOwnProperty(name) && fields[name] !== undefined && fields[name] !== null) {
				parts.push(encodeURIComponent(name) + '=' + encodeURIComponent(fields[name]));
			}
		}
		return parts.join('&');
	}

	function parseQuery(query) {
		var fields = {};
		var parts = String(query || '').replace(/^\?/, '').split('&');
		for (var i = 0; i < parts.length; i++) {
			if (parts[i] === '') {
				continue;
			}
			var pair = parts[i].split('=');
			fields[decodeURIComponent(pair[0])] = decodeURIComponent(pair.slice(1).join('='));
		}
		return fields;
	}

	// ---- CouchDB ----
	// Skylight's pages are attachments of a design document; everything else
	// lives in CouchDB itself, at the root of the same server.

	var USERNAME = /^[A-Za-z0-9_-]{1,32}$/;
	var FILE_NAME = /^[A-Za-z0-9_-][A-Za-z0-9_. -]{0,99}$/;
	var DEFAULT_SETTINGS = { background: 'system/images/backgrounds/3.jpg', notificationTime: 7000, shortcuts: [] };

	// Requests are synchronous, as they always were in Skylight OS.
	function request(method, url, body, contentType) {
		var xhr = new XMLHttpRequest();
		xhr.open(method, url, false);
		if (body !== undefined && body !== null) {
			xhr.setRequestHeader('Content-Type', contentType || 'application/json');
		}
		xhr.setRequestHeader('Accept', 'application/json');
		xhr.send(body === undefined ? null : body);
		return xhr;
	}

	// A CouchDB request: { status, data } with the reply parsed as JSON.
	function couch(method, path, data) {
		var xhr = request(method, path, data === undefined ? null : JSON.stringify(data));
		var reply = null;
		try {
			reply = JSON.parse(xhr.responseText);
		} catch (error) {
			// leave it null
		}
		return { status: xhr.status, data: reply };
	}

	function failure(message) {
		return { ok: false, error: message };
	}

	// What CouchDB said went wrong, in a sentence.
	function couchError(reply) {
		if (reply.status === 0) {
			return 'CouchDB could not be reached.';
		}
		if (reply.data && reply.data.reason) {
			return reply.data.reason + (/[.!?]$/.test(reply.data.reason) ? '' : '.');
		}
		return 'CouchDB replied with HTTP ' + reply.status + '.';
	}

	function hex(text) {
		var bytes = unescape(encodeURIComponent(text));   // UTF-8
		var out = '';
		for (var i = 0; i < bytes.length; i++) {
			out += (bytes.charCodeAt(i) < 16 ? '0' : '') + bytes.charCodeAt(i).toString(16);
		}
		return out;
	}

	// couch_peruser gives each user a private database named userdb-<name in hex>.
	function userDb(user) {
		return '/userdb-' + hex(user) + '/';
	}

	function currentUser() {
		var reply = couch('GET', '/_session');
		return reply.status === 200 && reply.data.userCtx ? reply.data.userCtx.name : null;
	}

	// Reads a document, or returns null if there isn't one (or it can't be read).
	function readDoc(user, id) {
		var reply = couch('GET', userDb(user) + encodeURIComponent(id));
		return reply.status === 200 ? reply.data : null;
	}

	// Creates or replaces a document, keeping its revision. Returns null, or an error message.
	function writeDoc(user, doc) {
		for (var attempt = 0; attempt < 3; attempt++) {
			var old = readDoc(user, doc._id);
			if (old) {
				doc._rev = old._rev;
			}
			var reply = couch('PUT', userDb(user) + encodeURIComponent(doc._id), doc);
			if (reply.status === 201 || reply.status === 202) {
				return null;
			}
			if (reply.status !== 409) {   // 409: someone saved in between, so try again
				return couchError(reply);
			}
		}
		return 'The document was being changed elsewhere. Please try again.';
	}

	function loadSettings(user) {
		var settings = {};
		var saved = readDoc(user, 'settings') || {};
		for (var name in DEFAULT_SETTINGS) {
			if (DEFAULT_SETTINGS.hasOwnProperty(name)) {
				settings[name] = saved.hasOwnProperty(name) ? saved[name] : DEFAULT_SETTINGS[name];
			}
		}
		return settings;
	}

	// Checks one setting. Returns its clean value, or undefined if it isn't acceptable.
	// The database's validate_doc_update function checks again on the server.
	function cleanSetting(name, value) {
		if (name === 'background' && typeof value === 'string' && /^[A-Za-z0-9_\/-]+\.(jpg|jpeg|png|gif)$/i.test(value)) {
			return value;
		}
		if (name === 'notificationTime' && typeof value === 'number' && value >= 1000 && value <= 60000) {
			return Math.round(value);
		}
		if (name === 'shortcuts' && Object.prototype.toString.call(value) === '[object Array]' && value.length <= 50) {
			var clean = [];
			for (var i = 0; i < value.length; i++) {
				var shortcut = value[i];
				if (!shortcut || typeof shortcut.path !== 'string' || !/^[A-Za-z0-9_\/-]+\/$/.test(shortcut.path)
						|| typeof shortcut.left !== 'number' || typeof shortcut.top !== 'number') {
					return undefined;
				}
				clean.push({ path: shortcut.path, left: Math.round(shortcut.left), top: Math.round(shortcut.top) });
			}
			return clean;
		}
		return undefined;
	}

	// The same calls as the PHP version's api/*.php, answered by CouchDB.
	var api = {
		'POST login': function (fields) {
			if (!USERNAME.test(fields.username || '')) {
				return failure('Either the username or password is incorrect.');
			}
			var reply = couch('POST', '/_session', { name: fields.username, password: fields.password || '' });
			if (reply.status !== 200) {
				return failure(reply.status === 401 ? 'Either the username or password is incorrect.' : couchError(reply));
			}
			return api['GET settings']();
		},

		'POST logout': function () {
			couch('DELETE', '/_session');
			return { ok: true };
		},

		'GET settings': function () {
			var user = currentUser();
			if (user === null) {
				return failure('You are not logged in.');
			}
			return { ok: true, user: user, settings: loadSettings(user) };
		},

		'POST settings': function (fields) {
			var user = currentUser();
			if (user === null) {
				return failure('You are not logged in.');
			}
			var changes;
			try {
				changes = JSON.parse(fields.changes);
			} catch (error) {
				return failure('Bad request.');
			}
			var settings = loadSettings(user);
			for (var name in changes) {
				if (changes.hasOwnProperty(name)) {
					var clean = cleanSetting(name, changes[name]);
					if (clean === undefined) {
						return failure('Bad value for ' + name + '.');
					}
					settings[name] = clean;
				}
			}
			var error = writeDoc(user, { _id: 'settings', type: 'settings', background: settings.background,
				notificationTime: settings.notificationTime, shortcuts: settings.shortcuts });
			return error ? failure(error) : { ok: true, user: user, settings: settings };
		},

		'GET files': function (fields) {
			var user = currentUser();
			if (user === null) {
				return failure('You are not logged in.');
			}
			if (fields && fields.name !== undefined) {
				var doc = FILE_NAME.test(fields.name) ? readDoc(user, 'file:' + fields.name) : null;
				return doc ? { ok: true, contents: doc.contents } : failure('There is no file called ' + fields.name + '.');
			}
			var reply = couch('GET', userDb(user) + '_all_docs?startkey=' + encodeURIComponent('"file:"') +
				'&endkey=' + encodeURIComponent('"file:\ufff0"'));
			if (reply.status !== 200) {
				return failure(couchError(reply));
			}
			var files = [];
			for (var i = 0; i < reply.data.rows.length; i++) {
				files.push(reply.data.rows[i].id.substring(5));
			}
			return { ok: true, files: files };
		},

		'POST files': function (fields) {
			var user = currentUser();
			if (user === null) {
				return failure('You are not logged in.');
			}
			if (!FILE_NAME.test(fields.name || '')) {
				return failure('File names may use letters, digits, spaces, "-", "_" and "." (up to 100 characters).');
			}
			var contents = String(fields.contents === undefined ? '' : fields.contents);
			if (contents.length > 1048576) {
				return failure('Files are limited to 1 MB.');
			}
			var error = writeDoc(user, { _id: 'file:' + fields.name, type: 'file', name: fields.name, contents: contents });
			return error ? failure(error) : { ok: true };
		}
	};

	function callApi(method, name, fields) {
		var handler = api[method + ' ' + name];
		return handler ? handler(fields || {}) : failure('Unknown request.');
	}

	return {
		root: root,
		origin: origin,
		toQuery: toQuery,
		parseQuery: parseQuery,

		get: function (name, fields) {
			return callApi('GET', name, fields);
		},

		post: function (name, fields) {
			return callApi('POST', name, fields || {});
		},

		// Returns a file's text, or null if it can't be loaded.
		getText: function (url) {
			var xhr = request('GET', url, null);
			return xhr.status === 200 ? xhr.responseText : null;
		},

		getJson: function (url) {
			var text = this.getText(url);
			try {
				return text === null ? null : JSON.parse(text);
			} catch (error) {
				return null;
			}
		},

		// Returns an XML document, or null if the text isn't valid XML.
		parseXml: function (text) {
			var doc = new DOMParser().parseFromString(String(text), 'text/xml');
			return doc.getElementsByTagName('parsererror').length ? null : doc;
		},

		serializeXml: function (doc) {
			return new XMLSerializer().serializeToString(doc);
		}
	};
}());
