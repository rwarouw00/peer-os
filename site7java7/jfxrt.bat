@echo off
rem Finds JavaFX's jfxrt.jar, for build.bat and run.bat.
rem Java 7 keeps it in jre\lib (not on the class path); Java 8 in jre\lib\ext.
if not defined JAVA_HOME (
	echo Set JAVA_HOME to your JDK folder, for example: set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_80
	exit /b 1
)
set JFXRT=
for %%f in ("%JAVA_HOME%\jre\lib\jfxrt.jar" "%JAVA_HOME%\lib\jfxrt.jar" "%JAVA_HOME%\jre\lib\ext\jfxrt.jar" "%JAVA_HOME%\lib\ext\jfxrt.jar") do (
	if not defined JFXRT if exist %%f set JFXRT=%%~f
)
if not defined JFXRT (
	echo No jfxrt.jar in %JAVA_HOME%. Java 7 update 6 or later includes JavaFX 2.2.
	exit /b 1
)
exit /b 0
