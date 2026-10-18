@echo off
rem Starts myxul: the launcher, or the XUL file given (and its arguments).
start "" javaw -jar "%~dp0myxul.jar" %*
