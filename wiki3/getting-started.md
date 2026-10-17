# Getting started

## What you need

* A web server with PHP. Skylight OS is mostly static files, but the Save File dialog and the server scripts are PHP. The test stand-in below needs PHP 8 or later.
* Skylight OS must be served from the **root** of the website (for example `http://localhost:8000/`, not `http://localhost:8000/skylight/`): logging out builds the save address in a way that only works there.
* A modern browser.
* The server scripts in `system/server/`. **They aren't included in `siteroot`**, so you need to provide them (see [Server API](server-api.md)) or use the test stand-in below.

## Run it locally

The stand-in below answers the three server scripts the desktop calls. It accepts any username and password and doesn't save anything, so use it only for trying things out.

1. Save this as `router.php` **outside** the `siteroot` folder:

   ```php
   <?php
   // Test stand-in for system/server/*.php. Accepts any login; saves nothing.
   $uri = urldecode(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH));
   if (str_starts_with($uri, '/system/server/')) {
       if (str_ends_with($uri, 'auth.php')) { echo json_encode(['error' => 'none']); return true; }
       echo 'NO ERROR';
       return true;
   }
   return false; // serve everything else from siteroot as normal
   ```

2. Start PHP's built-in web server from inside `siteroot`:

   ```
   cd siteroot
   php -S localhost:8000 ../router.php
   ```

3. Open <http://localhost:8000/> in your browser.

## Log in

![The login screen](images/login.png)

Enter the username **admin**, leave the password empty, and click **Log In** (Enter doesn't submit the form). With the stand-in any password works, but the username must be `admin`: it is the only user with a settings file, and without one the desktop doesn't load. To add another user, see [Configuration](configuration.md#user-settings).

## Next steps

* Take the [user guide](user-guide.md) tour of the desktop.
* Open the **Terminal** from the home menu and try a command such as `App.postNotification('Hello', 'It works');`.
* [Build your first app](building-apps.md).
