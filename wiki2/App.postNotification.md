## App.postNotification

App.postNotification sends a [notification](notification.md) to the user.  It is displayed as a toast, and is inserted into the [Notification Tray](Notification-Tray.md).  It accepts two arguments: header and body.

```
App.postNotification(header, body);
```

> **In `siteroot`:** the code also accepts a third argument, `action`, intended to run when the notification is clicked in the tray. It doesn't work from apps: functions can't be sent to the desktop, so the action is dropped (see [Architecture](Architecture.md)).
>
> Both `header` and `body` are inserted as HTML. The toast shortens them to 20 and 110 characters; the tray shows the full text.
>
> ```
> App.postNotification('Download complete', 'report.pdf was saved to Documents.');
> ```

---

_Source: [App.postNotification](https://sourceforge.net/p/skylightos/wiki/App.postNotification/) on the SourceForge wiki · Labels: UI, notification, app object · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
