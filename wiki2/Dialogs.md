## Dialogs

To open a dialog, use [App.openDialog()](App.openDialog.md):

```
App.openDialog('This is the Dialog Text.');
```

This is what the default Dialog looks like:

![Default Dialog](images/Dialogs/default%20dialog.png)

The default button also has the <abbr title="This class makes the button hop up and down">pulse</abbr> class added to it.

If you want to change the text inside the buttons, then use this:


```
App.openDialog('This is the Dialog Text.', 'ok=Yes&cancel=No');
```

![Dialog with Button Text Changed](images/Dialogs/dialog%20btn-text%20changed.png)

You can also execute a script when the default button is pushed:

```
App.openDialog('This is the Dialog Text.', '', 'alert("you clicked OK");');
```

> **In `siteroot`:** the dialog is a small 300×200 window with the message and two buttons, **OK** and **Cancel**. It looks like this:
>
> ![The siteroot dialog](images/siteroot/dialog.png)
>
> * Button labels can't be changed, and there is no pulse animation.
> * The script to run on OK is the **second** argument, not the third: `App.openDialog('This is the Dialog Text.', 'alert("you clicked OK");');`. The third example above would treat `''` as the script and ignore the rest.
> * The script runs inside the dialog's window. Use `parent.System` to act on the desktop (see [App.openDialog](App.openDialog.md)).
> * Both buttons close the dialog. Your app isn't told which one was clicked.
>
> The dialog lives in [`system/components/dialog/`](../siteroot/system/components/dialog/). The desktop hands it the message and script through the browser's `localStorage` (keys `Skylight.dialog.message` and `Skylight.dialog.onOK`), so two dialogs opened at the same moment can show the wrong text. The desktop also uses dialogs for its own errors, such as invalid XML.
>
> Because the Dialog is listed in `system/apps.xml`, it also appears in the home menu, where opening it shows an empty dialog.

---

_Source: [Dialogs](https://sourceforge.net/p/skylightos/wiki/Dialogs/) on the SourceForge wiki · Labels: UI, dialog · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
