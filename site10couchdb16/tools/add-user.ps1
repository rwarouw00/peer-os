# Adds a Skylight OS user, or changes an existing user's password (Windows PowerShell 5.1 or later).
# Usage: powershell -ExecutionPolicy Bypass -File tools\add-user.ps1 http://admin:password@127.0.0.1:5984 <username> <password>
#
# Creates the CouchDB user and their private database, userdb-<name in hex>,
# readable only by them, and installs the checks (validate_doc_update) in it.
param([Parameter(Mandatory = $true)][string]$Url,
	[Parameter(Mandatory = $true)][string]$Name,
	[Parameter(Mandatory = $true)][string]$Password)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'couch.ps1')
$root = Split-Path $PSScriptRoot -Parent
if ($Name -cnotmatch '^[A-Za-z0-9_-]{1,32}$') {
	throw "Usernames may use letters, digits, '-' and '_' (up to 32 characters)."
}
Connect-Couch $Url | Out-Null

$path = "/_users/org.couchdb.user:$Name"
$doc = @{ name = $Name; type = 'user'; roles = @(); password = $Password }
$rev = Get-Revision $path
if ($rev) { $doc._rev = $rev }
Assert-Ok (Invoke-Couch PUT $path $doc)
if ($rev) { Write-Output "Changed the password for $Name" } else { Write-Output "Added user $Name" }

# Their private database: CouchDB 1.6 has no couch_peruser, so we create it.
$db = '/userdb-' + (-join ([Text.Encoding]::UTF8.GetBytes($Name) | ForEach-Object { $_.ToString('x2') }))
Invoke-Couch PUT $db | Out-Null    # 412 "file_exists" is fine for an existing user
Assert-Ok (Invoke-Couch PUT "$db/_security" @{ admins = @{ names = @(); roles = @() }; members = @{ names = @($Name); roles = @() } })

$doc = @{ validate_doc_update = [IO.File]::ReadAllText([IO.Path]::Combine($root, 'userdb', 'validate_doc_update.js')) }
$rev = Get-Revision "$db/_design/skylight"
if ($rev) { $doc._rev = $rev }
Assert-Ok (Invoke-Couch PUT "$db/_design/skylight" $doc)
Write-Output "Their database is ready: $($db.Substring(1))"
