@echo off
rem Starts Skylight OS with PHP-GTK2. If php-win.exe is not on your PATH,
rem set PHP to its full path first, for example:
rem     set PHP=C:\php-gtk2\php-win.exe
if "%PHP%"=="" set PHP=php-win.exe
cd /d "%~dp0"
start "" "%PHP%" skylight.php
