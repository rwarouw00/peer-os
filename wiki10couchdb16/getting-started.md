# Getting started

## 1. Install CouchDB 1.6

Download CouchDB 1.6.1 from the Apache archive, <https://archive.apache.org/dist/couchdb/>:

* **Windows:** `binary/win/1.6.1/setup-couchdb-1.6.1_R14B04.exe`, which can install CouchDB as a Windows service.
* **macOS:** `binary/mac/1.6.1/Apache-CouchDB-1.6.1.zip`, an app.
* **Linux:** build it from `source/1.6.1/apache-couchdb-1.6.1.tar.gz` (it needs Erlang R14B01 to 17, SpiderMonkey 1.8.5, ICU and libcurl), or use a distribution package of 1.6 (Ubuntu 16.04 had `couchdb` 1.6.0).

Consider **1.7.2** instead, from the same archive: it's the last 1.x release and fixes the security holes described in [Security](security.md). It uses the same HTTP API, so Skylight should work on it unchanged, but only 1.6.1 was tested.

## 2. Create an administrator

A new CouchDB 1.6 is an **"admin party"**: until an administrator exists, *everyone* is an administrator. Open CouchDB's admin pages, **Futon**, at <http://127.0.0.1:5984/_utils/>, and click **Fix this** at the bottom right to create one. (Or add a line under `[admins]` in CouchDB's `local.ini` and restart it.)

Pick an administrator name that no Skylight user will have, such as `couchadmin`: CouchDB checks administrator names before ordinary users, so a Skylight user with the same name couldn't log in.

CouchDB 1.6 creates the `_users` database itself.

## 3. Push the couchapp

From the `site10couchdb16` folder, with your administrator's name and password in the address:

```
tools/push.sh http://couchadmin:password@127.0.0.1:5984                                       (Linux, macOS)
powershell -ExecutionPolicy Bypass -File tools\push.ps1 http://couchadmin:password@127.0.0.1:5984   (Windows)
```

This:

1. adds a check to `_users` so that only administrators can create or change accounts (which also blocks CVE-2017-12635);
2. creates the `skylight` database and uploads the design document `_design/skylight`, with every file under `attachments/` as an attachment.

Run it again whenever you change a file; it replaces the design document. `push.sh` needs `bash`, `curl` and `base64`; `push.ps1` needs Windows PowerShell 5.1 (part of Windows 10 and 11) or PowerShell 7.

## 4. Add users

```
tools/add-user.sh http://couchadmin:password@127.0.0.1:5984 alice choose-a-password
powershell -ExecutionPolicy Bypass -File tools\add-user.ps1 http://couchadmin:password@127.0.0.1:5984 alice choose-a-password
```

This creates the CouchDB user and their private database (`userdb-616c696365` for `alice`), makes them its only member, and installs the checks in it. Run it again to change a password; users can't change their own (see [Security](security.md)). Usernames may use letters, digits, `-` and `_`.

## 5. Log in

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
* Look at your data in Futon: the `userdb-...` database holds your `settings` and `file:...` documents.
