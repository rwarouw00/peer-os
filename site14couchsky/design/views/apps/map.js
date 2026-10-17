// The home menu: every app document with a "menu" number in its manifest,
// in that order. Apps without one (yet) can still be opened by path.
function (doc) {
	if (doc.type === 'app' && doc.manifest && typeof doc.manifest.menu === 'number') {
		emit(doc.manifest.menu, doc.manifest.title);
	}
}
