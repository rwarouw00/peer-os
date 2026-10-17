# Downloads Luxor XUL 1.0 beta 8 and the libraries it uses (Jython 2.1, JDOM,
# Velocity, ...) from SourceForge into lib\ (Windows PowerShell 5.1 or later).
# Usage: powershell -ExecutionPolicy Bypass -File tools\get-libs.ps1
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$url = 'https://downloads.sourceforge.net/project/luxor-xul/luxor-libs/Beta%208/luxor-libs-1.0-b8.zip'
# Everything but the examples' database (hsqldb) and j2h. Luxilla is only for tools\preview.
$jars = @('luxor-1.0-b8.jar', 'jython-2.1.jar', 'jdom-1.0-b8.jar', 'jaxen-1.0.jar', 'saxpath-1.0.jar',
	'velocity-1.3-rc1.jar', 'collections-2.0.jar', 'houston-1.0-b1.jar', 'caramel-1.0-b1.jar',
	'salsa-1.0-b1.jar', 'apollo-1.0-b1.jar', 'cypress-1.0-b1.jar', 'rachel-2.0-b2.jar', 'luxilla-1.0-b8.jar')

$zip = Join-Path ([IO.Path]::GetTempPath()) 'luxor-libs-1.0-b8.zip'
Write-Output "Downloading $url"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
# SourceForge sends browsers a download page; a download tool's name gets the file itself.
Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing -UserAgent 'Wget'
$lib = Join-Path $root 'lib'
New-Item -ItemType Directory -Force $lib | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead($zip)
try {
	foreach ($entry in $archive.Entries) {
		if ($jars -contains $entry.Name) {
			[IO.Compression.ZipFileExtensions]::ExtractToFile($entry, (Join-Path $lib $entry.Name), $true)
		}
	}
} finally {
	$archive.Dispose()
	Remove-Item $zip
}
Get-ChildItem $lib -Name
