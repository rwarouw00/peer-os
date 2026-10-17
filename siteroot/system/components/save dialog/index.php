<html>
<head>
<link rel="stylesheet" href="../../../system/css/ui.css"/>
<link rel="stylesheet" href="css/custom.css"/>
<script src="../../../system/js/app.js"></script>
<script src="js/custom.js"></script>
</head>
<body>
<span class="barEndLeft" onclick="history.go(-1);"><</span><span class="barEndRight" onclick="history.go(1);">></span>
<div id="fileContainer">
<?php

require('../../../system/server/functions.php');

if ($_GET['path'] === null) {
	$dir = '../../../';
} else {
	$dir = $_GET['path'];
}

$files = scandir($dir);
$mimeXML = file_get_contents('../../../system/server/mime-types.xml');

$dom = new DOMDocument;	

$dom->loadXML($mimeXML);
$entries = $dom->getElementsByTagName('mime');
$t1 = false;
$icon = '<img src="../../../system/images/mime/file.png" class="fileIcon"/>';

foreach ($files as $file) {
	
	$mime = get_mime_type($file, '../../../system/server/mime.types');
	
	foreach($entries as $entry) {
		if ($mime == $entry->getAttribute('value')) {
			$icon = '<img src="../../../system/images/mime/' . $entry->getAttribute('icon') . '" class="fileIcon"/>';
			$t1 = true;
		}
	}
	
	if (is_dir($dir . $file)) {
		$icon = '<img src="../../../system/images/mime/folder.png" class="fileIcon"/>';
		$t1 = true;
	}
	
	if (is_executable($dir . $file)) {
		$icon = '<img src="../../../system/images/mime/executable.png" class="fileIcon"/>';
		$t1 = true;
	}
	
	if ($t1 == false) {
		$icon = '<img src="../../../system/images/mime/file.png" class="fileIcon"/>';
	}
	
	$t1 = false;
	
	if ($file != '.' && $file != '..') {
		if (is_dir($dir . $file)) {
			echo '<div class="file" data-file="' . $file . '" onclick="App.gotoLocation(\'index.php\', \'path=' . $dir . $file . '/\');">' . $icon . $file . '</div>';
		} else {
			echo '<div class="file" data-file="' . $file . '" onclick="selectFile(\'' . $dir . '\', \'' . $file . '\');">' . $icon . $file . '</div>';
		}
	}
}
?>
</div>
<div id="fileNameInputContainer">File Name: <input type="text" id="fileNameInput"/></div>
<div id="buttonContainer">
<button id="saveButton">Save</button>
<button id="cancelButton">Cancel</button>
</div>
</body>
</html>
