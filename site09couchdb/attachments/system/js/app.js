/*
 * The App API, for pages running inside a Skylight OS window.
 * Plain ES5. Load system/js/core.js first.
 */
var App = (function () {
	var args = Skylight.parseQuery(location.search);
	var dialogCallbacks = {};
	var nextDialogId = 1;

	// Every request to the desktop is a JSON message to the parent page.
	function send(eventType, data) {
		data = data || {};
		data.eventType = eventType;
		parent.postMessage(JSON.stringify(data), Skylight.origin);
	}

	// The desktop tells us when the user clicks OK in one of our dialogs.
	window.addEventListener('message', function (event) {
		if (event.origin !== Skylight.origin || event.source !== parent) {
			return;
		}
		var message;
		try {
			message = JSON.parse(event.data);
		} catch (error) {
			return;
		}
		if (message.eventType === 'dialogConfirmed' && dialogCallbacks[message.dialogId]) {
			dialogCallbacks[message.dialogId]();
		}
		delete dialogCallbacks[message.dialogId];
	}, false);

	var App = {
		// Windows
		openWindow: function (path) {
			send('openWindow', { path: path });
		},

		closeWindow: function (appID) {
			send('closeWindow', { appID: appID || args['app-id'] });
		},

		// Dialogs and notifications
		openDialog: function (message, onOK) {
			var dialogId = nextDialogId++;
			if (typeof onOK === 'function') {
				dialogCallbacks[dialogId] = onOK;
			}
			send('openDialog', { message: String(message), dialogId: dialogId });
		},

		postNotification: function (header, body) {
			send('postNotification', { header: String(header), body: body ? String(body) : '' });
		},

		// Desktop and settings
		getSettings: function () {
			var reply = Skylight.get('settings');
			return reply.ok ? reply.settings : null;
		},

		saveSettings: function (changes) {
			send('saveSettings', { changes: changes });
		},

		setBackground: function (path) {
			App.saveSettings({ background: path });
		},

		addShortcut: function (path) {
			send('addShortcut', { path: path });
		},

		// Identity and navigation
		getAppID: function () {
			return args['app-id'];
		},

		getUsername: function () {
			return args.username;
		},

		getQueryStringArgs: function () {
			return Skylight.parseQuery(location.search);
		},

		gotoLocation: function (path, queryString) {
			var fields = App.getQueryStringArgs();
			var extra = Skylight.parseQuery(queryString);
			for (var name in extra) {
				if (extra.hasOwnProperty(name)) {
					fields[name] = extra[name];
				}
			}
			location.assign(path + '?' + Skylight.toQuery(fields));
		},

		// Files: site files by URL, and the user's own files by name
		openFile: function (url) {
			return Skylight.getText(url);
		},

		listFiles: function () {
			var reply = Skylight.get('files');
			return reply.ok ? reply.files : null;
		},

		readFile: function (name) {
			var reply = Skylight.get('files', { name: name });
			return reply.ok ? reply.contents : null;
		},

		writeFile: function (name, contents) {
			return Skylight.post('files', { name: name, contents: contents });
		},

		parseXml: Skylight.parseXml,
		serializeXml: Skylight.serializeXml
	};

	// The name earlier releases used for writeFile.
	App.saveFile = App.writeFile;

	return App;
}());
