<?php
/*
 * Skylight OS for PHP-GTK2. Start it with:
 *     php-win.exe skylight.php        (Windows, no console window)
 *     php skylight.php                 (anywhere, with a console for error messages)
 */
if (!class_exists('gtk')) {
	if (!extension_loaded('php-gtk')) {
		@dl(strtoupper(substr(PHP_OS, 0, 3)) == 'WIN' ? 'php_gtk2.dll' : 'php_gtk2.so');
	}
	if (!class_exists('gtk')) {
		die("Skylight OS needs the PHP-GTK2 extension. Load it in php.ini (extension=php_gtk2.dll).\n");
	}
}

// All Skylight text is UTF-8; PHP-GTK2 otherwise assumes ISO-8859-1.
ini_set('php-gtk.codepage', 'UTF-8');

// Use the computer's time zone, without PHP's warning when php.ini doesn't set one.
date_default_timezone_set(@date_default_timezone_get());

require dirname(__FILE__) . '/lib/Storage.php';
require dirname(__FILE__) . '/lib/App.php';
require dirname(__FILE__) . '/lib/Desktop.php';

$desktop = new Desktop();
$desktop->start();
if ($desktop->user !== null) {
	Gtk::main();
}
