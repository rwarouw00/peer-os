<?php
/*
 * Web Browser: PHP-GTK2 has no web page widget, so this opens addresses in
 * the computer's default browser.
 */
class WebBrowserApp extends App
{
	private $address;

	function build()
	{
		$this->address = new GtkEntry();
		$this->address->set_text('https://');
		$this->address->connect_simple('activate', array($this, 'go'));
		$go = new GtkButton('Open');
		$go->connect_simple('clicked', array($this, 'go'));

		$row = new GtkHBox(false, 6);
		$row->pack_start($this->address);
		$row->pack_start($go, false, false);

		$note = new GtkLabel('Pages open in your default web browser.');
		$note->set_alignment(0, 0.5);

		$box = new GtkVBox(false, 10);
		$box->set_border_width(12);
		$box->pack_start($row, false, false);
		$box->pack_start($note, false, false);
		$this->window->add($box);
	}

	function go()
	{
		$url = trim($this->address->get_text());
		if (!preg_match('#^https?://#i', $url)) {
			$url = 'http://' . $url;
		}
		// Only plain web addresses: no spaces, quotes or % signs that a shell could misread.
		if (!preg_match('#^https?://[A-Za-z0-9._~:/?\#\[\]@!$&\'()*+,;=-]+$#', $url)) {
			$this->openDialog('That doesn\'t look like a web address.');
			return;
		}
		if (strtoupper(substr(PHP_OS, 0, 3)) == 'WIN') {
			pclose(popen('start "" "' . $url . '"', 'r'));
		} elseif (PHP_OS == 'Darwin') {
			exec('open ' . escapeshellarg($url) . ' > /dev/null 2>&1 &');
		} else {
			exec('xdg-open ' . escapeshellarg($url) . ' > /dev/null 2>&1 &');
		}
		$this->postNotification('Web Browser', 'Opened ' . $url);
	}
}
