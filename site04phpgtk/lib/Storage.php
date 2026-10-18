<?php
/*
 * Skylight OS (PHP-GTK2): users, settings and files, stored as JSON under data/.
 * Plain PHP 5.2: no closures, no namespaces, no __DIR__.
 */

define('SKYLIGHT_ROOT', dirname(dirname(__FILE__)));
define('SKYLIGHT_DATA', SKYLIGHT_ROOT . DIRECTORY_SEPARATOR . 'data');

class Storage
{
	// ---- JSON files ----

	static function readJson($path, $default)
	{
		if (!is_file($path)) {
			return $default;
		}
		$data = json_decode(file_get_contents($path), true);
		return is_array($data) ? $data : $default;
	}

	static function writeJson($path, $data)
	{
		if (!is_dir(dirname($path))) {
			mkdir(dirname($path), 0700, true);
		}
		return file_put_contents($path, json_encode($data), LOCK_EX) !== false;
	}

	// ---- Users ----

	static function isValidUsername($name)
	{
		return preg_match('/^[A-Za-z0-9_-]{1,32}$/', $name) === 1;
	}

	static function loadUsers()
	{
		return Storage::readJson(SKYLIGHT_DATA . '/users.json', array());
	}

	static function userFolder($user)
	{
		return SKYLIGHT_DATA . '/users/' . $user;
	}

	// Returns true if the username and password match a stored user.
	static function checkLogin($username, $password)
	{
		$users = Storage::loadUsers();
		if (!Storage::isValidUsername($username) || !isset($users[$username])) {
			return false;
		}
		return Storage::checkPassword($password, $users[$username]['password']);
	}

	static function setPassword($username, $password)
	{
		$users = Storage::loadUsers();
		$users[$username] = array('password' => Storage::hashPassword($password));
		return Storage::writeJson(SKYLIGHT_DATA . '/users.json', $users);
	}

	// ---- Passwords: PBKDF2-SHA256, built from hash_hmac() so it works on PHP 5.2 ----

	static function pbkdf2($password, $salt, $iterations)
	{
		$block = hash_hmac('sha256', $salt . pack('N', 1), $password, true);
		$result = $block;
		for ($i = 1; $i < $iterations; $i++) {
			$block = hash_hmac('sha256', $block, $password, true);
			$result ^= $block;
		}
		return bin2hex($result);
	}

	static function randomHex($bytes)
	{
		$random = function_exists('openssl_random_pseudo_bytes') ? openssl_random_pseudo_bytes($bytes) : '';
		while (strlen($random) < $bytes) {
			$random .= chr(mt_rand(0, 255));
		}
		return bin2hex($random);
	}

	static function hashPassword($password)
	{
		$salt = Storage::randomHex(16);
		$iterations = 20000;
		return 'pbkdf2-sha256$' . $iterations . '$' . $salt . '$' . Storage::pbkdf2($password, $salt, $iterations);
	}

	static function checkPassword($password, $stored)
	{
		$parts = explode('$', (string) $stored);
		if (count($parts) !== 4 || $parts[0] !== 'pbkdf2-sha256') {
			return false;
		}
		$hash = Storage::pbkdf2($password, $parts[2], (int) $parts[1]);
		// Compare every character, so the time taken doesn't leak how much matched.
		$difference = strlen($hash) ^ strlen($parts[3]);
		for ($i = 0; $i < strlen($hash) && $i < strlen($parts[3]); $i++) {
			$difference |= ord($hash[$i]) ^ ord($parts[3][$i]);
		}
		return $difference === 0;
	}

	// ---- Settings ----

	static function defaultSettings()
	{
		return array(
			'background' => 'images/backgrounds/3.jpg',
			'notificationTime' => 7000,
			'shortcuts' => array()
		);
	}

	static function loadSettings($user)
	{
		return array_merge(Storage::defaultSettings(), Storage::readJson(Storage::userFolder($user) . '/settings.json', array()));
	}

	// Applies $changes after checking each one. Returns the new settings, or an error string.
	static function saveSettings($user, $changes)
	{
		$settings = Storage::loadSettings($user);
		foreach ($changes as $name => $value) {
			$clean = Storage::cleanSetting($name, $value);
			if ($clean === null) {
				return 'Bad value for ' . $name . '.';
			}
			$settings[$name] = $clean;
		}
		if (!Storage::writeJson(Storage::userFolder($user) . '/settings.json', $settings)) {
			return 'The settings could not be saved.';
		}
		return $settings;
	}

	// Returns the checked value, or null if it isn't acceptable.
	static function cleanSetting($name, $value)
	{
		if ($name === 'background' && is_string($value)
				&& preg_match('#^images/backgrounds/[A-Za-z0-9_-]+\.(jpg|jpeg|png)$#i', $value)) {
			return $value;
		}
		if ($name === 'notificationTime' && is_numeric($value) && $value >= 1000 && $value <= 60000) {
			return (int) $value;
		}
		if ($name === 'shortcuts' && is_array($value) && count($value) <= 50) {
			$clean = array();
			foreach ($value as $shortcut) {
				if (!is_array($shortcut) || !isset($shortcut['path']) || !Storage::isAppPath($shortcut['path'])) {
					return null;
				}
				$clean[] = array(
					'path' => $shortcut['path'],
					'x' => isset($shortcut['x']) ? (int) $shortcut['x'] : 20,
					'y' => isset($shortcut['y']) ? (int) $shortcut['y'] : 20
				);
			}
			return $clean;
		}
		return null;
	}

	// ---- Apps ----

	static function isAppPath($path)
	{
		return is_string($path) && preg_match('#^apps/[a-z0-9-]+/$#', $path) === 1;
	}

	// ---- The user's own files (data/users/<name>/files/) ----

	static function fileError($name)
	{
		// No slashes and no leading dot, so a name can't leave the user's folder.
		if (!preg_match('/^[A-Za-z0-9_-][A-Za-z0-9_. -]{0,99}$/', $name)) {
			return 'File names may use letters, digits, spaces, "-", "_" and "." (up to 100 characters).';
		}
		return null;
	}

	static function userFile($user, $name)
	{
		return Storage::userFolder($user) . '/files/' . $name;
	}
}
