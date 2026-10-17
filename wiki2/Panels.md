# Panels

`siteroot` contains the start of a feature called panels: small windows that were meant to sit at the bottom of the screen (the commented-out code positions them there). It isn't usable.

What exists:

* `System.openPanel(path, args)` and `System.closePanel(appID)` in [`system.js`](../siteroot/system/js/system.js), and `App.openPanel` in [`app.js`](../siteroot/system/js/app.js), all **commented out**.
* Styles for `.panel`, `.panelTitleBar` and `.panelFrame` in `system.css`. The `#panelContainer` they would go in is commented out, and the desktop page has no such element.
* A sample panel in [`apps/panels/hello world/`](../siteroot/apps/panels/hello%20world/), whose `appData.json` has only a `title` and a small icon.
* A right-click menu for panel title bars (see [Context Menus](Context-Menus.md)).

To finish the feature, the functions need uncommenting, a `<div id="panelContainer">` added to `index.html`, and an `openPanel` case added to `System.handleAppEvent`.

---

_New page, written from the `siteroot` code. Not on the SourceForge wiki._
