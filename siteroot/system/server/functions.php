<?php


function get_mime_type($filename, $mimePath = 'mime.types') {
   $fileext = substr(strrchr($filename, '.'), 1);
   if (empty($fileext)) return (false);
   $regex = "/^([\w\+\-\.\/]+)\s+(\w+\s)*($fileext\s)/i";
   $lines = file($mimePath);
   foreach($lines as $line) {
      if (substr($line, 0, 1) == '#') continue; // skip comments
      $line = rtrim($line) . " ";
      if (!preg_match($regex, $line, $matches)) continue; // no match to the extension
      return ($matches[1]);
   }
   return (false); // no match at all
} 

?>