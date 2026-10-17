<?php
// POST -> { ok }
require dirname(__FILE__) . '/../../lib/skylight.php';

require_post();
start_session();
$_SESSION = array();
session_destroy();
reply(array('ok' => true));
