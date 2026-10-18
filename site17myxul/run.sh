#!/bin/sh
# Starts myxul: the launcher, or the XUL file given (and its arguments).
#   ./run.sh                                     the launcher (ui/launcher.xul)
#   ./run.sh apps/leafpad/leafpad.xul notes.txt  Leafpad XUL, opening notes.txt
exec java -jar "$(dirname "$0")/myxul.jar" "$@"
