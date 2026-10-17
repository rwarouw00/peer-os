# Users and Login

## Logging in

When **Log In** is clicked, the desktop page:

1. Sends the username and password to `system/server/auth.php` (see [Server Endpoints](Server-Endpoints.md#authphp)) and waits for the reply.
2. `auth.php` looks the user up in `system/settings/users.xml`. If the password matches, it creates a login token, sends it to the browser as the `Skylight_OS_Auth` cookie, adds it to `system/LoggedInUsers.xml`, and replies `{"error": "none"}`. Otherwise it replies `{"error": "true"}` and the login screen shows "Either the username or password is incorrect".
3. Remembers the username in the browser's `localStorage` under `Skylight.user`, and in `System.username`.
4. Loads the user's settings from `system/settings/<username>.xml`, sets the background, starts the clock and fills the home menu from `system/apps.xml`.

Every app window then receives the username in its query string, which is where [App.getUsername](App.getUsername.md) reads it. Later saves prove the login by sending the cookie back; see [App.saveFile](App.saveFile.md).

**Use the standard port.** `auth.php` builds the cookie's domain from the full host name including any port, and browsers refuse such cookies. On an address like `http://localhost:8000` you can log in, but nothing can be saved and logout can't update `LoggedInUsers.xml`. Serve Skylight OS on port 80 or 443, or change the domain argument of `setcookie` in `auth.php` to `''`.

**One login per user.** Each login adds a token, but saving only checks the user's oldest one, so a second login (another browser, or logging in again without logging out) can't save.

**Each user needs a settings file.** If `system/settings/<username>.xml` is missing, an XML error dialog appears and the desktop never finishes loading: no background and an empty home menu.

## User settings file

`system/settings/<username>.xml`, for example [`admin.xml`](../siteroot/system/settings/admin.xml):

```
<settings>
	<background src="system/images/desktop/desktop_3.jpg"/>
	<displayNotificationsFor value="7000"/>
</settings>
```

| Element | Attribute | Meaning |
| --- | --- | --- |
| `background` | `src` | Desktop background, relative to the site root. Changed by the Settings app. |
| `displayNotificationsFor` | `value` | How long toasts stay on screen, in milliseconds. The Settings app has a box for this, but it doesn't save anything yet. |

Both elements must be present.

## Logging out

The power button in the home menu calls `System.logoutUser()`, which downloads `system/LoggedInUsers.xml`, removes the user's first token, saves the file through `saveFile.php`, and clears `Skylight.user`. **The desktop stays on screen**: nothing returns to the login screen, so reload the page to log in again. If the user isn't listed, logging out fails with a JavaScript error; if the save is refused (see the port problem above), the token stays in the file. The cookie itself isn't deleted until the browser closes.

## Adding users

There is no working way to add users from inside Skylight OS: the Settings app's **Add User** page has a form, but its **Save** button does nothing. To add a user by hand:

1. Add `<user username="alice" password="secret"/>` to [`system/settings/users.xml`](../siteroot/system/settings/users.xml). The username must be a valid XML element name.
2. Copy `system/settings/admin.xml` to `system/settings/alice.xml`.

Passwords in `users.xml` are stored as plain text and the file can be downloaded by anyone, so don't reuse real passwords. The shipped `admin` account has an **empty password**; set one before going live. (An older, commented-out version of the login in `index.html` checked passwords in the browser against the same file.)

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._

_Updated after the `system/server` scripts were added to `siteroot`._
