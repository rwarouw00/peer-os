<?php
// POST username, password -> { ok, user, settings }
require dirname(__FILE__) . '/../../lib/skylight.php';

require_post();
$username = input('username');
$users = load_users();

if (!is_valid_username($username) || !isset($users[$username])
		|| !check_password(input('password'), $users[$username]['password'])) {
	fail('Either the username or password is incorrect.');
}

start_session();
session_regenerate_id(true);
$_SESSION['user'] = $username;
reply(array('ok' => true, 'user' => $username, 'settings' => load_settings($username)));
