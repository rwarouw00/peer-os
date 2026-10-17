# Adds a Skylight OS user, or changes an existing user's password (Windows PowerShell 5.1 or later).
# Usage: powershell -ExecutionPolicy Bypass -File tools\add-user.ps1 http://admin:password@127.0.0.1:5984 <username> <password>
#
# Creates the CouchDB user, waits for couch_peruser to create their private
# database, and installs the checks (validate_doc_update) in it.
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

# couch_peruser creates userdb-<name in hex> a moment later.
$db = '/userdb-' + (-join ([Text.Encoding]::UTF8.GetBytes($Name) | ForEach-Object { $_.ToString('x2') }))
for ($try = 0; $try -lt 40 -and (Invoke-Couch GET $db).Status -ne 200; $try++) {
	Start-Sleep -Milliseconds 250
}
if ((Invoke-Couch GET $db).Status -ne 200) {
	throw "The user's database wasn't created. Is couch_peruser enabled? (tools\push.ps1 turns it on.)"
}

# couch_peruser makes the user an admin of their database, which would let
# them remove the checks below. Once it has, keep them as a member only.
for ($try = 0; $try -lt 40; $try++) {
	$security = (Invoke-Couch GET "$db/_security").Data
	if ($security.members.names -contains $Name) { break }
	Start-Sleep -Milliseconds 250
}
Assert-Ok (Invoke-Couch PUT "$db/_security" @{ admins = @{ names = @(); roles = @() }; members = @{ names = @($Name); roles = @() } })

$doc = @{ validate_doc_update = [IO.File]::ReadAllText([IO.Path]::Combine($root, 'userdb', 'validate_doc_update.js')) }
$rev = Get-Revision "$db/_design/skylight"
if ($rev) { $doc._rev = $rev }
Assert-Ok (Invoke-Couch PUT "$db/_design/skylight" $doc)
Write-Output "Their database is ready: $($db.Substring(1))"
