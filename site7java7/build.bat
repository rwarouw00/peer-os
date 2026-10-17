@echo off
rem Builds skylight.jar with a Java 7 JDK (JavaFX 2.2 is in its jre\lib\jfxrt.jar).
rem Set JAVA_HOME to the JDK folder first, for example:
rem   set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_80
cd /d "%~dp0"
call jfxrt.bat || exit /b 1
if exist build rmdir /s /q build
mkdir build
dir /s /b src\*.java > build\sources.txt
"%JAVA_HOME%\bin\javac" -encoding UTF-8 -source 7 -target 7 -cp "%JFXRT%" -d build @build\sources.txt || exit /b 1
copy src\skylight\skylight.css build\skylight\ > nul
del build\sources.txt
"%JAVA_HOME%\bin\jar" cfe skylight.jar skylight.Main -C build .
rmdir /s /q build
echo Built skylight.jar
