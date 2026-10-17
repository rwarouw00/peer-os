# Skylight OS Documentation

Skylight OS is a desktop environment that runs in a web browser. A single page shows a login screen, then a desktop with a taskbar, a home menu and notifications. Apps are ordinary web pages, each running in its own window.

This documentation describes the code in [`../siteroot`](../siteroot), which identifies itself as **Skylight OS Arctic Eagle v1.00.00** (2013). It was written from scratch by reading that code and running it in a browser. Everything here describes what the code does today, including the parts that are unfinished.

![The Skylight OS desktop](images/desktop.png)

## Contents

**Using Skylight OS**

* [Getting started](getting-started.md): run it on your own machine and log in
* [User guide](user-guide.md): the desktop, windows, notifications and the built-in apps

**Building apps**

* [Building your first app](building-apps.md): a step-by-step tutorial
* [App manifest](app-manifest.md): the `appData.json` file
* [App API](app-api.md): every function of the `App` object
* [UI components](ui-components.md): buttons, text boxes, dialogs and notifications

**How it works**

* [Architecture](architecture.md): the desktop page, apps, and how they talk to each other
* [System API](system-api.md): the desktop's own `System` object
* [Configuration](configuration.md): `apps.xml`, user settings and other files
* [Server API](server-api.md): the PHP scripts the browser code expects

**Status**

* [Security](security.md): the security model and its problems
* [Known issues](known-issues.md): bugs and unfinished features, with suggested fixes
* [Version notes](version-notes.md): how this release differs from the SourceForge wiki

## At a glance

| | |
| --- | --- |
| Runs on | Any modern browser; a web server with PHP |
| Written in | Plain JavaScript, HTML and CSS; no libraries or build step |
| App model | Each app is a folder with an `appData.json` and a web page, shown in an iframe |
| Data | XML and JSON files on the server, read over HTTP |
| Missing from `siteroot` | The server scripts in `system/server/` (login, saving files) |
