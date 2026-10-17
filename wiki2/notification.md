## Notification

A notification is a small bit of information that is passed to the user through a popout box called a toast, and in the [Notification Tray](Notification-Tray.md).  Notifications are unlimited as to their size, but a toast can hold a maximum of thirty header characters and one hundred body characters.  Any more than this will be truncated.  Notifications are simply pieces of text; they cannot be accepted and run scripts.

A toast slides out from the top right hand corner of the screen, and looks like this:

![A toast notification](images/notification/toast.png)

A notification is sent from an app with [App.postNotification()](App.postNotification.md). 

> **In `siteroot`:**
>
> * The toast shows at most **20** header characters and **110** body characters, followed by `...`. The tray keeps the full text.
> * The toast fades in (rather than sliding) at the top right, stays for the user's notification time, then fades out. The time comes from `displayNotificationsFor` in the user's settings file, in milliseconds; the default is 7000 (7 seconds). See [Users and Login](Users-and-Login.md).
> * Header and body are inserted as HTML, so markup works, and so does anything harmful a message might contain. Don't put text from untrusted sources in a notification.
> * The toast's × button does nothing; wait for it to fade.
> * A layout bug pushes most of the toast off the right edge of the screen at every screen size: only about the left 115 of its 300 pixels are visible, so longer text can't be read. The tray shows the full notification.
>
> ![A toast in siteroot, mostly cut off at the right edge](images/siteroot/toast.png)
>
> * The code has an `action` parameter for running a script when a notification is clicked, but apps can't use it (see [App.postNotification](App.postNotification.md)).

---

_Source: [notification](https://sourceforge.net/p/skylightos/wiki/notification/) on the SourceForge wiki · Labels: UI, notification · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
