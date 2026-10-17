#!/bin/sh
# Builds Skylight.exe and AddUser.exe with Mono, held to C# 5 like Windows' own csc.exe.
set -e
cd "$(dirname "$0")"
REFS="-r:System.Windows.Forms.dll -r:System.Drawing.dll -r:System.Web.Extensions.dll"
mcs -langversion:5 -nologo -target:winexe -out:Skylight.exe $REFS -recurse:'src/Skylight/*.cs'
mcs -langversion:5 -nologo -target:exe -out:AddUser.exe -r:System.Web.Extensions.dll src/AddUser/AddUser.cs src/Skylight/Storage.cs src/Skylight/Settings.cs
echo "Built Skylight.exe and AddUser.exe"
