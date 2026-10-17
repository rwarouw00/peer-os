# Downloads Luxor XUL 1.0 beta 8 (its libraries and its source) from
# SourceForge, then builds into lib\ (Windows PowerShell 5.1 or later):
#   luxor-1.0-b8-skylight.jar  Luxor, with Skylight's changes in luxor\ copied over its source
# Hecl, the scripting language, needs no download: its source is in hecl\src,
# and build.bat compiles it into skylight.jar.
# Needs a Java 8 JDK (javac and jar) on the PATH.
# Usage: powershell -ExecutionPolicy Bypass -File tools\get-libs.ps1
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$lib = Join-Path $root 'lib'
$tmp = Join-Path ([IO.Path]::GetTempPath()) ('skylight-libs-' + [Guid]::NewGuid())
New-Item -ItemType Directory -Force $tmp, $lib | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

function Get-Zip([string]$Url, [string]$Name) {
	Write-Host "Downloading $Url"
	$file = Join-Path $tmp $Name
	# SourceForge sends browsers a download page; a download tool's name gets the file itself.
	Invoke-WebRequest -Uri $Url -OutFile $file -UseBasicParsing -UserAgent 'Wget'
	return $file
}

# Copies the named jars out of a zip into lib\.
function Copy-Jars([string]$Zip, [string[]]$Jars) {
	$archive = [IO.Compression.ZipFile]::OpenRead($Zip)
	try {
		foreach ($entry in $archive.Entries) {
			if ($Jars -contains $entry.Name) {
				[IO.Compression.ZipFileExtensions]::ExtractToFile($entry, (Join-Path $lib $entry.Name), $true)
			}
		}
	} finally {
		$archive.Dispose()
	}
}

function Invoke-Tool([string]$Tool, [string[]]$Arguments) {
	& $Tool @Arguments
	if ($LASTEXITCODE -ne 0) { throw "$Tool failed" }
}

try {
	$sf = 'https://downloads.sourceforge.net/project'
	$libs = Get-Zip "$sf/luxor-xul/luxor-libs/Beta%208/luxor-libs-1.0-b8.zip" 'libs.zip'
	$luxor = Get-Zip "$sf/luxor-xul/luxor/Beta%208/luxor-1.0-b8.zip" 'luxor.zip'

	# Luxor's libraries (not Luxor itself, Jython, the examples' hsqldb and j2h).
	Copy-Jars $libs @('houston-1.0-b1.jar', 'cypress-1.0-b1.jar', 'jdom-1.0-b8.jar', 'jaxen-1.0.jar', 'saxpath-1.0.jar',
		'caramel-1.0-b1.jar', 'salsa-1.0-b1.jar', 'apollo-1.0-b1.jar', 'rachel-2.0-b2.jar',
		'velocity-1.3-rc1.jar', 'collections-2.0.jar', 'luxilla-1.0-b8.jar')

	# Luxor, built from its source with Skylight's changed files.
	[IO.Compression.ZipFile]::ExtractToDirectory($luxor, (Join-Path $tmp 'luxor'))
	$src = Join-Path $tmp 'luxor\luxor-1.0-b8\src'
	Copy-Item -Recurse -Force (Join-Path $root 'luxor\src\*') $src
	Remove-Item -Recurse -Force (Join-Path $src 'luxor\python')   # the Jython console: Skylight uses Hecl
	$classes = Join-Path $tmp 'classes'
	New-Item -ItemType Directory $classes | Out-Null
	$sources = Join-Path $tmp 'sources.txt'
	Get-ChildItem $src -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' } | Set-Content $sources
	$cp = (Get-ChildItem $lib -Filter *.jar | Where-Object { $_.Name -notmatch 'luxilla' } | ForEach-Object { $_.FullName }) -join [IO.Path]::PathSeparator
	Invoke-Tool javac @('-nowarn', '-encoding', 'ISO-8859-1', '-source', '8', '-target', '8', '-cp', $cp, '-d', $classes, "@$sources")
	foreach ($file in Get-ChildItem (Join-Path $src 'luxor') -Recurse -File | Where-Object { $_.Extension -ne '.java' }) {
		$name = $file.FullName.Substring($src.Length + 1)
		$target = Join-Path $classes $name
		New-Item -ItemType Directory -Force (Split-Path $target) | Out-Null
		Copy-Item $file.FullName $target
	}
	Copy-Item (Join-Path $src 'VM_global_library.vm') $classes
	Invoke-Tool jar @('cf', (Join-Path $lib 'luxor-1.0-b8-skylight.jar'), '-C', $classes, '.')

	Get-ChildItem $lib -Name
} finally {
	Remove-Item -Recurse -Force $tmp
}
