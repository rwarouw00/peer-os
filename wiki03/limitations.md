# Limitations

Known gaps in site03, and ideas for later.

## Behaviour

* **Requests are synchronous.** As in the original, `App.readFile`, `App.writeFile`, `App.getSettings` and `App.openFile` block until the server answers. This keeps the API simple, but browsers warn about synchronous requests and the page freezes on a slow server. A later version could add callback versions alongside.
* **Windows can't be resized**, only maximized.
* **The Web Browser** can't show sites that refuse framing, and back/forward only work for pages on the same site.
* **Notifications** are kept in the page only; they are gone after a reload.

## Features removed from the original

These were unfinished in the original code and were removed rather than carried over (see the [restructure proposal](restructure-proposal.md#what-was-removed)):

* The **Save File dialog**: it couldn't return a file to an app. A file picker could be built on `App.listFiles()`.
* The **Add User** page in Settings: its Save button did nothing. Use `tools/add-user.php`.
* The Terminal's **Save Script** button, which did nothing.
* **Pin/Unpin**, **Remove** and **Delete App** menu items, which called functions that didn't exist.
* **Panels**, which were commented out.
* The notification **action** parameter, which apps couldn't use.

## Not tested

* Browsers other than Chromium. The code is checked to be ES5, but old browsers haven't been tried.
* PHP 5. The PHP avoids newer features and passes `php -l` on PHP 8.3, where it was tested; it hasn't been run on PHP 5.
