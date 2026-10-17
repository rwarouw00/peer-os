# Security

Skylight OS Arctic Eagle was built as a prototype and has no real security boundary. **Don't run it on a server other people can reach.** Findings 1 to 3 let a stranger take over the server or the desktop. *Tested* means the attack was carried out against a local copy.

## The model

* **Apps are trusted completely.** They are pages on the same site as the desktop, so the browser gives them full access to it through `parent`, and to each other. The message-based [App API](app-api.md) is a convenience, not a barrier.
* **A login is a cookie plus a line in a public file.** `auth.php` gives the browser a token cookie and writes the same token to `system/LoggedInUsers.xml`; `saveFile.php` compares the two. See [Server API](server-api.md#login-flow).
* **All data lives inside the website.** Accounts, tokens, settings and shortcuts are XML files the web server will serve to anyone.

## Findings

### 1. Anyone can write files, and run code, on the server (*tested*)

`saveFile.php` writes a file when the token stored for `user` `==` the cookie. For a user who isn't logged in, with no cookie sent, both are `null`, and PHP's `==` treats them as equal. `file` isn't checked either. One request is enough to create a PHP file and run it:

```
curl -d 'user=nobody' -d 'file=system/x.php' --data-urlencode 'contents=<?php phpinfo(); ?>' http://server/system/server/saveFile.php
curl http://server/system/x.php
```

**Fix** (works on PHP 5):

```php
$token = isset($_COOKIE['Skylight_OS_Auth']) ? $_COOKIE['Skylight_OS_Auth'] : '';
$node  = $dom->getElementsByTagName($username)->item(0);
if ($token === '' || $node === null || $node->nodeValue !== $token) { /* refuse */ }

$root = realpath('../..');
$path = realpath(dirname('../../' . $filename));
if ($path === false || strpos($path . '/', $root . '/') !== 0
		|| preg_match('/\.(php|phtml|phar)|htaccess/i', $filename)) { /* refuse */ }
```

The path check was tested: it allows `system/settings/admin.xml` and refuses `../outside.txt`, `system/../../x`, `system/x.php` and `system/.htaccess`. Ideally, also limit each user to their own folder.

### 2. Login tokens are public and guessable (*tested*)

`system/LoggedInUsers.xml` holds every active token and can be downloaded by anyone; copying a token into a `Skylight_OS_Auth` cookie is enough to save files as that user. The tokens are also `sha1(time())`, so knowing roughly when someone logged in is enough to compute theirs, and the cookie is readable by JavaScript.

**Fix:** use PHP sessions (`session_start()`, `$_SESSION['user']`) instead of the XML file and custom cookie. They are random, stored outside the website, and the session cookie can be set `HttpOnly`.

### 3. Outside websites can run code on the desktop (*tested*)

The desktop accepts app messages from any window and doesn't check `event.origin`. It also inserts notification and dialog text as HTML. A page from another website, loaded in the Web Browser app, can run:

```javascript
top.postMessage(JSON.stringify({
	eventType: 'postNotification', header: 'News',
	body: '<img src=x onerror="/* any code */">'
}), '*');
```

and its code runs on the desktop as the logged-in user. The same works with `openDialog` (code runs if the user clicks OK).

**Fix:** in `System.handleAppEvent`, return early unless `event.origin === location.origin`; insert notification, toast and dialog text with `textContent`. Consider loading the Web Browser's pages with `<iframe sandbox>`.

### 4. Passwords are plain text and public

`system/settings/users.xml` stores passwords unencrypted and anyone can download it. `auth.php` compares them with `==`, so some different passwords match (`1e1` and `10`). The `admin` account ships with an empty password.

**Fix:** store hashes (`crypt()` with a Blowfish salt on PHP 5.3+, `password_hash()`/`password_verify()` on 5.5+), compare with `===`, move the file out of the website, and set an admin password.

### 5. Shortcuts need no login

`shortcuts.php` writes to a shared file for anyone who calls it. Low impact on its own, but it lets anyone grow the file without limit.

### 6. The Save File dialog lists any folder

`system/components/save dialog/index.php` lists the folder named in its `path` query value with no restriction. **Fix:** check the path with `realpath` as in finding 1.

### 7. Code stored as text

Dialog OK actions (`eval`) and context menu items (`onclick` attributes) run code from strings. This is how the features are designed, but it turns any text injection into code execution, as in finding 3.

### 8. Credentials and data travel in the clear

Logins are plain form posts and the cookie isn't marked `Secure`. Serve the site over HTTPS.

## Checklist before deploying

- [ ] `saveFile.php`: strict token check and path restriction (1)
- [ ] PHP sessions instead of `LoggedInUsers.xml` (2)
- [ ] Origin check and `textContent` in the desktop (3)
- [ ] Hashed passwords outside the website; admin password set (4)
- [ ] Login required for `shortcuts.php` (5)
- [ ] Save File dialog restricted, or removed from `apps.xml` (6)
- [ ] HTTPS
