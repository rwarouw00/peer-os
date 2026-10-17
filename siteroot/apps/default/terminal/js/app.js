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
	
	postNotification: function (JSONObj) {
		JSONObj = JSON.parse(JSONObj);
		JSONObj.eventType = 'postNotification';
		JSONObj = JSON.stringify(JSONObj);
		parent.postMessage(JSONObj, '*');
	}
}