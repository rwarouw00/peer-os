# Uploads apps to a CouchDB app store (Windows PowerShell 5.1 or later).
# Usage: powershell -ExecutionPolicy Bypass -File tools\push-apps.ps1 http://admin:password@127.0.0.1:5984/skylight-apps [app ...]
#
# Each folder under apps\ becomes one document, named after the folder, with
# every file in it as an attachment. apps\apps.txt (the home menu) and the
# store's rule (only administrators may write) go in _design/store.
param([Parameter(Mandatory = $true)][string]$Url, [Parameter(ValueFromRemainingArguments = $true)][string[]]$Apps)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'couch.ps1')
$root = Split-Path $PSScriptRoot -Parent
$public = Connect-Couch $Url
$store = ([Uri]$public).AbsolutePath.TrimEnd('/')    # e.g. /skylight-apps

$session = (Invoke-Couch GET '/_session').Data
if (-not $session -or $session.userCtx.roles -notcontains '_admin') {
	throw "Can't log in to $public as a CouchDB administrator."
}
Invoke-Couch PUT $store | Out-Null    # 412 "file_exists" is fine

$types = @{ '.fxml' = 'application/xml; charset=utf-8'; '.js' = 'application/javascript; charset=utf-8';
	'.properties' = 'text/plain; charset=iso-8859-1'; '.txt' = 'text/plain; charset=iso-8859-1';
	'.css' = 'text/css; charset=utf-8'; '.png' = 'image/png'; '.jpg' = 'image/jpeg'; '.jpeg' = 'image/jpeg';
	'.gif' = 'image/gif'; '.jar' = 'application/java-archive' }

function Get-Attachment([string]$File) {
	$type = $types[[IO.Path]::GetExtension($File).ToLower()]
	if (-not $type) { $type = 'application/octet-stream' }
	return @{ content_type = $type; data = [Convert]::ToBase64String([IO.File]::ReadAllBytes($File)) }
}

function Save-Document([string]$Path, [hashtable]$Doc) {
	$rev = Get-Revision $Path
	if ($rev) { $Doc._rev = $rev }
	Assert-Ok (Invoke-Couch PUT $Path $Doc)
}

# _design/store: the rule, and the home menu's list.
Save-Document "$store/_design/store" @{
	validate_doc_update = [IO.File]::ReadAllText([IO.Path]::Combine($root, 'store', 'validate_doc_update.js'))
	_attachments = @{ 'apps.txt' = (Get-Attachment ([IO.Path]::Combine($root, 'apps', 'apps.txt'))) }
}

# One document per app.
$appsFolder = Join-Path $root 'apps'
if (-not $Apps) {
	$Apps = Get-ChildItem $appsFolder -Directory | ForEach-Object { $_.Name }
}
foreach ($app in $Apps) {
	$folder = Join-Path $appsFolder $app
	$manifest = Join-Path $folder 'app.properties'
	if (-not (Test-Path $manifest)) {
		Write-Warning "apps\$app\ has no app.properties; skipped."
		continue
	}
	$title = (Get-Content $manifest | Where-Object { $_ -like 'title=*' } | Select-Object -First 1) -replace '^title=', ''
	$attachments = @{}
	foreach ($file in Get-ChildItem $folder -Recurse -File) {
		$name = $file.FullName.Substring($folder.Length + 1).Replace('\', '/')
		if ($name -like 'src/*' -or $name -like 'classes/*') { continue }    # sources and build output stay behind
		$attachments[$name] = Get-Attachment $file.FullName
	}
	Save-Document "$store/$app" @{ type = 'app'; title = $title; _attachments = $attachments }
	Write-Output "Uploaded $app"
}
Write-Output "The app store is ready: $public/"
