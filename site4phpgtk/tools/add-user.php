<?php
// Adds a user, or changes an existing user's password.
// Usage: php tools/add-user.php <username> <password>
require dirname(__FILE__) . '/../lib/Storage.php';

if ($argc !== 3 || !Storage::isValidUsername($argv[1]) || $argv[2] === '') {
	exit("Usage: php tools/add-user.php <username> <password>\n" .
		"Usernames may use letters, digits, '-' and '_' (up to 32 characters).\n");
}

$existed = array_key_exists($argv[1], Storage::loadUsers());
if (!Storage::setPassword($argv[1], $argv[2])) {
	exit('Could not write ' . SKYLIGHT_DATA . "/users.json\n");
}
echo ($existed ? 'Changed the password for ' : 'Added user ') . $argv[1] . "\n";
