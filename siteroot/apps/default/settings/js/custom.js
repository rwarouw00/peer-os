var handleClick = function (event) {
	target = event.target;
	if (target.className == 'backgroundPreview') {
		var settings = App.openFile('../../../system/settings/' + App.getUsername() + '.xml');
		settings = App.parseXml(settings);
		var background = settings.getElementsByTagName('background')[0];
		background.setAttribute('src', target.getAttribute('data-path'));
		settings = App.serializeXml(settings);
		App.saveFile('system/settings/' + App.getUsername() + '.xml', settings);
		
		App.setBackground(target.getAttribute('data-path'));
	}
}

var handleChange = function (event) {
	if (target.id == 'notifDisplayTime') {
		alert('test');
	}
}

document.addEventListener('click', handleClick);
document.addEventListener('change', handleChange);