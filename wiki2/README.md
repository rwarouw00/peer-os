# Skylight OS Wiki (revised)

This is a revised copy of the [Skylight OS SourceForge wiki](https://sourceforge.net/p/skylightos/wiki/browse_pages/) (the unmodified export is in [`../wiki`](../wiki/README.md)). Every page has been checked against the code in [`../siteroot`](../siteroot), corrected where the code disagrees, and new pages have been added for parts of the system the original wiki never covered. Start at [Home](Home.md).

## Which version is documented?

The `siteroot` code identifies itself as **Skylight OS Arctic Eagle v1.00.00** (2013). The SourceForge wiki was written in 2014 and describes features from **prealpha A2 and later**, several of which (permissions, themes, text-to-speech, the open file dialog) are not in `siteroot`. Rather than delete that material, pages keep the original text and add a callout wherever the code differs:

> **In `siteroot`:** what the Arctic Eagle code actually does.

Pages that only exist because of the code review are marked **new** below.

## Pages

| Page | Status |
| --- | --- |
| [Home](Home.md) | updated |
| [Architecture](Architecture.md) | **new** |
| [Desktop](Desktop.md) | **new** |
| [Users and Login](Users-and-Login.md) | **new** |
| [Configuration Files](Configuration-Files.md) | **new** |
| [Server Endpoints](Server-Endpoints.md) | **new** |
| [System Object](System-Object.md) | **new** |
| [Built-in Apps](Built-in-Apps.md) | **new** |
| [Context Menus](Context-Menus.md) | **new** |
| [Panels](Panels.md) | **new** |
| [Known Issues](Known-Issues.md) | **new** |
| [How to build a Skylight OS app](How-to-build-a-Skylight-OS-app.md) | updated |
| [appData.json](appData.json.md) | updated |
| [App Object](App-Object.md) | updated |
| [App.addShortcut](App.addShortcut.md) | **new** |
| [App.closeWindow](App.closeWindow.md) | updated |
| [App.getAppID](App.getAppID.md) | updated |
| [App.getQueryStringArgs](App.getQueryStringArgs.md) | updated |
| [App.getUsername](App.getUsername.md) | updated |
| [App.gotoLocation](App.gotoLocation.md) | updated |
| [App.openDialog](App.openDialog.md) | updated |
| [App.openFile](App.openFile.md) | updated |
| [App.openWindow](App.openWindow.md) | updated |
| [App.parseXml](App.parseXml.md) | updated |
| [App.postNotification](App.postNotification.md) | updated |
| [App.registerError](App.registerError.md) | updated (not in `siteroot`) |
| [App.saveFile](App.saveFile.md) | updated |
| [App.serializeXml](App.serializeXml.md) | updated |
| [App.setBackground](App.setBackground.md) | **new** |
| [App.speak](App.speak.md) | updated (not in `siteroot`) |
| [AppID](AppID.md) | updated |
| [UI elements](UI-elements.md) | updated |
| [Buttons](Buttons.md) | updated |
| [Dialogs](Dialogs.md) | updated |
| [window](window.md) | updated |
| [notification](notification.md) | updated |
| [Notification Tray](Notification-Tray.md) | updated |
| [Open File Dialog](Open-File-Dialog.md) | updated (not in `siteroot`) |
| [Save File Dialog](Save-File-Dialog.md) | updated |
| [Permissions](Permissions.md) | updated (not in `siteroot`) |
| [Themes](Themes.md) | updated (not in `siteroot`) |
| [Tutorials](Tutorials.md) | updated |
| [Tutorials-2](Tutorials-2.md) | unchanged (duplicate page with a leading space in its SourceForge title) |

Images from the original wiki are in [`images/`](images/), one folder per page. Screenshots of the `siteroot` code running are in [`images/siteroot/`](images/siteroot/).
