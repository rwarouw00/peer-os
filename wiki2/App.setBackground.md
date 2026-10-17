## App.setBackground

Changes the desktop's background image. It accepts one argument: the image's path **relative to the site root**.

```
App.setBackground('system/images/desktop/desktop_2.jpg');
```

The change only lasts until the user logs in again, because the background at login comes from the user's settings file (see [Users and Login](Users-and-Login.md)). To make it stick, also update the `<background>` element in that file, as the Settings app does:

```
var path = 'system/images/desktop/desktop_2.jpg';
var file = 'system/settings/' + App.getUsername() + '.xml';
var settings = App.parseXml(App.openFile('../../../' + file));
settings.getElementsByTagName('background')[0].setAttribute('src', path);
App.saveFile(file, App.serializeXml(settings));
App.setBackground(path);
```

The images that ship with Skylight OS are `system/images/desktop/desktop_1.jpg` to `desktop_6.jpg`. (`desktop_4` is actually named `desktop_4.JPG`, so it won't load on servers that treat file names as case-sensitive.)

The original wiki listed this function on the [App Object](App-Object.md) page but never wrote a page for it.

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
