// Added to CouchDB's _users database by tools/push. Only CouchDB administrators
// may create or change accounts (with tools/add-user).
//
// CouchDB 1.6 needs this more than later versions: by default anyone may sign
// up, and CouchDB 1.6.1 has a bug (CVE-2017-12635, fixed in 1.7.0) that lets
// anyone who can write a user document make themselves a server administrator,
// by giving "roles" twice. Refusing every write from non-administrators closes
// that hole, and also stops users attaching files to their account. The price:
// users can't change their own password; an administrator does it with
// tools/add-user.
function (newDoc, oldDoc, userCtx, secObj) {
	if (userCtx.roles.indexOf('_admin') < 0) {
		throw({ forbidden: 'Only CouchDB administrators can create or change Skylight OS users.' });
	}
}
