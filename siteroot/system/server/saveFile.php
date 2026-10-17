<?php

$filename = $_POST['file'];
$contents = $_POST['contents'];
$username = $_POST['user'];
$cookie = $_COOKIE['Skylight_OS_Auth'];
$dom = new DOMDocument;
$file = file_get_contents('../LoggedInUsers.xml');

$dom->loadXML($file);

$user = $dom->getElementsByTagName($username)->item(0)->nodeValue;

if ($user == $cookie) {
	file_put_contents('../../' . $filename, $contents);
	echo '{"error": "none"}';
} else {
	echo '{"error": "Operation not performed by a logged in user"}';
}