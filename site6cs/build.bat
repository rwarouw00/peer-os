@echo off
rem Builds Skylight.exe and AddUser.exe with the C# 5 compiler that comes with
rem Windows (.NET Framework 4.x). No Visual Studio or SDK needed.
cd /d "%~dp0"
set CSC=%WINDIR%\Microsoft.NET\Framework64\v4.0.30319\csc.exe
if not exist "%CSC%" set CSC=%WINDIR%\Microsoft.NET\Framework\v4.0.30319\csc.exe
"%CSC%" /nologo /target:winexe /out:Skylight.exe /reference:System.Web.Extensions.dll /recurse:src\Skylight\*.cs || exit /b 1
"%CSC%" /nologo /target:exe /out:AddUser.exe /reference:System.Web.Extensions.dll src\AddUser\AddUser.cs src\Skylight\Storage.cs src\Skylight\Settings.cs || exit /b 1
echo Built Skylight.exe and AddUser.exe
