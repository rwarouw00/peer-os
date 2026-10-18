# Restructure proposal

> **Status: implemented** in [`../site03`](../site03). This page records why the original Arctic Eagle code ([`../siteroot`](../siteroot)) was restructured, the rules the rewrite followed, and how the old structure maps to the new one.

## Constraints

* **Same language versions.** Browser code stays ES5 (no `let`, `const`, arrow functions, classes, Promises or modules). PHP stays 5-compatible (no short arrays, `??` or other 5.4+ features), with 5.3.7 as the minimum for bcrypt.
* **Same features and app API names**, so the desktop looks and works the same and existing apps need only small changes ([Migrating apps](migrating-apps.md)).
* **No libraries or build step.** Files are still served as they are.

## Problems in the original structure

| Problem | Examples | Effect |
| --- | --- | --- |
| The same code in several places | `parseXml`/`serializeXml` (43 identical lines in `system.js` and `app.js`); `openFile`/`saveFile` twice; two stale copies of `app.js` | Fixes made in one copy, not the others |
| No single place for requests | 7 hand-built `XMLHttpRequest`s, none encoding their values | Data with `&`, `+` or `%` corrupted; the same bug in every caller |
| Boilerplate | 7 near-identical 8-line `postMessage` wrappers in `app.js` | Long file, easy to get one wrong |
| Logic mixed into markup | 334 lines of script inside `index.html` | Hard to find and test |
| Long if-chains | Clicks sorted by 17 comparisons on `className` and `id`; messages by an `if`/`else if` chain | Each new feature touches a fragile chain |
| Hard-coded relative paths | `../../../system/server/saveFile.php` | Apps had to sit exactly three folders deep; the desktop only worked at the site root |
| Misleading names | `windowActionButtonMinimize` maximizes; `App.closeWindow(title)` takes an AppID; `toastAnimState0/1`; icon sizes `small`…`Xlarge` | Readers guess wrong |
| Inconsistent names | `saveFile.php`, `LoggedInUsers.xml`, `mime-types.xml`; folders with spaces (`web browser`, `save dialog`, `context menus`) | `%20` in URLs, guesswork |
| Data inside the website | `users.xml`, `LoggedInUsers.xml`, settings | Passwords and login tokens publicly downloadable |
| No shared server code | Each PHP script loads and saves XML its own way; JSON written by hand | Inconsistent checks; the `null == null` login bypass |
| Duplicated CSS | Toast states: two 20-line rules differing in one line; hover rules repeating every property | Large stylesheet for little |
| Dead code | `jse_*.js`, `login.wcf`, IE-only fallbacks, panels, commented-out blocks, `shortcuts.json` | About 400 lines to read past |

## Principles

1. **One home for each job.** Shared helpers in one file, loaded by everyone.
2. **Data in, data out.** Pass data, never code-as-text: no `eval` outside the Terminal, no `onclick` strings, no HTML from messages.
3. **Look things up instead of branching.** Tables of actions and messages, keyed by `data-action` and `eventType`.
4. **Say what it is.** Names describe what things do; one naming style per language.
5. **Keep the web root public and nothing else.** Code and data that aren't meant to be downloaded live outside it.
6. **Delete what doesn't work.** Unfinished features are removed rather than carried along; they can come back when finished.

## The new structure

