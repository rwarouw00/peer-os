# Shared by push.ps1, push-app.ps1 and add-user.ps1: talking to CouchDB from PowerShell.
# Works with Windows PowerShell 5.1 (built into Windows 10 and 11) and PowerShell 7.

function Connect-Couch([string]$Url) {
	# "http://admin:password@127.0.0.1:5984" -> the server address and a login header
	$uri = [Uri]$Url.TrimEnd('/')
	$script:CouchServer = '{0}://{1}:{2}' -f $uri.Scheme, $uri.Host, $uri.Port
	$script:CouchHeaders = @{}
	if ($uri.UserInfo) {
		$login = [Uri]::UnescapeDataString($uri.UserInfo)
		$script:CouchHeaders.Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($login))
	}
	return ($script:CouchServer + $uri.AbsolutePath).TrimEnd('/')
}

# A CouchDB request. Returns @{ Status = 201; Data = <the reply, parsed> }, also for errors.
function Invoke-Couch([string]$Method, [string]$Path, $Body = $null) {
	$request = @{ Uri = $script:CouchServer + $Path; Method = $Method; Headers = $script:CouchHeaders; UseBasicParsing = $true }
	if ($Body -ne $null) {
		if ($Body -isnot [string]) {
			$Body = ConvertTo-Json $Body -Depth 20 -Compress
		}
		$request.Body = [Text.Encoding]::UTF8.GetBytes($Body)
		$request.ContentType = 'application/json'
	}
	try {
		$reply = Invoke-WebRequest @request
		$status = [int]$reply.StatusCode
		$text = $reply.Content
	} catch {
		if ($_.Exception.Response -eq $null) {
			throw "CouchDB could not be reached at $($script:CouchServer)."
		}
		$status = [int]$_.Exception.Response.StatusCode
		$text = $_.ErrorDetails.Message
	}
	$data = $null
	if ($text) {
		if ($text -is [byte[]]) {
			$text = [Text.Encoding]::UTF8.GetString($text)
		}
		try { $data = ConvertFrom-Json $text } catch { }
	}
	return @{ Status = $status; Data = $data }
}

function Get-Revision([string]$Path) {
	$reply = Invoke-Couch GET $Path
	if ($reply.Status -eq 200) { return $reply.Data._rev }
	return $null
}

# Stops with CouchDB's reason unless the reply was a success.
function Assert-Ok($Reply) {
	if ($Reply.Status -ge 300) {
		$reason = if ($Reply.Data) { "$($Reply.Data.error): $($Reply.Data.reason)" } else { "HTTP $($Reply.Status)" }
		throw "CouchDB said: $reason"
	}
}

# The attachments for every file in a folder (names use "/"), except those listed in $Skip.
function Get-Attachments([string]$Folder, [string[]]$Skip = @()) {
	$types = @{ '.html' = 'text/html; charset=utf-8'; '.js' = 'application/javascript; charset=utf-8';
		'.css' = 'text/css; charset=utf-8'; '.json' = 'application/json'; '.png' = 'image/png';
		'.jpg' = 'image/jpeg'; '.jpeg' = 'image/jpeg'; '.gif' = 'image/gif' }
	$attachments = @{}
	foreach ($file in Get-ChildItem $Folder -Recurse -File) {
		$name = $file.FullName.Substring($Folder.Length + 1).Replace('\', '/')
		if ($Skip -contains $name) { continue }
		$type = $types[$file.Extension.ToLower()]
		if (-not $type) { $type = 'application/octet-stream' }
		$attachments[$name] = @{ content_type = $type; data = [Convert]::ToBase64String([IO.File]::ReadAllBytes($file.FullName)) }
	}
	return $attachments
}
