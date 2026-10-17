// The app store's only rule: apps are programs that run on every desktop that
// uses this store, so only CouchDB administrators may add or change them.
// Anyone may read them (the desktop downloads apps without logging in).
function (newDoc, oldDoc, userCtx, secObj) {
	if (userCtx.roles.indexOf('_admin') < 0) {
		throw({ unauthorized: 'Only CouchDB administrators can change the app store.' });
	}
}
