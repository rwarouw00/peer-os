var App = {
	
	openDialog: function (msg, onOK) {
		
		var JSONObj = {
			eventType: 'openDialog',
			message: msg
		}
		
		if (onOK !== null) {
			JSONObj.onOK = onOK;
		}
		
		JSONObj = JSON.stringify(JSONObj);
		parent.postMessage(JSONObj, '*');
	},
	
	openWindow: function (path) {
		var JSONObj = {
			eventType: 'openWindow',
			path: path
		}
		
		JSONObj = JSON.stringify(JSONObj);
		parent.postMessage(JSONObj, '*');
	},

	closeWindow: function (title) {
		var JSONObj = {
			eventType: 'closeWindow',
			title: title
		}
		
		JSONObj = JSON.stringify(JSONObj);
		parent.postMessage(JSONObj, '*');
	},
	
	postNotification: function (header, body, action) {
		var obj = {
			eventType: 'postNotification',
			header: header,
			body: body,
			action: action
		}
		obj = JSON.stringify(obj);
		parent.postMessage(obj, '*');
	},
	
	addShortcut: function (path) {
		var obj = {
			eventType: 'addShortcut',
			path: path
		}
		
		obj = JSON.stringify(obj);
		parent.postMessage(obj, '*');
	},
	
	getQueryStringArgs: function () {
		
		var qs = (location.search.length > 0 ? location.search.substring(1) : '');
		
		var args = {};
		
		var items = qs.split('&');
		var item = null,
			name = null,
			value = null;
		
		for (var i=0; i < items.length; i++) {
			item = items[i].split('=');
			name = decodeURIComponent(item[0]);
			value = decodeURIComponent(item[1]);
			args[name] = value;
		}
		
		return args;
	},
	
	getAppID: function () {
		
		var args = App.getQueryStringArgs();
		
		return args['app-id'];
	},
	
	getUsername: function () {
		
		var args = App.getQueryStringArgs();
		
		return args['username'];
	},
	
	/*openPanel: function (path, args) {
		var obj = {
			eventType: 'openPanel',
			path: path,
			args: args
		}
		
		obj = JSON.stringify(obj);
		parent.postMessage(obj, '*');
	}*/
	
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
		xhr.send('file=' + file + '&contents=' + contents + '&user=' + this.getUsername());
		
		return xhr.responseText;
	},
	
	parseXml: function (xml) {
		var xmldom = null;
		
		if (typeof DOMParser != 'undefined') {
			xmldom = (new DOMParser()).parseFromString(xml, 'text/xml');
			var errors = xmldom.getElementsByTagName('parsererror');
			if (errors.length) {
				App.openDialog('Skylight OS has encountered a problem.<br/>Technical Details:<br/>XML parsing error:' + errors[0].textContent);
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
				App.openDialog('Skylight OS has encountered a problem.<br/>Technical Details:<br/>XML parsing error:' + xmldom.parseError.reason);
			}
		} else {
			App.openDialog('Sorry, but you browser seems to be incompatible with Skylight OS :(.  Try Mozilla Firefox http://mozilla.org.');
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
			App.openDialog('Sorry, but you browser seems to be incompatible with Skylight OS :(.  Try Mozilla Firefox http://mozilla.org.');
		}
	},
	
	setBackground: function (path) {
		obj = {
			eventType: 'setBackground',
			path: path
		}
		
		obj = JSON.stringify(obj);
		parent.postMessage(obj, '*');
	},
	
	gotoLocation: function (path, queryString) {
		location.assign(path + location.search + '&' + queryString);
	}
}