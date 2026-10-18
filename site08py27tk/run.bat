@echo off
rem Starts Skylight OS with Python 2.7. If pythonw.exe is not on your PATH,
rem set PYTHONW to its full path first, for example:
rem     set PYTHONW=C:\Python27\pythonw.exe
if "%PYTHONW%"=="" set PYTHONW=pythonw.exe
cd /d "%~dp0"
start "" "%PYTHONW%" start-skylight.pyw
