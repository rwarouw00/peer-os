/*
 * Skylight OS desktop: login, windows, taskbar, home menu, notifications,
 * dialogs and shortcuts. Plain ES5. Load system/js/core.js first.
 */
var Desktop = (function () {

	// ---- Small DOM helpers (no classList: it isn't in IE9) ----

	function byId(id) {
		return document.getElementById(id);
	}

	function hasClass(element, name) {
		return (' ' + element.className + ' ').indexOf(' ' + name + ' ') > -1;
	}

	function setClass(element, name, on) {
		var present = hasClass(element, name);
		if (on && !present) {
			element.className += ' ' + name;
		} else if (!on && present) {
			element.className = (' ' + element.className + ' ').replace(' ' + name + ' ', ' ').replace(/^ | $/g, '');
		}
	}

	function create(tag, className, html) {
		var element = document.createElement(tag);
		element.className = className;
		if (html) {
			element.innerHTML = html;
		}
		return element;
	}

	function remove(element) {
		if (element && element.parentNode) {
			element.parentNode.removeChild(element);
		}
	}

	// Walks up from an element to the nearest one carrying `attribute`.
	function closestWith(element, attribute) {
		while (element && element.getAttribute) {
			if (element.getAttribute(attribute) !== null) {
				return element;
			}
			element = element.parentNode;
		}
		return null;
	}

	function appIdOf(element) {
		var owner = closestWith(element, 'data-app-id');
		return owner ? owner.getAttribute('data-app-id') : null;
	}

	var APP_PATH = /^[\w\-\/]+\/$/;   // e.g. "apps/terminal/"

	var Desktop = {
		user: null,
		settings: null,
		windows: {},     // appID -> { element, frame, button }
		dialogs: {},     // dialog appID -> { callback } or { source, dialogId }
		manifests: {},   // app path -> parsed app.json
		topLayer: 1,

		// ---- Start-up and login ----

		start: function () {
			var form = byId('login');
			form.onsubmit = function (event) {
				event.preventDefault();
				var reply = Skylight.post('login', { username: form.username.value, password: form.password.value });
				form.password.value = '';
				if (reply.ok) {
					Desktop.show(reply.user, reply.settings);
				} else {
					byId('login-error').textContent = reply.error;
				}
			};

			document.addEventListener('click', Desktop.onClick, false);
			document.addEventListener('dblclick', Desktop.onDoubleClick, false);
			document.addEventListener('mousedown', Desktop.onMouseDown, false);
			document.addEventListener('contextmenu', Desktop.onContextMenu, false);
			window.addEventListener('message', Desktop.onMessage, false);

			// Still logged in from before a reload? Skip the login screen.
			var session = Skylight.get('settings');
			if (session.ok) {
				Desktop.show(session.user, session.settings);
			}
		},

		show: function (user, settings) {
			Desktop.user = user;
			Desktop.settings = settings;
			setClass(byId('login'), 'hidden', true);
			setClass(byId('desktop'), 'hidden', false);
			Desktop.applySettings();
			Desktop.buildAppMenu();
			Desktop.tickClock();
			setInterval(Desktop.tickClock, 1000);
		},

		logout: function () {
			Skylight.post('logout');
			location.reload();
		},

		tickClock: function () {
			var now = new Date();
			var hours = now.getHours();
			var minutes = now.getMinutes();
			byId('clock').textContent = ((hours + 11) % 12 + 1) + ':' + (minutes < 10 ? '0' : '') + minutes +
				(hours < 12 ? ' am ' : ' pm ') + Desktop.user;
		},

		// ---- Settings ----

		applySettings: function () {
			byId('desktop').style.backgroundImage = 'url("' + Skylight.root + Desktop.settings.background + '")';
			Desktop.renderShortcuts();
		},

		saveSettings: function (changes) {
			var reply = Skylight.post('settings', { changes: JSON.stringify(changes) });
			if (reply.ok) {
				Desktop.settings = reply.settings;
				Desktop.applySettings();
			} else {
				Desktop.notify('Settings not saved', reply.error);
			}
		},

		// ---- Apps and windows ----

		readManifest: function (path) {
			if (!Desktop.manifests[path] && APP_PATH.test(path)) {
				Desktop.manifests[path] = Skylight.getJson(Skylight.root + path + 'app.json');
			}
			return Desktop.manifests[path] || null;
		},

		buildAppMenu: function () {
			var paths = Skylight.getJson(Skylight.root + 'system/apps.json') || [];
			var menu = byId('menu-apps');
			for (var i = 0; i < paths.length; i++) {
				var manifest = Desktop.readManifest(paths[i]);
				if (manifest) {
					var icon = create('img', 'menu-app');
					icon.src = Skylight.root + paths[i] + manifest.icons['48'];
					icon.title = manifest.title;
					icon.setAttribute('data-action', 'openApp');
					icon.setAttribute('data-path', paths[i]);
					menu.appendChild(icon);
				}
			}
		},

		// Opens the app in folder `path` (relative to the site root, ending in "/").
		// `args` is an optional object of extra query string values. Returns the AppID.
		openWindow: function (path, args) {
			var manifest = Desktop.readManifest(path);
			if (!manifest) {
				Desktop.notify('Cannot open app', path + ' is not an app folder with a valid app.json.');
				return null;
			}

			var appID = String(Math.random()).slice(2);
			var width = parseInt(manifest.width, 10);
			var height = parseInt(manifest.height, 10);
			var query = { 'app-id': appID, username: Desktop.user };
			for (var name in args) {
				if (args.hasOwnProperty(name)) {
					query[name] = args[name];
				}
			}

			var win = create('div', 'window',
				'<div class="window-title" data-drag="window">' +
				'<img class="window-icon" alt=""/><span class="window-name"></span>' +
				'<span class="window-button window-maximize" data-action="toggleMaximize" title="Maximize"></span>' +
				'<span class="window-button window-close" data-action="closeWindow" title="Close"></span>' +
				'</div><iframe class="window-content"></iframe>');
			win.setAttribute('data-app-id', appID);
			win.style.width = width + 'px';
			win.style.height = height + 'px';
			win.style.left = (manifest.position === 'centered' ? Math.max(0, (window.innerWidth - width) / 2) : 100) + 'px';
			win.style.top = (manifest.position === 'centered' ? Math.max(0, (window.innerHeight - height) / 2) : 100) + 'px';
			win.getElementsByTagName('img')[0].src = Skylight.root + path + manifest.icons['16'];
			win.getElementsByTagName('span')[0].textContent = manifest.title;
			var frame = win.getElementsByTagName('iframe')[0];
			frame.src = Skylight.root + path + 'index.html?' + Skylight.toQuery(query);   // CouchDB has no folder index pages

			var button = create('img', 'taskbar-button');
			button.src = Skylight.root + path + manifest.icons['32'];
			button.title = manifest.title;
			button.setAttribute('data-app-id', appID);
			button.setAttribute('data-action', 'toggleWindow');

			byId('windows').appendChild(win);
			byId('taskbar').appendChild(button);
			Desktop.windows[appID] = { element: win, frame: frame, button: button };
			Desktop.focusWindow(appID);
			return appID;
		},

		closeWindow: function (appID) {
			var win = Desktop.windows[appID];
			if (win) {
				remove(win.element);
				remove(win.button);
				delete Desktop.windows[appID];
				delete Desktop.dialogs[appID];
			}
		},

		focusWindow: function (appID) {
			var win = Desktop.windows[appID];
			if (win) {
				win.element.style.zIndex = ++Desktop.topLayer;
			}
		},

		// Finds which of our windows a message came from.
		windowOfFrame: function (source) {
			for (var appID in Desktop.windows) {
				if (Desktop.windows.hasOwnProperty(appID) && Desktop.windows[appID].frame.contentWindow === source) {
					return appID;
				}
			}
			return null;
		},

		// ---- Dialogs ----

		// `callback` runs on OK for dialogs the desktop opens itself; for dialogs an
		// app opens, `source` and `dialogId` say where to send the answer.
		openDialog: function (message, callback, source, dialogId) {
			var appID = Desktop.openWindow('system/dialog/', { message: message });
			Desktop.dialogs[appID] = { callback: callback, source: source, dialogId: dialogId };
		},

		answerDialog: function (appID, confirmed) {
			var dialog = Desktop.dialogs[appID];
			Desktop.closeWindow(appID);
			if (!dialog || !confirmed) {
				return;
			}
			if (dialog.callback) {
				dialog.callback();
			} else if (dialog.source) {
				dialog.source.postMessage(JSON.stringify({ eventType: 'dialogConfirmed', dialogId: dialog.dialogId }), Skylight.origin);
			}
		},

		// ---- Notifications ----

		notify: function (header, body) {
			var html = '<span class="close-button" data-action="dismiss" title="Remove">&times;</span><b></b><p></p>';
			var item = create('div', 'notification', html);
			var toast = create('div', 'toast', html);
			item.getElementsByTagName('b')[0].textContent = toast.getElementsByTagName('b')[0].textContent = header;
			item.getElementsByTagName('p')[0].textContent = toast.getElementsByTagName('p')[0].textContent = body || '';

			var list = byId('notification-list');
			list.insertBefore(item, list.firstChild);
			byId('toasts').appendChild(toast);
			setTimeout(function () { setClass(toast, 'visible', true); }, 20);
			setTimeout(function () {
				setClass(toast, 'visible', false);
				setTimeout(function () { remove(toast); }, 1000);
			}, Desktop.settings.notificationTime);
		},

		// ---- Shortcuts (stored in the user's settings) ----

		renderShortcuts: function () {
			var container = byId('shortcuts');
			var shortcuts = Desktop.settings.shortcuts;
			container.innerHTML = '';
			for (var i = 0; i < shortcuts.length; i++) {
				var manifest = Desktop.readManifest(shortcuts[i].path);
				if (manifest) {
					var shortcut = create('div', 'shortcut', '<img alt="" draggable="false"/><div></div>');
					shortcut.setAttribute('data-drag', 'shortcut');
					shortcut.setAttribute('data-path', shortcuts[i].path);
					shortcut.style.left = shortcuts[i].left + 'px';
					shortcut.style.top = shortcuts[i].top + 'px';
					shortcut.getElementsByTagName('img')[0].src = Skylight.root + shortcuts[i].path + manifest.icons['64'];
					shortcut.getElementsByTagName('div')[0].textContent = manifest.title;
					container.appendChild(shortcut);
				}
			}
		},

		addShortcut: function (path) {
			var shortcuts = Desktop.settings.shortcuts.slice(0);
			for (var i = 0; i < shortcuts.length; i++) {
				if (shortcuts[i].path === path) {
					return;
				}
			}
			shortcuts.push({ path: path, left: 10 + 90 * (shortcuts.length % 10), top: 60 + 100 * Math.floor(shortcuts.length / 10) });
			Desktop.saveSettings({ shortcuts: shortcuts });
		},

		saveShortcutPositions: function () {
			var elements = byId('shortcuts').childNodes;
			var shortcuts = [];
			for (var i = 0; i < elements.length; i++) {
				shortcuts.push({
					path: elements[i].getAttribute('data-path'),
					left: parseInt(elements[i].style.left, 10),
					top: parseInt(elements[i].style.top, 10)
				});
			}
			Desktop.saveSettings({ shortcuts: shortcuts });
		},

		// ---- Home menu and context menu ----

		showMenu: function (visible) {
			setClass(byId('menu'), 'hidden', !visible);
			if (visible) {
				Desktop.showMenuPage('apps');
			}
		},

		showMenuPage: function (page) {
			setClass(byId('menu-apps'), 'hidden', page !== 'apps');
			setClass(byId('menu-notifications'), 'hidden', page !== 'notifications');
		},

		closeContextMenu: function () {
			remove(byId('context-menu'));
		},

		// ---- Event handling ----

		// Clicks are routed by the element's data-action attribute.
		actions: {
			toggleMenu: function () { Desktop.showMenu(hasClass(byId('menu'), 'hidden')); },
			showApps: function () { Desktop.showMenuPage('apps'); },
			showNotifications: function () { Desktop.showMenuPage('notifications'); },
			clearNotifications: function () { byId('notification-list').innerHTML = ''; },
			dismiss: function (target) { remove(target.parentNode); },
			logout: function () { Desktop.logout(); },
			openApp: function (target) {
				Desktop.showMenu(false);
				Desktop.openWindow(target.getAttribute('data-path'));
			},
			closeWindow: function (target) { Desktop.closeWindow(appIdOf(target)); },
			toggleMaximize: function (target) {
				var win = Desktop.windows[appIdOf(target)].element;
				setClass(win, 'maximized', !hasClass(win, 'maximized'));
			},
			toggleWindow: function (target) {
				var win = Desktop.windows[appIdOf(target)].element;
				var hidden = hasClass(win, 'hidden');
				setClass(win, 'hidden', !hidden);
				if (hidden) {
					Desktop.focusWindow(appIdOf(target));
				}
			}
		},

		onClick: function (event) {
			var target = event.target;
			var action = target.getAttribute('data-action');
			Desktop.closeContextMenu();
			if (action !== 'toggleMenu' && !closestWith(target, 'data-menu')) {
				Desktop.showMenu(false);
			}
			if (action && Desktop.actions.hasOwnProperty(action)) {
				Desktop.actions[action](target, event);
			}
		},

		onDoubleClick: function (event) {
			var shortcut = closestWith(event.target, 'data-path');
			if (shortcut && hasClass(shortcut, 'shortcut')) {
				Desktop.openWindow(shortcut.getAttribute('data-path'));
			}
		},

		onContextMenu: function (event) {
			var appID = appIdOf(event.target);
			if (!appID || !hasClass(event.target, 'taskbar-button')) {
				return;
			}
			event.preventDefault();
			Desktop.closeContextMenu();
			var menu = create('div', 'context-menu', '<div class="context-menu-item" data-action="closeWindow">Close</div>');
			menu.id = 'context-menu';
			menu.setAttribute('data-app-id', appID);
			menu.style.left = event.clientX + 'px';
			menu.style.top = event.clientY + 'px';
			byId('desktop').appendChild(menu);
		},

		// Brings windows to the front, and drags anything marked with data-drag.
		onMouseDown: function (event) {
			var appID = appIdOf(event.target);
			if (appID && Desktop.windows[appID]) {
				Desktop.focusWindow(appID);
			}

			var handle = closestWith(event.target, 'data-drag');
			if (!handle || event.button !== 0 || hasClass(event.target, 'window-button')) {
				return;
			}
			var item = handle.getAttribute('data-drag') === 'window' ? handle.parentNode : handle;
			if (hasClass(item, 'maximized')) {
				return;
			}
			event.preventDefault();

			var offsetX = event.clientX - item.offsetLeft;
			var offsetY = event.clientY - item.offsetTop;
			var moved = false;
			setClass(document.body, 'dragging', true);   // stops iframes swallowing the mouse

			function move(e) {
				moved = true;
				item.style.left = Math.max(0, e.clientX - offsetX) + 'px';
				item.style.top = Math.max(0, e.clientY - offsetY) + 'px';
			}

			function drop() {
				document.removeEventListener('mousemove', move, false);
				document.removeEventListener('mouseup', drop, false);
				setClass(document.body, 'dragging', false);
				if (moved && hasClass(item, 'shortcut')) {
					Desktop.saveShortcutPositions();
				}
			}

			document.addEventListener('mousemove', move, false);
			document.addEventListener('mouseup', drop, false);
		},

		// Messages from apps. Only our own windows, on our own site, are listened to.
		messages: {
			openWindow: function (message) {
				Desktop.openWindow(String(message.path));
			},
			closeWindow: function (message, sender) {
				Desktop.closeWindow(message.appID || sender);
			},
			openDialog: function (message, sender, source) {
				Desktop.openDialog(String(message.message), null, source, message.dialogId);
			},
			dialogAnswer: function (message, sender) {
				Desktop.answerDialog(sender, message.confirmed === true);
			},
			postNotification: function (message) {
				Desktop.notify(String(message.header), message.body ? String(message.body) : '');
			},
			saveSettings: function (message) {
				Desktop.saveSettings(message.changes);
			},
			addShortcut: function (message) {
				Desktop.addShortcut(String(message.path));
			}
		},

		onMessage: function (event) {
			var sender = Desktop.windowOfFrame(event.source);
			if (event.origin !== Skylight.origin || !sender) {
				return;
			}
			var message;
			try {
				message = JSON.parse(event.data);
			} catch (error) {
				return;
			}
			if (Desktop.messages.hasOwnProperty(message.eventType)) {
				Desktop.messages[message.eventType](message, sender, event.source);
			}
		}
	};

	return Desktop;
}());

Desktop.start();
