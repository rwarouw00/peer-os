# Getting started

## What you need

* A web server with PHP. The code is written in a PHP 5 style and also runs on current PHP 8 (tested with 8.3), which logs a few harmless warnings.
* A modern browser.
* Write access for the web server to `system/LoggedInUsers.xml`, `system/settings/` and `system/server/shortcuts.xml`.

Two requirements come from how the code is written:

* Serve Skylight OS from the **root** of a website (`http://localhost/`, not `http://localhost/skylight/`). Logout builds its save address in a way that only works there.
* Use the **standard port** (80 for HTTP, 443 for HTTPS). On an address like `http://localhost:8000`, `auth.php` sets a cookie the browser rejects: you can log in and use apps, but nothing can be saved. To use another port, change `$_SERVER['HTTP_HOST']` to `''` in the `setcookie` call in `system/server/auth.php`.

**Don't expose it to a network.** The server scripts let anyone write files on the server without logging in; see [Security](security.md).

## Run it locally

With PHP's built-in web server, from inside `siteroot`:

```
cd siteroot
sudo php -S localhost:80
```

(`sudo` is needed on Linux and macOS to use port 80.) Then open <http://localhost/>.

If you can't use port 80, run `php -S localhost:8000` instead: everything works except saving (the Settings background, logout).

## Log in

![The login screen](images/login.png)

Enter the username **admin**, leave the password empty, and click **Log In** (Enter doesn't submit the form). Accounts are listed in `system/settings/users.xml`; to add one, see [Configuration](configuration.md#usersxml).

## Next steps

* Take the [user guide](user-guide.md) tour of the desktop.
* Open the **Terminal** from the home menu and try a command such as `App.postNotification('Hello', 'It works');`.
* [Build your first app](building-apps.md).
