# Desktop internals

The desktop and the apps are the PHP version's, so its [desktop internals](../wiki3/desktop-internals.md) page applies as written: the `Desktop` object, its state, how events are handled, and how to extend it. This page covers what's different.

## What changed from the PHP version

| File | Change |
| --- | --- |
| [`system/js/core.js`](../site9couchdb/attachments/system/js/core.js) | `Skylight.get()` and `Skylight.post()` no longer call `api/*.php`; they call CouchDB (below) |
| [`system/js/desktop.js`](../site9couchdb/attachments/system/js/desktop.js) | App windows open `<path>index.html` rather than `<path>`, because CouchDB has no folder index pages |
| `api/`, `lib/`, `data/`, `tools/add-user.php` | Gone: CouchDB does their work |

Nothing else changed: `app.js`, the apps, the dialog, the styles and the App API are the same files.

## core.js

`core.js` keeps the PHP version's interface, so the desktop and `app.js` didn't need to change:

```javascript
Skylight.post('login', { username: 'alice', password: '...' });   // { ok, user, settings } or { ok: false, error }
Skylight.get('settings');                                          // { ok, user, settings }
Skylight.post('settings', { changes: '{"notificationTime":3000}' });
Skylight.get('files');                                             // { ok, files: [names] }
Skylight.get('files', { name: 'notes.txt' });                      // { ok, contents }
Skylight.post('files', { name: 'notes.txt', contents: '...' });     // { ok } or { ok: false, error }
Skylight.post('logout');
```

Behind each call is a small function in the `api` table, named after the PHP endpoint (`'POST login'`, `'GET files'` and so on), which makes the [CouchDB requests listed in the architecture page](architecture.md#requests-to-couchdb). The helpers:

| Function | Does |
| --- | --- |
| `couch(method, path, data)` | A synchronous JSON request; returns `{ status, data }` |
| `couchError(reply)` | CouchDB's `reason`, as a sentence for the user |
| `userDb(user)` | `/userdb-<hex>/`, the user's database |
| `currentUser()` | The logged-in name from `GET /_session`, or `null` |
| `readDoc(user, id)`, `writeDoc(user, doc)` | Read a document; write one, with its current `_rev`, retrying on conflicts |
| `loadSettings(user)` | The `settings` document merged over the defaults |
| `cleanSetting(name, value)` | The same checks the PHP version made, before anything is sent |

The checks run twice: in `core.js`, so the user gets a clear message without a round trip, and in the database's `validate_doc_update`, so nothing else (another program, or someone using CouchDB's API by hand) can store bad data.

## Adding a new kind of data

To store something new for each user, say a list of favourite apps:

1. In `core.js`, add an entry to `api` (for example `'GET favourites'`) that reads or writes a document such as `favourites` with `readDoc` and `writeDoc`.
2. In `userdb/validate_doc_update.js`, accept that `_id` and check its fields; the function refuses unknown documents.
3. Run `tools/push.sh`. New users get the new checks from `tools/add-user`; for existing users, put the new function into each `userdb-…/_design/skylight` as an administrator (in Fauxton, or by running `tools/add-user` again, which also resets their password).
