// The "skylight" database holds Skylight OS itself: this design document and
// its attachments. Anyone may read it (the login page must load before anyone
// has logged in), but only CouchDB administrators may change it.
function (newDoc, oldDoc, userCtx, secObj) {
	if (userCtx.roles.indexOf('_admin') < 0) {
		throw({ unauthorized: 'Only CouchDB administrators can change Skylight OS itself.' });
	}
}
