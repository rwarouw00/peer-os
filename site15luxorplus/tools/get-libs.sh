#!/bin/sh
# Downloads Luxor XUL 1.0 beta 8 (its libraries and its source) and Jacl 1.3.3
# from SourceForge, then builds into lib/:
#   luxor-1.0-b8-skylight.jar  Luxor, with Skylight's changes in luxor/ copied over its source
#   jacl-java8.jar             the old sun.io classes Jacl 1.3.3 needs, which Java 8 removed (compat/)
# Needs curl, unzip and a Java 8 JDK (javac and jar).
set -e
cd "$(dirname "$0")/.."
SF=https://downloads.sourceforge.net/project
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
get() {   # get <url> <file>
	echo "Downloading $1"
	curl -fsSL --retry 3 -o "$TMP/$2" "$1"
}
get "$SF/luxor-xul/luxor-libs/Beta%208/luxor-libs-1.0-b8.zip" libs.zip
get "$SF/luxor-xul/luxor/Beta%208/luxor-1.0-b8.zip" luxor.zip
get "$SF/tcljava/jacl/1.3.3/jaclBinary133.zip" jacl.zip
mkdir -p lib

# Luxor's libraries (not Luxor itself, Jython, the examples' hsqldb and j2h).
unzip -o -j -q "$TMP/libs.zip" -d lib \
	'*/houston-1.0-b1.jar' '*/cypress-1.0-b1.jar' '*/jdom-1.0-b8.jar' '*/jaxen-1.0.jar' '*/saxpath-1.0.jar' \
	'*/caramel-1.0-b1.jar' '*/salsa-1.0-b1.jar' '*/apollo-1.0-b1.jar' '*/rachel-2.0-b2.jar' \
	'*/velocity-1.3-rc1.jar' '*/collections-2.0.jar' '*/luxilla-1.0-b8.jar'
# Jacl 1.3.3: Tcl for Java.
unzip -o -j -q "$TMP/jacl.zip" -d lib '*/jacl.jar' '*/tcljava.jar'

# Luxor, built from its source with Skylight's changed files.
unzip -q "$TMP/luxor.zip" -d "$TMP/luxor"
SRC="$TMP/luxor/luxor-1.0-b8/src"
cp -R luxor/src/. "$SRC/"
rm -rf "$SRC/luxor/python"   # the Jython console: Skylight uses Tcl
mkdir "$TMP/classes"
find "$SRC" -name '*.java' > "$TMP/sources.txt"
CP=$(ls lib/*.jar | grep -v 'luxilla\|jacl\|tcljava' | tr '\n' ':')
javac -nowarn -encoding ISO-8859-1 -source 8 -target 8 -cp "$CP" -d "$TMP/classes" @"$TMP/sources.txt" 2>&1 | grep -v '^Note:\|^warning:\|^[0-9]* warning' || true
(cd "$SRC" && find luxor -type f ! -name '*.java' | while read -r f; do   # Luxor's images and settings
	mkdir -p "$TMP/classes/$(dirname "$f")" && cp "$f" "$TMP/classes/$f"
done)
cp "$SRC/VM_global_library.vm" "$TMP/classes/"
jar cf lib/luxor-1.0-b8-skylight.jar -C "$TMP/classes" .

# The sun.io classes Jacl 1.3.3 uses, rebuilt on java.nio.charset.
mkdir "$TMP/compat"
javac -nowarn -source 8 -target 8 -d "$TMP/compat" compat/sun/io/*.java 2>&1 | grep -v '^warning:\|^[0-9]* warning' || true
jar cf lib/jacl-java8.jar -C "$TMP/compat" sun
ls lib
