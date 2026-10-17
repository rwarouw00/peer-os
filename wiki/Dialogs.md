## Dialogs

To open a dialog, use App.openDialog():

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

---

_Source: [Dialogs](https://sourceforge.net/p/skylightos/wiki/Dialogs/) on the SourceForge wiki · Labels: UI, dialog · Last modified 2014-01-21_
