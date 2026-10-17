#!/bin/sh
# Shows the layout of every .xul file in Skylight (or in one folder) with
# Luxilla, Luxor's own XUL runner: each window is a tab. Nothing runs: buttons
# do nothing, and Luxilla warns that their commands aren't found.
# Usage: tools/preview.sh [folder]     (default: all of Skylight)
# Icons and images are found from the folder given, so preview from the
# Skylight folder (the default) to see them.
cd "$(dirname "$0")/.."
exec java -cp "lib/*" luxilla "${1:-.}"
