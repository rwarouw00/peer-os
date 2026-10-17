## UI Elements

* [Buttons](Buttons.md)
* [Text boxes](#text-boxes)
* [Button bars](#button-bars)
* [Dialogs](Dialogs.md)
* [Windows](window.md)
* [Notifications](notification.md) and the [Notification Tray](Notification-Tray.md)
* [Context Menus](Context-Menus.md)

The look of buttons, text boxes and button bars comes from [`system/css/ui.css`](../siteroot/system/css/ui.css). Load it in your app's page to match the rest of the system:

```
<link href="../../../system/css/ui.css" rel="stylesheet"/>
```

It also sets the page font to Arial.

### Text boxes

Text and password inputs (`<input type="text">` and `<input type="password">`) get a grey rounded border and a slight inner shadow. No class is needed.

### Button bars

Three classes join plain elements into a segmented bar of buttons. Use `barEndLeft` for the first item, `barMiddle` for any in between, and `barEndRight` for the last:

```
<span class="barEndLeft" onclick="history.go(-1);">&lt;</span><span class="barEndRight" onclick="history.go(1);">&gt;</span>
```

The [Save File Dialog](Save-File-Dialog.md) uses this for its back and forward buttons. Keep the elements on one line with no spaces between them, or gaps appear.

---

_Source: [UI elements](https://sourceforge.net/p/skylightos/wiki/UI%20elements/) on the SourceForge wiki · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
