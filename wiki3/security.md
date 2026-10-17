# Security

Skylight OS Arctic Eagle was built as a prototype and has no real security boundary. **Don't run it on a server other people can reach** without fixing at least the first three items below. The findings come from reading the code; the ones marked *tested* were confirmed in a browser.

## The model

* **Apps are trusted completely.** They are pages on the same site as the desktop, so the browser gives them full access to it through `parent`, and to each other. The message-based [App API](app-api.md) is a convenience, not a barrier.
* **The browser holds the login.** After `auth.php` approves a login, the username lives in `localStorage` and in each app's URL. Nothing the browser sends later proves who the user is.
* **The server is the only place rules can be enforced.** Any limit on what a user or app may do (which files they can write, for example) must be checked in the PHP scripts.

## Findings

### 1. Outside websites can run code on the desktop (*tested*)

The desktop accepts app messages from any window and doesn't check `event.origin`. It also inserts notification and dialog text as HTML. A page from another website, loaded in the Web Browser app, can run:

```javascript
top.postMessage(JSON.stringify({
	eventType: 'postNotification', header: 'News',
	body: '<img src=x onerror="/* any code */">'
}), '*');
```

and its code runs on the desktop as the logged-in user. The same works with `openDialog` (code runs if the user clicks OK).

**Fix:** in `System.handleAppEvent`, return early unless `event.origin === location.origin`; insert notification, toast and dialog text with `textContent`. Consider loading the Web Browser's pages with `<iframe sandbox>`.

### 2. Password data is public

`system/settings/users.xml` stores passwords in plain text and can be downloaded by anyone, as can every user's settings file. **Fix:** keep credentials outside the web root and store password hashes (PHP's `password_hash` and `password_verify`).

### 3. Saving files isn't tied to a login

`saveFile.php` receives a file path and a username from the browser. Unless it checks a server session, anyone can call it directly and write any file the web server can, including the PHP scripts themselves. The script isn't in `siteroot`, so this can't be confirmed. **Fix:** start a session in `auth.php`; in `saveFile.php`, require it and restrict paths to an allowed folder.

### 4. The Save File dialog lists any folder

`system/components/save dialog/index.php` lists the folder named in its `path` query value, with no restriction, so it can browse the whole server. **Fix:** resolve the path with `realpath` and refuse anything outside the user's folder.

### 5. Code stored as text

Dialog OK actions (`eval`) and context menu items (`onclick` attributes) run code from strings. This is how the features are designed, but it turns any text injection into code execution, as in finding 1.

### 6. Credentials and data are sent unencoded

The login form and `saveFile` don't URL-encode values, and logins are sent in a plain form post. Serve the site over HTTPS, and encode values with `encodeURIComponent`.

## Checklist before deploying

- [ ] Origin check and `textContent` in the desktop (finding 1)
- [ ] Credentials hashed and outside the web root (2)
- [ ] Server sessions checked by `saveFile.php`, with path restrictions (3)
- [ ] Save File dialog restricted, or removed from `apps.xml` (4)
- [ ] HTTPS
