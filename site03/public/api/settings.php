<?php
// GET -> { ok, user, settings }
// POST changes (a JSON object of settings to change) -> { ok, user, settings }
require dirname(__FILE__) . '/../../lib/skylight.php';

$user = require_user();
$settings = load_settings($user);

if (is_post()) {
	require_post();
	$changes = json_decode(input('changes'), true);
	if (!is_array($changes)) {
		fail('Bad request.');
	}
	foreach ($changes as $name => $value) {
		$settings[$name] = clean_setting($name, $value);
	}
	if (!write_json(user_folder($user) . '/settings.json', $settings)) {
		fail('The settings could not be saved.');
	}
}

reply(array('ok' => true, 'user' => $user, 'settings' => $settings));
