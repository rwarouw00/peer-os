## App.openDialog

> **This function is still in development and the API is subject to change.  Developers should subscribe to the [RSS feed](https://sourceforge.net/p/skylightos/wiki/App.openDialog/feed) to be notified of changes.**

This function opens a [Dialog](Dialogs.md).  It accepts three arguments: 

```
App.openDialog(text, btnLabels, onOK);
```

> **In `siteroot`:** the function takes **two** arguments, and there is no way to change the button labels:
>
> ```
> App.openDialog(text, onOK);
> ```
>
> | Argument | Type | Meaning |
> | --- | --- | --- |
> | `text` | string | The message. It is inserted as HTML, so tags such as `<b>` and `<br/>` work. |
> | `onOK` | string, optional | JavaScript **source code** to run when OK is clicked. A function will not work, because it can't be sent to the desktop (see [Architecture](Architecture.md)). |
>
> The dialog always has an **OK** and a **Cancel** button, and both close it. The `onOK` code runs with `eval` **inside the dialog's own window**, not inside your app, so it can't see your app's variables. To act on the desktop, call `parent.System` from that code:
>
> ```
> App.openDialog('Delete this file?', "parent.System.postNotification('Deleted', 'The file was deleted')");
> ```
>
> The call doesn't wait for the user and returns nothing, so your app can't find out which button was clicked.
>
> ![The Skylight OS dialog](images/siteroot/dialog.png)

---

_Source: [App.openDialog](https://sourceforge.net/p/skylightos/wiki/App.openDialog/) on the SourceForge wiki · Labels: dialog, app object · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
