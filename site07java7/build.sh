#!/bin/sh
# Builds skylight.jar with a Java 7 JDK (JavaFX 2.2 is in its jre/lib/jfxrt.jar).
# Set JAVA_HOME to the JDK folder. A Java 8 JDK with JavaFX works too.
set -e
cd "$(dirname "$0")"
. ./jfxrt.sh
rm -rf build && mkdir build
"$JAVA_BIN/javac" -encoding UTF-8 -source 7 -target 7 -cp "$JFXRT" -d build $(find src -name '*.java')
cp src/skylight/skylight.css build/skylight/
"$JAVA_BIN/jar" cfe skylight.jar skylight.Main -C build .
rm -rf build
echo "Built skylight.jar"
