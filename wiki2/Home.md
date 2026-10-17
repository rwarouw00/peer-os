## Skylight OS Developer Guide

Here you can find all sorts of information on how to create Skylight OS apps.  Here are some quick links:

* [UI elements](UI-elements.md)
* [App Object](App-Object.md)
* [Permissions](Permissions.md)
* [How to Build a Skylight OS App](How-to-build-a-Skylight-OS-app.md)

### Understanding the system

Skylight OS is a desktop environment that runs in a web browser. The desktop is a single page, and every app runs in its own `<iframe>` window on top of it. These pages explain how the pieces fit together:

* [Architecture](Architecture.md): the desktop page, the `System` and `App` objects, and how apps talk to the desktop
* [Desktop](Desktop.md): the login screen, taskbar, home menu, clock and desktop shortcuts
* [Users and Login](Users-and-Login.md): how logging in and out works, and where user settings live
* [Configuration Files](Configuration-Files.md): `apps.xml`, per-user settings and the other XML files
* [Server Endpoints](Server-Endpoints.md): the PHP scripts that handle login, saving and shortcuts
* [System Object](System-Object.md): the desktop-side API that `App` calls end up in
* [Built-in Apps](Built-in-Apps.md): Terminal, Settings, Web Browser, Dialog and Save File
* [Context Menus](Context-Menus.md): the right-click menus and their XML format
* [Panels](Panels.md): an unfinished feature found in the code
* [Known Issues](Known-Issues.md): bugs and security problems found while reviewing the code

> **In `siteroot`:** the code is Skylight OS Arctic Eagle v1.00.00. Some pages in this guide describe later releases; look for these callouts to see what the code actually does.

![The Skylight OS desktop with the home menu open](images/siteroot/desktop.png)

---

_Source: [Home](https://sourceforge.net/p/skylightos/wiki/Home/) on the SourceForge wiki · Last modified 2014-01-21_

_Updated after reviewing the `siteroot` code._
