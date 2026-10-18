#!/bin/sh
# Starts Skylight OS with Python 2.7.
cd "$(dirname "$0")"
exec "${PYTHON:-python2.7}" start-skylight.pyw
