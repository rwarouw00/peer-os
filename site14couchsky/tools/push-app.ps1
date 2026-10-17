# Installs or updates apps in Skylight OS, without touching anything else (Windows PowerShell 5.1 or later).
# Usage: powershell -ExecutionPolicy Bypass -File tools\push-app.ps1 http://admin:password@127.0.0.1:5984 notes [app ...]
#
# Each app is a folder in apps\ (apps\notes\ is the app "notes"). It becomes
# one document in the "skylight" database: its app.json as the "manifest"
# field, and every other file in the folder as an attachment.
param(
	[Parameter(Mandatory = $true)][string]$Url,
	[Parameter(Mandatory = $true, ValueFromRemainingArguments = $true)][string[]]$Apps
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'couch.ps1')
$root = Split-Path $PSScriptRoot -Parent
Connect-Couch $Url | Out-Null

foreach ($app in $Apps) {
	$app = $app.TrimEnd('/', '\') -replace '^apps[/\\]', ''
	$folder = [IO.Path]::Combine($root, 'apps', $app)
	$manifest = Join-Path $folder 'app.json'
	if (-not (Test-Path $manifest)) {
		Write-Warning "apps\$app\ has no app.json; skipped."
		continue
	}
	$doc = @{
		_id = $app
		type = 'app'
		manifest = ConvertFrom-Json ([IO.File]::ReadAllText($manifest))
		_attachments = Get-Attachments $folder @('app.json')
	}
	$rev = Get-Revision "/skylight/$app"
	if ($rev) { $doc._rev = $rev }
	Assert-Ok (Invoke-Couch PUT "/skylight/$app" $doc)
	Write-Output "Pushed $app"
}
