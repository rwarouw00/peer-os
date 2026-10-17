<?php
// The logged-in user's own files.
// GET                 -> { ok, files: [names] }
// GET name            -> { ok, contents }
// POST name, contents -> { ok }
require dirname(__FILE__) . '/../../lib/skylight.php';

$user = require_user();
$name = input('name');

if (is_post()) {
	require_post();
	$path = user_file($user, $name);
	$contents = input('contents');
	if (strlen($contents) > 1048576) {
		fail('Files are limited to 1 MB.');
	}
	if (!is_dir(dirname($path))) {
		mkdir(dirname($path), 0700, true);
	}
	if (file_put_contents($path, $contents, LOCK_EX) === false) {
		fail('The file could not be saved.');
	}
	reply(array('ok' => true));
}

if ($name === '') {
	$folder = user_folder($user) . '/files';
	$files = is_dir($folder) ? array_values(array_diff(scandir($folder), array('.', '..'))) : array();
	reply(array('ok' => true, 'files' => $files));
}

$path = user_file($user, $name);
if (!is_file($path)) {
	fail('There is no file called ' . $name . '.');
}
reply(array('ok' => true, 'contents' => file_get_contents($path)));
