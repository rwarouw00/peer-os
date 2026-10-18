@echo off
rem Starts Skylight OS. Run build.bat first.
cd /d "%~dp0"
call jfxrt.bat || exit /b 1
start "" "%JAVA_HOME%\bin\javaw" -cp "skylight.jar;%JFXRT%" skylight.Main
