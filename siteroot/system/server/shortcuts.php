<?php

// Shortcut property manager
$JSON = json_decode($_POST['contents'], true);
$dom = new DOMDocument;

if ($JSON['action'] == 'add') {
	$file = file_get_contents('shortcuts.xml');
	$dom->loadXML($file);
	$xml = $dom->getElementsByTagName('shortcuts');
	$shortcut = $dom->createElement('shortcut', $JSON['path']);
	$shortcut->setAttribute('top', $JSON['top']);
	$shortcut->setAttribute('left', $JSON['left']);
	$xml->item(0)->appendChild($shortcut);
	$file = $dom->saveXML($dom);
	file_put_contents('shortcuts.xml', $file);
	echo 'NO ERROR';
} else if ($JSON['action'] == 'edit') {
	$file = file_get_contents('shortcuts.xml');
	$dom->loadXML($file);
	$xml = $dom->getElementsByTagName('shortcut');
	
	foreach ($xml as $xml) {
		
		if ($xml->nodeValue == $JSON['path']) {
			$xml->setAttribute('top', $JSON['top']);
			$xml->setAttribute('left', $JSON['left']);
			//$shortcuts = $dom->getElementsByTagName('shortcuts')->item(0);
			$file = $dom->saveXML($dom);
			file_put_contents('shortcuts.xml', $file);
			echo 'NO ERROR';
		}
	}
}

?>