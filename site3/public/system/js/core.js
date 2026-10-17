/*
 * Skylight OS core: helpers shared by the desktop and by apps.
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

	// Requests are synchronous, as they always were in Skylight OS.
	function request(method, url, fields) {
		var xhr = new XMLHttpRequest();
		var body = fields ? toQuery(fields) : null;
		if (method === 'GET' && body) {
			url += (url.indexOf('?') < 0 ? '?' : '&') + body;
			body = null;
		}
		xhr.open(method, url, false);
		xhr.setRequestHeader('X-Requested-With', 'Skylight');
		if (body !== null) {
			xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		}
		xhr.send(body);
		return xhr;
	}

	// Calls public/api/<name>.php. Always returns an object with an `ok` field.
	function callApi(method, name, fields) {
		var xhr = request(method, root + 'api/' + name + '.php', fields);
		try {
			return JSON.parse(xhr.responseText);
		} catch (error) {
			return { ok: false, error: 'The server sent an unreadable reply (HTTP ' + xhr.status + ').' };
		}
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
			var xhr = request('GET', url);
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
