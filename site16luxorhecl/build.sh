#!/bin/sh
# Builds skylight.jar, Skylight and Hecl (hecl/src), with a Java 8 JDK.
# Run tools/get-libs.sh first, to download and build Luxor into lib/.
set -e
cd "$(dirname "$0")"
if [ ! -f lib/luxor-1.0-b8-skylight.jar ]; then
	echo "Luxor isn't in lib/ yet: run tools/get-libs.sh first." >&2
	exit 1
fi
CP=$(ls lib/*.jar | tr '\n' ':')
rm -rf build && mkdir build
javac -encoding UTF-8 -source 8 -target 8 -nowarn -cp "$CP" -d build $(find src hecl/src -name '*.java') 2>&1 | grep -v '^Note:' || true
[ -f build/skylight/Main.class ] || exit 1
# skylight.jar finds the libraries in lib/ through its manifest's Class-Path.
printf 'Main-Class: skylight.Main\nClass-Path: %s\n' "$(ls lib/*.jar | tr '\n' ' ')" > build/MANIFEST.MF
jar cfm skylight.jar build/MANIFEST.MF -C build skylight -C build org -C hecl LICENSE -C hecl NOTICE
rm -rf build
echo "Built skylight.jar"
