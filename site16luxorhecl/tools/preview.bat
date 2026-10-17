@echo off
rem Shows the layout of every .xul file in Skylight (or in one folder) with
rem Luxilla, Luxor's own XUL runner: each window is a tab. Nothing runs.
rem Usage: tools\preview.bat [folder]     (default: all of Skylight)
cd /d "%~dp0\.."
set FOLDER=%1
if "%FOLDER%"=="" set FOLDER=.
java -cp "lib/*" luxilla %FOLDER%
