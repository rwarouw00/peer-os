#!/bin/sh
# Starts Skylight OS. Run build.sh first.
cd "$(dirname "$0")"
exec java -jar skylight.jar
