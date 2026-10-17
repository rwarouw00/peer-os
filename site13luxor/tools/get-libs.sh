#!/bin/sh
# Downloads Luxor XUL 1.0 beta 8 and the libraries it uses (Jython 2.1, JDOM,
# Velocity, ...) from SourceForge into lib/. Needs curl and unzip.
set -e
cd "$(dirname "$0")/.."
URL=https://downloads.sourceforge.net/project/luxor-xul/luxor-libs/Beta%208/luxor-libs-1.0-b8.zip
ZIP=$(mktemp)
trap 'rm -f "$ZIP"' EXIT
echo "Downloading $URL"
curl -fsSL --retry 3 -o "$ZIP" "$URL"
mkdir -p lib
# Everything but the examples' database (hsqldb) and j2h. Luxilla is only for tools/preview.
unzip -o -j -q "$ZIP" -d lib \
	'*/luxor-1.0-b8.jar' '*/jython-2.1.jar' '*/jdom-1.0-b8.jar' '*/jaxen-1.0.jar' '*/saxpath-1.0.jar' \
	'*/velocity-1.3-rc1.jar' '*/collections-2.0.jar' '*/houston-1.0-b1.jar' '*/caramel-1.0-b1.jar' \
	'*/salsa-1.0-b1.jar' '*/apollo-1.0-b1.jar' '*/cypress-1.0-b1.jar' '*/rachel-2.0-b2.jar' '*/luxilla-1.0-b8.jar'
ls lib
