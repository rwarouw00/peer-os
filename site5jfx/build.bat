@echo off
rem Builds skylight.jar with a Java 8 JDK that includes JavaFX.
cd /d "%~dp0"
if exist build rmdir /s /q build
mkdir build
dir /s /b src\*.java > build\sources.txt
javac -encoding UTF-8 -source 8 -target 8 -d build @build\sources.txt || exit /b 1
copy src\skylight\skylight.css build\skylight\ > nul
del build\sources.txt
jar cfe skylight.jar skylight.Main -C build .
rmdir /s /q build
echo Built skylight.jar
