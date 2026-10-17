@echo off
rem Builds skylight.jar, Skylight and Hecl (hecl\src), with a Java 8 JDK.
rem Run tools\get-libs.ps1 first, to download and build Luxor into lib\.
setlocal enabledelayedexpansion
cd /d "%~dp0"
if not exist lib\luxor-1.0-b8-skylight.jar (
	echo Luxor isn't in lib\ yet: run tools\get-libs.ps1 first.
	exit /b 1
)
if exist build rmdir /s /q build
mkdir build
dir /s /b src\*.java hecl\src\*.java > build\sources.txt
javac -encoding UTF-8 -source 8 -target 8 -nowarn -cp "lib/*" -d build @build\sources.txt || exit /b 1
rem skylight.jar finds the libraries in lib\ through its manifest's Class-Path.
set CLASSPATH_LINE=
for %%f in (lib\*.jar) do set CLASSPATH_LINE=!CLASSPATH_LINE! lib/%%~nxf
echo Main-Class: skylight.Main> build\MANIFEST.MF
echo Class-Path:!CLASSPATH_LINE!>> build\MANIFEST.MF
jar cfm skylight.jar build\MANIFEST.MF -C build skylight -C build org -C hecl LICENSE -C hecl NOTICE
rmdir /s /q build
echo Built skylight.jar
