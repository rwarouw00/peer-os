<?php
/*
 * Shared server code for Skylight OS. Kept outside the web root.
 * Written for PHP 5.3.7 and later (no short arrays, no ?? operator).
 */

define('SKYLIGHT_DATA', dirname(dirname(__FILE__)) . '/data');

// ---- Replies ----

function reply($data) {
	header('Content-Type: application/json; charset=utf-8');
	echo json_encode($data);
	exit;
}

function fail($message) {
	reply(array('ok' => false, 'error' => $message));
}

// ---- Requests ----

function input($name) {
	$value = isset($_POST[$name]) ? $_POST[$name] : (isset($_GET[$name]) ? $_GET[$name] : '');
	if (!is_string($value)) {
		return '';
	}
	// Old PHP 5 installs may still add "magic quotes" to input.
	if (version_compare(PHP_VERSION, '5.4.0', '<') && get_magic_quotes_gpc()) {
		$value = stripslashes($value);
	}
	return $value;
}

function is_post() {
	return $_SERVER['REQUEST_METHOD'] === 'POST';
}

// Changes must be POSTed by Skylight's own scripts. Browsers won't send this
// header from another site's page without asking first, which blocks forged posts.
function require_post() {
	if (!is_post() || !isset($_SERVER['HTTP_X_REQUESTED_WITH']) || $_SERVER['HTTP_X_REQUESTED_WITH'] !== 'Skylight') {
		fail('Bad request.');
	}
}

// ---- Sessions ----

function start_session() {
	if (session_id() === '') {
		ini_set('session.cookie_httponly', 1);
		ini_set('session.use_only_cookies', 1);
		session_name('skylight');
		session_start();
	}
}

function current_user() {
	start_session();
	return isset($_SESSION['user']) ? $_SESSION['user'] : null;
}

function require_user() {
	$user = current_user();
	if ($user === null) {
		fail('You are not logged in.');
	}
	return $user;
}

// ---- Data files (JSON, under data/) ----

function read_json($path, $default) {
	if (!is_file($path)) {
		return $default;
	}
	$data = json_decode(file_get_contents($path), true);
	return is_array($data) ? $data : $default;
}

function write_json($path, $data) {
	if (!is_dir(dirname($path))) {
		mkdir(dirname($path), 0700, true);
	}
	return file_put_contents($path, json_encode($data), LOCK_EX) !== false;
}

// ---- Users ----

function is_valid_username($name) {
	return preg_match('/^[A-Za-z0-9_-]{1,32}$/', $name) === 1;
}

function load_users() {
	return read_json(SKYLIGHT_DATA . '/users.json', array());
}

function user_folder($user) {
	return SKYLIGHT_DATA . '/users/' . $user;
}

function random_salt() {
	$bytes = function_exists('openssl_random_pseudo_bytes') ? openssl_random_pseudo_bytes(16) : '';
	for ($i = strlen($bytes); $i < 16; $i++) {
		$bytes .= chr(mt_rand(0, 255));
	}
	return substr(strtr(base64_encode($bytes), '+', '.'), 0, 22);
}

// Blowfish hashes via crypt(), available from PHP 5.3.7.
function hash_password($password) {
	return crypt($password, '$2y$10$' . random_salt());
}

function check_password($password, $hash) {
	return is_string($hash) && strlen($hash) === 60 && crypt($password, $hash) === $hash;
}

// ---- Settings ----

function default_settings() {
	return array(
		'background' => 'system/images/backgrounds/3.jpg',
		'notificationTime' => 7000,
		'shortcuts' => array()
	);
}

function load_settings($user) {
	return array_merge(default_settings(), read_json(user_folder($user) . '/settings.json', array()));
}

// Checks one change sent by the browser. Returns the clean value, or calls fail().
function clean_setting($name, $value) {
	$app_path = '#^[A-Za-z0-9_/-]+/$#';
	if ($name === 'background' && is_string($value) && preg_match('#^[A-Za-z0-9_/-]+\.(jpg|jpeg|png|gif)$#i', $value)) {
		return $value;
	}
	if ($name === 'notificationTime' && is_numeric($value) && $value >= 1000 && $value <= 60000) {
		return (int) $value;
	}
	if ($name === 'shortcuts' && is_array($value) && count($value) <= 50) {
		$clean = array();
		foreach ($value as $shortcut) {
			if (!is_array($shortcut) || !isset($shortcut['path'], $shortcut['left'], $shortcut['top'])
					|| !is_string($shortcut['path']) || !preg_match($app_path, $shortcut['path'])) {
				fail('Bad shortcut.');
			}
			$clean[] = array('path' => $shortcut['path'], 'left' => (int) $shortcut['left'], 'top' => (int) $shortcut['top']);
		}
		return $clean;
	}
	fail('Bad value for ' . $name . '.');
}

// ---- The user's own files (data/users/<name>/files/) ----

function user_file($user, $name) {
	// No slashes and no leading dot, so a name can't leave the user's folder.
	if (!preg_match('/^[A-Za-z0-9_-][A-Za-z0-9_. -]{0,99}$/', $name)) {
		fail('File names may use letters, digits, spaces, "-", "_" and "." (up to 100 characters).');
	}
	return user_folder($user) . '/files/' . $name;
}
