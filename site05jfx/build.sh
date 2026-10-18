#!/bin/sh
# Builds skylight.jar with a Java 8 JDK that includes JavaFX.
set -e
cd "$(dirname "$0")"
rm -rf build && mkdir build
javac -encoding UTF-8 -source 8 -target 8 -d build $(find src -name '*.java')
cp src/skylight/skylight.css build/skylight/
jar cfe skylight.jar skylight.Main -C build .
rm -rf build
echo "Built skylight.jar"
