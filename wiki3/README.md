# Skylight OS Documentation

Skylight OS is a desktop environment that runs in a web browser: a login screen, then a desktop with a taskbar, a home menu, notifications and windows. Apps are ordinary web pages, each running in its own window.

This documentation covers the restructured code in [`../site3`](../site3). It is a rewrite of the original Arctic Eagle code (documented in [`../wiki2`](../wiki2/README.md)) that keeps the same features and app API names, stays on **ES5 JavaScript and PHP 5.3.7+**, and fixes the security problems and bugs found when the original was reviewed. Why and how it was restructured is in the [restructure proposal](restructure-proposal.md).

![The Skylight OS desktop](images/desktop.png)

## Contents

**Using Skylight OS**

* [Getting started](getting-started.md): install, add a user, log in
* [User guide](user-guide.md): the desktop, windows, notifications and the built-in apps

**Building apps**

* [Building your first app](building-apps.md): a step-by-step tutorial
* [App manifest](app-manifest.md): the `app.json` file
* [App API](app-api.md): every function of the `App` object
* [UI components](ui-components.md): buttons, text boxes, dialogs and notifications
* [Migrating apps from Arctic Eagle](migrating-apps.md)

**How it works**

* [Architecture](architecture.md): pages, scripts, messages and data
* [Desktop internals](desktop-internals.md): the `Desktop` object, and how to extend it
* [Server API](server-api.md): the PHP endpoints and shared library
* [Configuration and data](configuration.md): `apps.json`, users, settings and files

**Background**

* [Restructure proposal](restructure-proposal.md): what was wrong with the original structure, and what changed
* [Security](security.md): what is protected and what isn't
* [Limitations](limitations.md): known gaps and ideas for later

## At a glance

| | |
| --- | --- |
| Browser code | ES5 JavaScript, HTML and CSS; no libraries, no build step |
| Server code | PHP 5.3.7 or later (tested on PHP 8.3); no framework, no database |
| Data | JSON files in `site3/data/`, outside the web root |
| Logins | PHP sessions; passwords stored as bcrypt hashes |
| Size | About 1,400 non-blank lines in 51 files |
