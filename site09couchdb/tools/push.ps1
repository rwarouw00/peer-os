# Installs or updates Skylight OS in CouchDB 2.3.x (Windows PowerShell 5.1 or later).
# Usage: powershell -ExecutionPolicy Bypass -File tools\push.ps1 http://admin:password@127.0.0.1:5984
#
# Creates the "skylight" database, turns on couch_peruser (a private database
# for each user), and uploads the design document _design/skylight with every
# file under attachments\ as an attachment.
param([Parameter(Mandatory = $true)][string]$Url)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'couch.ps1')
$root = Split-Path $PSScriptRoot -Parent
$public = Connect-Couch $Url

# Must be a CouchDB administrator.
$session = (Invoke-Couch GET '/_session').Data
if (-not $session -or $session.userCtx.roles -notcontains '_admin') {
	throw "Can't log in to $public as a CouchDB administrator."
}

# Every user gets a private database, userdb-<name in hex>.
$node = (Invoke-Couch GET '/_membership').Data.all_nodes[0]
Assert-Ok (Invoke-Couch PUT "/_node/$node/_config/couch_peruser/enable" '"true"')
Invoke-Couch PUT '/_users' | Out-Null      # these exist already after CouchDB's set-up;
Invoke-Couch PUT '/skylight' | Out-Null    # 412 "file_exists" is fine

# Only administrators may create users (CouchDB otherwise allows sign-ups).
$doc = @{ validate_doc_update = [IO.File]::ReadAllText([IO.Path]::Combine($root, 'users', 'validate_doc_update.js')) }
$rev = Get-Revision '/_users/_design/skylight'
if ($rev) { $doc._rev = $rev }
Assert-Ok (Invoke-Couch PUT '/_users/_design/skylight' $doc)

# The design document, with every file under attachments\.
$types = @{ '.html' = 'text/html; charset=utf-8'; '.js' = 'application/javascript; charset=utf-8';
	'.css' = 'text/css; charset=utf-8'; '.json' = 'application/json'; '.png' = 'image/png';
	'.jpg' = 'image/jpeg'; '.jpeg' = 'image/jpeg'; '.gif' = 'image/gif' }
$attachments = @{}
$folder = Join-Path $root 'attachments'
foreach ($file in Get-ChildItem $folder -Recurse -File) {
	$name = $file.FullName.Substring($folder.Length + 1).Replace('\', '/')
	$type = $types[$file.Extension.ToLower()]
	if (-not $type) { $type = 'application/octet-stream' }
	$attachments[$name] = @{ content_type = $type; data = [Convert]::ToBase64String([IO.File]::ReadAllBytes($file.FullName)) }
}
$doc = @{
	_id = '_design/skylight'
	validate_doc_update = [IO.File]::ReadAllText([IO.Path]::Combine($root, 'design', 'validate_doc_update.js'))
	rewrites = @(ConvertFrom-Json ([IO.File]::ReadAllText([IO.Path]::Combine($root, 'design', 'rewrites.json'))))
	_attachments = $attachments
}
$rev = Get-Revision '/skylight/_design/skylight'
if ($rev) { $doc._rev = $rev }
Assert-Ok (Invoke-Couch PUT '/skylight/_design/skylight' $doc)
Write-Output "Skylight OS is installed: $public/skylight/_design/skylight/_rewrite/"
