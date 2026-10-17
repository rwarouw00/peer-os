# Getting started

## 1. Install CouchDB 2.3.1

Download CouchDB 2.3.1 from the Apache archive, <https://archive.apache.org/dist/couchdb/>:

* **Windows:** `binary/win/2.3.1/apache-couchdb-2.3.1.msi`, which installs CouchDB as a Windows service.
* **macOS:** `binary/mac/2.3.1/Apache-CouchDB-2.3.1.zip`, an app.
* **Linux:** build it from `source/2.3.1/apache-couchdb-2.3.1.tar.gz` (it needs Erlang 19, 20.3.8.11 or later, or 21.2.3 or later, and SpiderMonkey 1.8.5), or use a distribution package of 2.3.x.

Then open CouchDB's admin pages, **Fauxton**, at <http://127.0.0.1:5984/_utils/>, choose **Setup**, and configure a **single node**. This creates an administrator and the system databases, including `_users`.

Pick an administrator name that no Skylight user will have, such as `couchadmin`: CouchDB checks administrator names before ordinary users, so a Skylight user with the same name couldn't log in.

## 2. Push the couchapp

From the `site9couchdb` folder, with your administrator's name and password in the address:

```
tools/push.sh http://couchadmin:password@127.0.0.1:5984                                     (Linux, macOS)
powershell -ExecutionPolicy Bypass -File tools\push.ps1 http://couchadmin:password@127.0.0.1:5984   (Windows)
```

This:

1. turns on **couch_peruser**, so each user gets a private database;
2. stops anonymous sign-ups, by adding a check to `_users`;
3. creates the `skylight` database and uploads the design document `_design/skylight`, with every file under `attachments/` as an attachment.

Run it again whenever you change a file; it replaces the design document. `push.sh` needs `bash`, `curl` and `base64`; `push.ps1` needs Windows PowerShell 5.1 (part of Windows 10 and 11) or PowerShell 7.

## 3. Add users

```
tools/add-user.sh http://couchadmin:password@127.0.0.1:5984 alice choose-a-password
powershell -ExecutionPolicy Bypass -File tools\add-user.ps1 http://couchadmin:password@127.0.0.1:5984 alice choose-a-password
```

This creates the CouchDB user, waits for couch_peruser to create their database (`userdb-616c696365` for `alice`), and installs the checks in it. Run it again to change a password. Usernames may use letters, digits, `-` and `_`.

## 4. Log in

Open:

```
http://127.0.0.1:5984/skylight/_design/skylight/_rewrite/
```

![The login page](images/login.png)

`.../_design/skylight/index.html` works too. Log in with a user you added. You stay logged in across reloads until you log out, or until 10 minutes pass without Skylight asking CouchDB anything (CouchDB's default; see [Data and configuration](data-and-configuration.md#couchdb-settings)).

## Next steps

* Take the [user guide](user-guide.md) tour.
* Open the **Terminal** and run `App.postNotification('Hello', 'It works');`.
* [Build your first app](building-apps.md).
* Look at your data in Fauxton: the `userdb-...` database holds your `settings` and `file:...` documents.
