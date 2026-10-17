# Getting started

## Requirements

* **PHP 5.3.7 or later**, with a web server that runs it (Apache, nginx with PHP-FPM, or PHP's built-in server for trying it out). Tested on PHP 8.3.
* **A browser.** The code is plain ES5 and was tested in Chromium. The styles use `calc()`, gradients and transitions, so browsers older than about 2013 (Internet Explorer 9 and earlier) won't look right.

## Install

1. Copy the `site3` folder to the server.
2. Point the web server's document root at `site3/public/`. The `lib/`, `data/` and `tools/` folders must **not** be inside the document root.
3. Give the web server's user write access to `site3/data/`.
4. Create a user from the command line:

   ```
   php tools/add-user.php admin choose-a-password
   ```

   Run the same command again to change a password. Usernames may use letters, digits, `-` and `_`.

Unlike the original, Skylight OS can now run on any port and in a subfolder of a website.

## Try it locally

```
cd site3
php tools/add-user.php admin choose-a-password
cd public
php -S localhost:8000
```

Open <http://localhost:8000/>.

## Log in

![The login screen](images/login.png)

Enter the username and password and press Enter or click **Log In**. If you reload the page while logged in, you stay logged in until you use the power button or close the browser.

## Next steps

* Take the [user guide](user-guide.md) tour.
* Open the **Terminal** from the home menu and try `App.postNotification('Hello', 'It works');`.
* [Build your first app](building-apps.md).
