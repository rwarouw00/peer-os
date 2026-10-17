<?php
/*
 * Base class for Skylight OS apps. Each app is a class that extends App and
 * builds its window's contents in build(). Plain PHP 5.2.
 */
class App
{
	public $desktop;   // the Desktop that opened us
	public $appID;     // this window's ID
	public $path;      // e.g. "apps/terminal/"
	public $manifest;  // parsed app.json
	public $window;    // our GtkWindow
	public $args;      // extra values passed to openWindow()

	function __construct($desktop, $appID, $path, $manifest, $window, $args)
	{
		$this->desktop = $desktop;
		$this->appID = $appID;
		$this->path = $path;
		$this->manifest = $manifest;
		$this->window = $window;
		$this->args = $args;
	}

	// Override: add widgets to $this->window.
	function build()
	{
	}

	// Override: return false to stop the window closing (e.g. to ask about unsaved work).
	function canClose()
	{
		return true;
	}

	// ---- Windows ----

	function openWindow($path, $args = array())
	{
		return $this->desktop->openWindow($path, $args);
	}

	function closeWindow($appID = null)
	{
		$this->desktop->closeWindow($appID === null ? $this->appID : $appID);
	}

	// ---- Dialogs and notifications ----

	// Shows OK and Cancel. $onOK is a callback (e.g. array($this, 'deleteNote')), run on OK.
	function openDialog($message, $onOK = null)
	{
		return $this->desktop->openDialog($message, $onOK, $this->window);
	}

	function postNotification($header, $body = '')
	{
		$this->desktop->notify($header, $body);
	}

	// ---- Settings and the desktop ----

	function getSettings()
	{
		return $this->desktop->settings;
	}

	// Returns true, or shows the problem to the user and returns false.
	function saveSettings($changes)
	{
		return $this->desktop->saveSettings($changes);
	}

	function setBackground($path)
	{
		return $this->saveSettings(array('background' => $path));
	}

	function addShortcut($path)
	{
		$this->desktop->addShortcut($path);
	}

	// ---- Identity ----

	function getAppID()
	{
		return $this->appID;
	}

	function getUsername()
	{
		return $this->desktop->user;
	}

	// ---- Files ----

	// Full path of a file inside this app's folder, e.g. appFile('icons/16.png').
	function appFile($name)
	{
		return SKYLIGHT_ROOT . '/' . $this->path . $name;
	}

	function listFiles()
	{
		$folder = Storage::userFolder($this->desktop->user) . '/files';
		return is_dir($folder) ? array_values(array_diff(scandir($folder), array('.', '..'))) : array();
	}

	// Returns the text of one of the user's files, or null if there's no such file.
	function readFile($name)
	{
		if (Storage::fileError($name) !== null) {
			return null;
		}
		$path = Storage::userFile($this->desktop->user, $name);
		return is_file($path) ? file_get_contents($path) : null;
	}

	// Returns array('ok' => true) or array('ok' => false, 'error' => '...').
	function writeFile($name, $contents)
	{
		$error = Storage::fileError($name);
		if ($error !== null) {
			return array('ok' => false, 'error' => $error);
		}
		$path = Storage::userFile($this->desktop->user, $name);
		if (!is_dir(dirname($path))) {
			mkdir(dirname($path), 0700, true);
		}
		if (file_put_contents($path, $contents, LOCK_EX) === false) {
			return array('ok' => false, 'error' => 'The file could not be saved.');
		}
		return array('ok' => true);
	}
}
