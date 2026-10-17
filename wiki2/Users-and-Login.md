# Users and Login

## Logging in

When **Log In** is clicked, the desktop page:

1. Sends the username and password to `system/server/auth.php` (see [Server Endpoints](Server-Endpoints.md)) and waits for the reply.
2. Treats the reply as JSON. If it is `{"error": "none"}` the login succeeds; anything else shows "Either the username or password is incorrect".
3. Remembers the username in the browser's `localStorage` under `Skylight.user`, and in `System.username`.
4. Loads the user's settings from `system/settings/<username>.xml`, sets the background, starts the clock and fills the home menu from `system/apps.xml`.

Every app window then receives the username in its query string, which is where [App.getUsername](App.getUsername.md) reads it.

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

The power button in the home menu calls `System.logoutUser()`, which removes the user from `system/LoggedInUsers.xml` (saved through `saveFile.php`) and clears `Skylight.user`. **The desktop stays on screen**: nothing returns to the login screen, so reload the page to log in again. If the user isn't listed in `LoggedInUsers.xml` (for example because the server didn't add them at login), logging out fails with a JavaScript error.

## Adding users

There is no working way to add users from inside Skylight OS: the Settings app's **Add User** page has a form, but its **Save** button does nothing. Users are managed by whatever `auth.php` checks against, and each one needs a settings file as described above.

[`system/settings/users.xml`](../siteroot/system/settings/users.xml) lists an `admin` user with an empty password. Nothing in the code reads it any more (an older, commented-out version of the login checked passwords in the browser against this file), but if your `auth.php` uses it, note that passwords are stored as plain text.

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
