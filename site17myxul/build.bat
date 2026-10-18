@echo off
rem Builds myxul.jar with a Java 8 JDK that includes JavaFX. Nothing else is needed.
setlocal
cd /d "%~dp0"
if exist build rmdir /s /q build
mkdir build
dir /s /b src\*.java > build\sources.txt
javac -encoding UTF-8 -source 8 -target 8 -Xlint:-options -d build @build\sources.txt || exit /b 1
copy /y src\myxul\bootstrap.js build\myxul\ > nul
copy /y src\myxul\myxul.css build\myxul\ > nul
del build\sources.txt
jar cfe myxul.jar myxul.Main -C build .
rmdir /s /q build
echo Built myxul.jar
