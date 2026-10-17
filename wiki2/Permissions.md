## Permissions

These permissions only apply to versions prealpha A2 and later.

Permission Name  | Description
---------------- | -------------
CHANGE_WALLPAPER | Permissions to change the desktop background
FILESYSTEM_ACCESS_READ| Permission to read files
FILESYSTEM_ACCESS_WRITE| Permission to write files
FILESYSTEM_ACCESS_FULL| Permission to have full file access (currently read/write, but will include create & delete)
ALLOW_NOTIFICATIONS| Permission to send local notifications
OPEN_WINDOWS     | Permission to open other windows
ALLOW_TTS        | Permission to use [TTS](App.speak.md)
ALLOW_OS_UPDATE  | Permissions to update Skylight OS; this is not intended for 3rd party apps
LOAD_THEME       | Permission to load system themes
ACCESS_USERNAME  | Permission to read the user's username

Apps request permissions in the `permissions` section of their [appData.json](appData.json.md).

> **In `siteroot`:** there is no permission system. The desktop never reads the `permissions` section of `appData.json`, and every app can call every [App function](App-Object.md): opening windows, sending notifications, changing the background, reading any file the web server publishes, and (where the server provides `saveFile.php`) saving files. The functions linked to `ALLOW_TTS` and `LOAD_THEME` don't exist.
>
> Even in later releases, a check made in the browser can't stop a determined app, because apps run JavaScript in the same browser. Anything that matters, such as which files a user may write, has to be enforced by the server scripts. See [Known Issues](Known-Issues.md).

---

_Source: [Permissions](https://sourceforge.net/p/skylightos/wiki/Permissions/) on the SourceForge wiki · Last modified 2014-03-13_

_Updated after reviewing the `siteroot` code._
