<?php
// Adds a user, or changes an existing user's password.
// Usage: php tools/add-user.php <username> <password>
require dirname(__FILE__) . '/../lib/skylight.php';

if (PHP_SAPI !== 'cli') {
	exit("Run this from the command line.\n");
}
if ($argc !== 3 || !is_valid_username($argv[1]) || $argv[2] === '') {
	exit("Usage: php tools/add-user.php <username> <password>\n" .
		"Usernames may use letters, digits, '-' and '_' (up to 32 characters).\n");
}

$users = load_users();
$existed = isset($users[$argv[1]]);
$users[$argv[1]] = array('password' => hash_password($argv[2]));

if (!write_json(SKYLIGHT_DATA . '/users.json', $users)) {
	exit("Could not write " . SKYLIGHT_DATA . "/users.json\n");
}
echo ($existed ? 'Changed the password for ' : 'Added user ') . $argv[1] . "\n";
