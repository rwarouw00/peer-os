var System = {
	
	
	// Variables
	
	selected: {},
	username: '',
	background: '',
	displayNotificationsFor: '7000',
	
	// Functions
	openFile: function (file) {
		var xhr = new XMLHttpRequest();
		xhr.open('get', file, false);
		xhr.send(null);
		
		return xhr.responseText;
	},
	
	saveFile: function (file, contents) {
		var xhr = new XMLHttpRequest();
		xhr.open('post', '../../../system/server/saveFile.php', false);
		xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		xhr.send('file=' + file + '&contents=' + contents + '&user=' + this.username);
		
		return xhr.responseText;
	},
	
	openWindow: function (path, args) {
		
		var appID = Math.random();
		var windowObj = document.createElement('div');
		var windowObjBanner = document.createElement('div');
		var windowObjActionButtonHolder = document.createElement('div');
		var windowObjActionClose = document.createElement('div');
		var windowObjActionMinimize = document.createElement('div');
		var windowObjContent = document.createElement('iframe');
		var windowObjTitle = document.createElement('div');
		var windowObjIcon = document.createElement('img');
		var desktop = document.getElementById('windowContainer');
		var taskBarElement = document.createElement('span');
		var taskBar = document.getElementsByClassName('taskbar')[0];
		
		var appData = System.openFile(path + 'appData.json');
		
		windowObj.style.visibility = 'hidden';
		windowObj.className = 'window draggable';
		windowObjBanner.className = 'windowBanner';
		windowObjActionButtonHolder.className = 'windowActionButtonHolder';
		
		windowObjActionClose.className = 'windowActionButtonClose';
		//windowObjActionClose.style.backgroundColor = 'red';
		//windowObjActionClose.title = 'Close';
		
		windowObjActionMinimize.className = 'windowActionButtonMinimize';
		//windowObjActionMinimize.style.backgroundColor = 'green';
		//windowObjActionMinimize.title = 'Minimize';
		
		windowObjContent.className = 'windowContent';
		//if (args !== null) {
			windowObjContent.src = path + '?app-id=' + encodeURIComponent(appID) + '&username=' + System.username + '&' + args;
		//} else {
		//	windowObjContent.src = path + '?app-id=' + encodeURIComponent(appID) + '&username=' + System.username;
		//}
		
		windowObjTitle.className = 'windowTitle';
		windowObjIcon.className = 'windowIcon';
		windowObjIcon.src = '';
		
		taskBarElement.className = 'taskBarAppUnit';
		
		desktop.appendChild(windowObj);
		windowObj.appendChild(windowObjBanner);
		windowObjBanner.appendChild(windowObjIcon);
		windowObjBanner.appendChild(windowObjTitle);
		windowObjBanner.appendChild(windowObjActionButtonHolder);
		windowObjActionButtonHolder.appendChild(windowObjActionClose);
		windowObjActionButtonHolder.appendChild(windowObjActionMinimize);
		windowObj.appendChild(windowObjContent);
		taskBar.appendChild(taskBarElement);

		appData = JSON.parse(appData);
		windowObjTitle.innerHTML = appData.title;
		windowObj.id = appID;
		windowObj.setAttribute('data-appID', appID);
		windowObj.setAttribute('data-state', 'default');
		windowObjIcon.src = appData.icons.small;
		windowObj.style.width = appData.width + 'px';
		windowObj.style.height = appData.height + 'px';
		windowObjContent.style.height = appData.height - 30 + 'px';
		windowObjContent.name = 'window' + appData.title;
		windowObjContent.id = 'window' + appData.title + appID;
		windowObjActionClose.id = appData.title + 'CloseButton' + appID;
		windowObjActionMinimize.id = appData.title + 'MinButton' + appID;
		
		if (appData.position == 'centered') {
			var halfTop = appData.height / 2;
			var halfLeft = appData.width / 2;
			windowObj.style.top = window.innerHeight / 2 - halfTop + 'px';
			windowObj.style.left = window.innerWidth / 2 - halfLeft + 'px';
		} else {
			windowObj.style.top = '100px';
			windowObj.style.left = '100px';
		}
		
		taskBarElement.id = 'taskBarUnit' + appID;
		taskBarElement.setAttribute('data-appID', appID);
		taskBarElement.innerHTML = '<img src="' + appData.icons.medium + '" style="position:relative;top:3px;left:5px" title="' + appData.title + '"/>';
		
		
		windowObj.style.visibility = 'visible';
		
		return appID;
		
	},
	
	hideWindow: function (appID) {
		document.getElementById(appID).style.visibility = 'hidden';
	},
	
	showWindow: function (appID) {
		document.getElementById(appID).style.visibility = 'visible';
	},
	
	closeWindow: function (appID) {
		document.getElementById(appID).outerHTML = '';
		document.getElementById('taskBarUnit' + appID).outerHTML = '';
	},
	
	openDialog: function (msg, onOK) {
		System.openWindow('system/components/dialog/');
		localStorage.setItem('Skylight.dialog.message', msg);
		if (onOK !== null) { localStorage.setItem('Skylight.dialog.onOK', onOK); }
	},
	
	/*openPanel: function (path, args) {
		
		var appData = System.openFile(path + 'appData.json');
		var appID = Math.random();
		var panel = document.createElement('div');
		var titleBar = document.createElement('div');
		var panelContent = document.createElement('iframe');
		var panelContainer = document.getElementById('panelContainer');
		
		appData = JSON.parse(appData);
		panel.className = 'panel draggable';
		panel.id = 'panel' + appID;
		panel.setAttribute('app-id', appID);
		titleBar.className = 'panelTitleBar';
		panelContent.className = 'panelFrame';
		
		if (args !== null) {
			panelContent.src = path + '?app-id=' + encodeURIComponent(appID) + '&' + args;
		} else {
			panelContent.src = path + '?app-id=' + encodeURIComponent(appID);
		}
		
		titleBar.innerHTML = appData.title;
		
		panelContainer.appendChild(panel);
		panel.appendChild(titleBar);
		panel.appendChild(panelContent);
		
		//var computedStyle = document.defaultView.getComputedStyle(panel, null);
		panel.style.bottom = '-270px';
		//alert(computedStyle.height);
		panel.style.visibility = 'visible';
		
		return appID;
	},
	
	closePanel: function (appID) {
		document.getElementById('panel' + appID).outerHTML = '';
	},*/
	
	addShortcut: function (path) {
		var appData = System.openFile(path + 'appData.json');
		var shortcutContainer = document.getElementById('shortcutContainer');
		var shortcut = document.createElement('div');
		var shortcutScript = document.createElement('script');
		shortcutContainer.style.top = '30px';
		var xhr = new XMLHttpRequest();
		var shortcutInfo = {
			action: 'add',
			path: path,
			top: shortcutContainer.style.top,
			left: '0px'
		}
		shortcutInfo = JSON.stringify(shortcutInfo);
		
		appData = JSON.parse(appData);
		
		shortcut.className = 'shortcut draggable';
		shortcut.id = 'shortcut' + appData.title;
		shortcut.innerHTML = '<img draggable="false" src="' + appData.icons.Xlarge + '" /><div>' + appData.title + '</div><div class="shortcutCover"></div><div class="shortcutPath" style="display:none;">' + path + '</div>';
		
		shortcutScript.innerHTML = 'document.getElementById("shortcut' + appData.title + '").ondblclick = function() {System.openWindow("' + path + '");}';
		
		shortcutContainer.appendChild(shortcut);
		shortcutContainer.appendChild(shortcutScript);
		
		xhr.open('post', 'system/server/shortcuts.php', false);
		xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		xhr.send('contents=' + encodeURIComponent(shortcutInfo));
		
		if (xhr.responseText != 'NO ERROR') {
			System.openDialog('There was an error creating the shortcut.<br/>' + xhr.responseText);
		}
		
	},
	
	addAppIcon: function (path) {
		var appData = System.openFile(path + 'appData.json');
		appData = JSON.parse(appData);
		
		var app = document.createElement('span');
		app.className = 'appItem';
		app.innerHTML = '<img src="' + appData.icons.large + '" title="' + appData.title + '" onclick="System.openWindow(\'' + path + '\');"/>';
		
		document.getElementById('apps').appendChild(app);
	},
	
	postNotification: function (header, body, action) {
		var notification = document.createElement('tr');
		notification.className = 'notificationElement';
		var contents = null;
		
		if (action != null) {
			notification.onclick = action;
		}
		
		contents = '<span class="notificationHeader">' + header + '</span><img src="system/images/cmd_buttons/close_button.png" class="notificationCloseButton"/><br/>';
		
		if (body != null) {
			contents += body;
		}
		
		notification.innerHTML = contents;
		
		document.getElementById('notificationArea').appendChild(notification);
		System.toast.send(header, body);
	},
	
	toast: {
		header: 'Header',
		text: 'Text',
		autoHide: true,
		
		send: function (header, text) {
			if (header !== null) {
				this.header = header;
			}
			
			if (text !== null) {
				this.text = text;
			}
			
			var toast = document.createElement('div');
			var header = document.createElement('div');
			var text = document.createElement('div');
			var icon = null;
			var closeButton = document.createElement('span');
			var textContainer = document.createElement('span');
			
			textContainer.className = 'toastTextContainer';
			
			toast.className = 'toastAnimState0';
			
			if (this.header.length > 20) {
				this.header = this.header.substr(0, 20) + '...';
			}
			
			if (this.text.length > 110) {
				this.text = this.text.substr(0, 110) + '...';
			}
			
			header.innerHTML = this.header;
			header.className = 'toastHeader';
			
			text.innerHTML = this.text;
			text.className = 'toastText';
			
			closeButton.className = 'toastCloseButton';
			
			document.getElementById('toastContainer').appendChild(toast);
			toast.appendChild(textContainer);
			textContainer.appendChild(header);
			textContainer.appendChild(text);
			toast.appendChild(closeButton);
			
			setTimeout(function () {toast.className = 'toastAnimState1';}, 300);
			
			if (this.autoHide == true) {
				setTimeout(function () {
					toast.className = 'toastAnimState0';
					
					setTimeout(function () {
						toast.outerHTML = '';
					}, 500);
				}, System.displayNotificationsFor);
			}
			
			return toast;
		}
	},
	
	parseXml: function (xml) {
		var xmldom = null;
		
		if (typeof DOMParser != 'undefined') {
			xmldom = (new DOMParser()).parseFromString(xml, 'text/xml');
			var errors = xmldom.getElementsByTagName('parsererror');
			if (errors.length) {
				System.openDialog('Skylight OS has encountered a problem.<br/>Technical Details:<br/>XML parsing error:' + errors[0].textContent);
			}
		} else if (document.implementation.hasFeature('LS', '3.0')) {
			var implementation = document.implementation;
			var parser = implementation.createLSParser(implementation.MODE_SYNCHRONOUS, null);
			var input = implementation.createLSInput();
			input.stringData = xml;
			xmldom = parser.parse(input);
		} else if (typeof ActiveXObject != 'undefined') {
			xmldom = createDocument();
			xmldom.loadXML(xml);
			if (xmldom.parseError != 0) {
				System.openDialog('Skylight OS has encountered a problem.<br/>Technical Details:<br/>XML parsing error:' + xmldom.parseError.reason);
			}
		} else {
			System.openDialog('Sorry, but you browser seems to be incompatible with Skylight OS :(.  Try Mozilla Firefox http://mozilla.org.');
		}
		
		return xmldom;
	},
	
	serializeXml: function (xmldom) {
		if (typeof XMLSerializer != 'undefined') {
			return (new XMLSerializer()).serializeToString(xmldom);
		} else if (document.implementation.hasFeature('LS', '3.0')) {
			var implementation = document.implementation;
			var serializer = implementation.createLSSerializer();
			return serializer.writeToString(xmldom);
		} else if (typeof xmldom.xml != 'undefined') {
			return xmldom.xml;
		} else {
			System.openDialog('Sorry, but you browser seems to be incompatible with Skylight OS :(.  Try Mozilla Firefox http://mozilla.org.');
		}
	},
	
	renderContextMenu: function (event, xmlString) {
		
		var xmldom = System.parseXml(xmlString);
		var i = 0;
		var items = xmldom.getElementsByTagName('item');
		var menu = document.createElement('table');
		menu.className = 'contextmenu';
		menu.id = 'contextmenu';
		var menuContents = '';
		do {
			menuContents += '<tr><td class="contextmenuItem" onclick="' + items[i].innerHTML + '">' + items[i].getAttribute('value') + '</td></tr>';
			i++;
		} while (i < items.length);
		
		menu.innerHTML = menuContents;
		
		menu.style.top = event.clientY + 'px';
		menu.style.left = event.clientX + 'px';
		
		document.getElementById('desktop').appendChild(menu);
	},
	
	logoutUser: function () {
		var file = System.openFile('system/LoggedInUsers.xml');
		var xml = System.parseXml(file);
		var user = localStorage.getItem('Skylight.user');
		xml.getElementsByTagName(user)[0].outerHTML = '';
		xml = System.serializeXml(xml);
		System.saveFile('system/LoggedInUsers.xml', xml);
		localStorage.setItem('Skylight.user', '');
	},
	
	handleAppEvent: function (event) {
		var JSONObj = JSON.parse(event.data);
		if (JSONObj.eventType == 'openDialog') {
			System.openDialog(JSONObj.message, JSONObj.onOK);
		} else if (JSONObj.eventType == 'openWindow') {
			System.openWindow(JSONObj.path);
		} else if (JSONObj.eventType == 'closeWindow') {
			System.closeWindow(JSONObj.title);
		} else if (JSONObj.eventType == 'postNotification') {
			
			System.postNotification(JSONObj.header, JSONObj.body, JSONObj.action);
		} else if (JSONObj.eventType == 'setBackground') {
			document.getElementById('desktop').style.backgroundImage = 'url(' + JSONObj.path + ')';
		} else if (JSONObj.eventType == 'addShortcut') {
			System.addShortcut(JSONObj.path);
		}
	}
	
}