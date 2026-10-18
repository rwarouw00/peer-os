// Added to CouchDB's _users database by tools/push. CouchDB normally lets
// anyone create an account ("sign up"); with this, only CouchDB administrators
// can, using tools/add-user. Users can still change their own password, but
// can't attach files to their account (an HTML attachment would be served from
// CouchDB's own address, where it could act for an administrator who opened it).
function (newDoc, oldDoc, userCtx, secObj) {
	if (userCtx.roles.indexOf('_admin') >= 0) {
		return;
	}
	if (!oldDoc) {
		throw({ forbidden: 'Only CouchDB administrators can create Skylight OS users.' });
	}
	if (newDoc._attachments) {
		throw({ forbidden: 'User documents have no attachments.' });
	}
}
