# Security

Skylight OS site3 was restructured to fix the security problems found in the original code (listed in [`../wiki2/Known-Issues.md`](../wiki2/Known-Issues.md#security)). This page describes what is protected now, and what still isn't.

## What is protected

| Threat | Protection | Where |
| --- | --- | --- |
| Downloading passwords or user data | `data/` and `lib/` are outside the web root | Folder layout |
| Cracking stolen passwords | bcrypt hashes with a random salt | `hash_password()` |
| Guessing or stealing a login | PHP sessions: random IDs, `HttpOnly` cookie, new ID at each login | `start_session()`, `login.php` |
| Writing files outside your own folder | File names can't contain `/` or start with `.`; only `data/users/<you>/files/` is writable | `user_file()` |
| Writing code onto the server | Nothing an app saves ends up under `public/` | Folder layout |
| Another website making your browser change things | Changes need `POST` with the `X-Requested-With: Skylight` header | `require_post()` |
| Bad settings values | Every setting is checked against a rule; unknown names are refused | `clean_setting()` |
| A web page (e.g. in the Web Browser) controlling the desktop | Messages are only accepted from the site's origin **and** from the desktop's own windows | `Desktop.onMessage` |
| Text that contains HTML or script | Notifications, dialogs and titles are shown with `textContent` | `desktop.js`, `dialog/index.html` |
| Code passed as text | Dialog callbacks are real functions kept inside the app; context menus are built in code | `app.js`, `desktop.js` |

## What isn't

* **Apps are trusted.** Apps are pages from the same site as the desktop, so the browser lets any app reach into the desktop page directly. Only install apps you trust, as you would on any computer. The message checks keep *outside* pages out, not apps.
* **The Terminal runs any JavaScript**, by design. Remove it from `apps.json` on systems where users shouldn't have it.
* **No limit on login attempts.** Add rate limiting in `login.php` or at the web server before exposing it to the internet.
* **Use HTTPS.** Passwords and the session cookie travel as sent. On HTTPS, also set PHP's `session.cookie_secure` to `1`.
* **Anyone with server access can read `data/`.** Files are stored unencrypted.
