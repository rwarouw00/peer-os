## AppID

An AppID is a random decimal number between 0 and 1 that is assigned to a specific instance of an app.  It is required when [closing an app's window](App.closeWindow.md).

> **In `siteroot`:** the desktop creates it with `Math.random()` each time a window opens, so opening the same app twice gives two different AppIDs. It is used as:
>
> * the HTML `id` of the window element on the desktop,
> * part of the taskbar icon's id (`taskBarUnit<AppID>`),
> * the `app-id` value in the app's query string, which is where [App.getAppID](App.getAppID.md) reads it from (as a string).
>
> AppIDs aren't secret or checked: any app that knows another window's AppID can close it.

---

_Source: [AppID](https://sourceforge.net/p/skylightos/wiki/AppID/) on the SourceForge wiki · Labels: AppID · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
