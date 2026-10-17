// Checks every document saved in a user's private database (userdb-<hex name>):
// one "settings" document, and one "file:<name>" document per file. The pages
// check the same rules before saving; this makes sure nothing else gets in.
function (newDoc, oldDoc, userCtx, secObj) {
	function forbid(message) {
		throw({ forbidden: message });
	}

	function isArray(value) {
		return Object.prototype.toString.call(value) === '[object Array]';
	}

	if (userCtx.roles.indexOf('_admin') >= 0) {
		return;   // CouchDB administrators may change anything
	}
	var members = (secObj && secObj.members && secObj.members.names) || [];
	if (members.indexOf(userCtx.name) < 0) {
		throw({ unauthorized: 'This is not your database.' });
	}
	if (newDoc._id.indexOf('_design/') === 0) {
		forbid('Only CouchDB administrators can change design documents here.');
	}
	if (newDoc._deleted) {
		return;
	}
	if (newDoc._attachments) {
		// An HTML attachment here would be served from CouchDB's own address,
		// where it could act with the rights of an administrator who opened it.
		forbid('Skylight OS documents have no attachments.');
	}

	if (newDoc._id === 'settings') {
		if (newDoc.type !== 'settings') {
			forbid('The settings document needs type "settings".');
		}
		if (typeof newDoc.background !== 'string' || !/^[A-Za-z0-9_\/-]+\.(jpg|jpeg|png|gif)$/i.test(newDoc.background)) {
			forbid('Bad value for background.');
		}
		if (typeof newDoc.notificationTime !== 'number' || newDoc.notificationTime < 1000 || newDoc.notificationTime > 60000) {
			forbid('Bad value for notificationTime.');
		}
		if (!isArray(newDoc.shortcuts) || newDoc.shortcuts.length > 50) {
			forbid('Bad value for shortcuts.');
		}
		for (var i = 0; i < newDoc.shortcuts.length; i++) {
			var shortcut = newDoc.shortcuts[i];
			if (!shortcut || typeof shortcut.path !== 'string' || !/^[A-Za-z0-9_\/-]+\/$/.test(shortcut.path)
					|| typeof shortcut.left !== 'number' || typeof shortcut.top !== 'number') {
				forbid('Bad shortcut.');
			}
		}
		return;
	}

	if (newDoc._id.indexOf('file:') === 0) {
		var name = newDoc._id.substring(5);
		if (newDoc.type !== 'file' || newDoc.name !== name) {
			forbid('A file document needs type "file" and its name.');
		}
		if (!/^[A-Za-z0-9_-][A-Za-z0-9_. -]{0,99}$/.test(name)) {
			forbid('File names may use letters, digits, spaces, "-", "_" and "." (up to 100 characters).');
		}
		if (typeof newDoc.contents !== 'string' || newDoc.contents.length > 1048576) {
			forbid('Files are limited to 1 MB of text.');
		}
		return;
	}

	forbid('Skylight OS keeps only settings and files here.');
}
