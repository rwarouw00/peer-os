// The "skylight" database holds Skylight OS itself: this design document, with
// the desktop as its attachments, and one document per app. Anyone may read it
// (the login page must load before anyone has logged in), but only CouchDB
// administrators may change it: apps are programs that run for every user.
function (newDoc, oldDoc, userCtx, secObj) {
	if (userCtx.roles.indexOf('_admin') < 0) {
		throw({ unauthorized: 'Only CouchDB administrators can change Skylight OS or its apps.' });
	}
	if (newDoc._deleted || newDoc._id.indexOf('_design/') === 0) {
		return;
	}
	// Anything else is an app: its name is its folder's, and it needs a manifest.
	if (!/^[a-z0-9\-]+$/.test(newDoc._id)) {
		throw({ forbidden: 'App names may use only lowercase letters, digits and "-".' });
	}
	if (newDoc.type !== 'app' || !newDoc.manifest || typeof newDoc.manifest.title !== 'string') {
		throw({ forbidden: 'An app document needs "type": "app" and a manifest with a title.' });
	}
}
