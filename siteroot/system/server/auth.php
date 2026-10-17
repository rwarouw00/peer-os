<?php
$username = $_POST['username'];
$password = $_POST['password'];

$dom = new DOMDocument;
$dom2 = new DOMDocument;
$file = file_get_contents('../settings/users.xml');
$file2 = file_get_contents('../LoggedInUsers.xml');

$dom->loadXML($file);
$users = $dom->getElementsByTagName('user');
$usersLength = $users->length;
$i = 0;
$skip = false;

foreach ($users as $user) {
	if ($user->getAttribute('username') == $username) {
		if ($user->getAttribute('password') == $password) {
			$sha1 = sha1(time());
			setcookie("Skylight_OS_Auth", $sha1, 0 , '/', $_SERVER['HTTP_HOST'], false, false);
			$dom2->loadXML($file2);
			$users2 = $dom2->getElementsByTagName('users');
			$newUser = $dom2->createElement($username, $sha1);
			$users2->item(0)->appendChild($newUser);
			file_put_contents('../LoggedInUsers.xml', $dom2->saveXML());
			echo '{"error": "none"}';
			$error = 0;
			$skip = true;
		} else {
			$error = 1;
		}
		
	}
	// TODO: check if this loop is the last time, and then send an error if is and there have been no matches if the
	// above if statement.
	if ($i == $usersLength - 1 && $skip == false) {
		$error = 1;
	}
	
	$i += 1;
}

if ($error != 0) { echo '{"error": "true"}';}

?>