| Original | site03 | Why |
| --- | --- | --- |
| Web root = whole project | `public/` is the web root; `lib/`, `data/`, `tools/` beside it | Data and server code can't be downloaded |
| `index.html` (410 lines, mostly script) | `index.html` (markup only) + `system/js/desktop.js` | Separate structure from behaviour |
| `System` object in `system.js` | `Desktop` object in `desktop.js` | It's the desktop, not the operating system |
| Helpers copied into `system.js` and `app.js` | `system/js/core.js` (`Skylight` object), loaded by every page | One copy; values always encoded; site root worked out once |
| 7 `postMessage` wrappers | One private `send(type, data)` in `app.js` | Each API function is one line |
| `if` chains for clicks and messages | `Desktop.actions` and `Desktop.messages` tables | Add a feature by adding an entry |
| Dialog script passed as a string through `localStorage` | Callback kept in the app; desktop relays `dialogConfirmed` | No `eval`; callbacks see the app's variables |
| Context menus as XML with code inside | One menu built in `onContextMenu` | No code in data files |
| `system/server/*.php`, each standalone | `public/api/*.php` (4–33 lines each) + `lib/skylight.php` | Shared reply, input, session and file helpers |
| `LoggedInUsers.xml` + `sha1(time())` cookie | PHP sessions | Random, server-side, `HttpOnly`, works on any port |
| `users.xml`, plain-text passwords | `data/users.json`, bcrypt; `tools/add-user.php` | Hashed, not downloadable |
| `system/settings/<user>.xml`, saved through a general file writer | `data/users/<user>/settings.json` through `api/settings.php`, each value checked | No general file writer at all |
| `App.saveFile` to any path | `App.writeFile` / `readFile` / `listFiles` in `data/users/<user>/files/` | Private per-user storage |
| `shortcuts.xml` shared by everyone, never read back | `shortcuts` in each user's settings, restored at login | Shortcuts persist per user |
| `system/apps.xml` | `system/apps.json` | JSON everywhere; no XML parsing for config |
| `appData.json`, icons `small`…`Xlarge`, site-root paths | `app.json`, icons `16`…`64`, paths relative to the app | Clear names; apps can move |
| `apps/default/web browser/`, `system/components/dialog/` | `apps/web-browser/`, `system/dialog/` | No spaces, shorter |
| `windowActionButtonMinimize`, `toastAnimState0/1`, `programBox` | `window-maximize`, `toast` + `visible`, `menu` | Names match behaviour; state as classes |
| Inline sizes recomputed in script on maximize | `.window.maximized` and `calc()` in CSS | Less code |

## What was removed

| Removed | Reason |
| --- | --- |
| `jse_form.js`, `jse_search.js` | Unused third-party search script |
| `login.wcf` | Unused prototype |
| Stale copies of `app.js` in the Terminal and Dialog | Never loaded |
| IE `ActiveXObject` and DOM Level 3 fallbacks | Unreachable: any browser with `addEventListener` has `DOMParser` |
| Save File dialog, `functions.php`, MIME tables and icons | Unfinished; couldn't return a file to an app; listed any folder on the server |
| Add User page, Save Script button, Pin/Unpin, Remove, Delete App | Did nothing, or called functions that didn't exist |
| Panels | Commented out |
| Unused images (`home2`, `min_button`, SVG copies, throbber) | Not referenced |

## Bugs fixed along the way

All the issues listed in [`../wiki2/Known-Issues.md`](../wiki2/Known-Issues.md) that still apply to the kept features are fixed, including: unauthenticated file writes, public login tokens, the cookie that broke saving on non-standard ports, second logins being unable to save, cross-site messages running code on the desktop, toasts off screen, toasts without a body failing, logout leaving the desktop up, Enter not logging in, the clock at midnight, windows not coming to the front, the `&undefined` query value, shortcut clicks showing an error, and `desktop_4.JPG`.

## Result

| | Arctic Eagle (`siteroot`) | site03 |
| --- | --- | --- |
| Files (excluding READMEs) | 108 | 51 |
| Non-blank lines of code (HTML, JS, CSS, PHP, JSON, XML; excluding `mime.types`) | 2,085 | 1,390 |
| Desktop and API script (`index.html` + `system.js` + `app.js` / `index.html` + `core.js` + `desktop.js` + `app.js`) | 788 | 668 |
| Stylesheets | 479 | 290 |
| Server PHP | 96 | 229 |

The server code grew because it now does real work: sessions, password hashing, input checks, per-user settings and files, and a user tool. Everything else shrank, while gaining features (saved shortcuts, notification time, per-user files, session resume).

Both versions pass an ES5-only syntax check of every script, including those inside HTML pages.
