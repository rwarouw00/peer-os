#!/bin/sh
# Starts Skylight OS. Run build.sh first.
cd "$(dirname "$0")"
. ./jfxrt.sh
exec "$JAVA_BIN/java" -cp "skylight.jar${JFXRT:+:$JFXRT}" skylight.Main
