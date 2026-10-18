#!/bin/sh
# Builds myxul.jar with a Java 8 JDK that includes JavaFX. Nothing else is needed.
set -e
cd "$(dirname "$0")"
rm -rf build && mkdir build
javac -encoding UTF-8 -source 8 -target 8 -Xlint:-options -d build $(find src -name '*.java')
cp src/myxul/bootstrap.js src/myxul/myxul.css build/myxul/
jar cfe myxul.jar myxul.Main -C build .
rm -rf build
echo "Built myxul.jar"
