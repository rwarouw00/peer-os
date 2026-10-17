#!/bin/bash
# Installs or updates Skylight OS in CouchDB 2.3.x.
# Usage: tools/push.sh http://admin:password@127.0.0.1:5984
#
# Creates the "skylight" database, turns on couch_peruser (a private database
# for each user), and uploads the design document _design/skylight with every
# file under attachments/ as an attachment. Needs bash, curl and base64.
set -e
COUCH="${1%/}"
if [ -z "$COUCH" ]; then
	echo "Usage: $0 http://admin:password@127.0.0.1:5984" >&2
	exit 1
fi
cd "$(dirname "$0")/.."

# A file's text as a JSON string.
json_string() {
	printf '"'
	sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' -e 's/\t/\\t/g' -e 's/\r$//' "$1" | awk 'NR > 1 { printf "\\n" } { printf "%s", $0 }'
	printf '"'
}

content_type() {
	case "$1" in
		*.html) echo 'text/html; charset=utf-8' ;;
		*.js) echo 'application/javascript; charset=utf-8' ;;
		*.css) echo 'text/css; charset=utf-8' ;;
		*.json) echo 'application/json' ;;
		*.png) echo 'image/png' ;;
		*.jpg|*.jpeg) echo 'image/jpeg' ;;
		*.gif) echo 'image/gif' ;;
		*) echo 'application/octet-stream' ;;
	esac
}

# Must be a CouchDB administrator.
if ! curl -sf "$COUCH/_session" | grep -q '"_admin"'; then
	echo "Can't log in to $COUCH as a CouchDB administrator." >&2
	exit 1
fi

# Every user gets a private database, userdb-<name in hex>.
NODE=$(curl -sf "$COUCH/_membership" | sed 's/.*"all_nodes":\["\([^"]*\)".*/\1/')
curl -sf -X PUT "$COUCH/_node/$NODE/_config/couch_peruser/enable" -d '"true"' > /dev/null
curl -s -X PUT "$COUCH/_users" > /dev/null      # these exist already after CouchDB's set-up;
curl -s -X PUT "$COUCH/skylight" > /dev/null    # 412 "file_exists" is fine

# Only administrators may create users (CouchDB otherwise allows sign-ups).
REV=$(curl -s "$COUCH/_users/_design/skylight" | sed -n 's/.*"_rev":"\([^"]*\)".*/\1/p')
REPLY=$(curl -s -X PUT "$COUCH/_users/_design/skylight" -H 'Content-Type: application/json' \
	--data-binary "{${REV:+\"_rev\":\"$REV\",}\"validate_doc_update\":$(json_string users/validate_doc_update.js)}")
case "$REPLY" in
	*'"ok":true'*) ;;
	*) echo "CouchDB said: $REPLY" >&2; exit 1 ;;
esac

# The design document, with the current revision if it's an update.
DOC=$(mktemp)
trap 'rm -f "$DOC"' EXIT
REV=$(curl -s "$COUCH/skylight/_design/skylight" | sed -n 's/.*"_rev":"\([^"]*\)".*/\1/p')
{
	printf '{"_id":"_design/skylight",'
	[ -n "$REV" ] && printf '"_rev":"%s",' "$REV"
	printf '"validate_doc_update":'; json_string design/validate_doc_update.js
	printf ',"rewrites":'; cat design/rewrites.json
	printf ',"_attachments":{'
	FIRST=1
	cd attachments
	for FILE in $(find . -type f | sed 's|^\./||' | sort); do
		[ $FIRST = 1 ] || printf ','
		FIRST=0
		printf '"%s":{"content_type":"%s","data":"' "$FILE" "$(content_type "$FILE")"
		base64 < "$FILE" | tr -d '\n'
		printf '"}'
	done
	cd ..
	printf '}}'
} > "$DOC"

REPLY=$(curl -s -X PUT "$COUCH/skylight/_design/skylight" -H 'Content-Type: application/json' --data-binary @"$DOC")
case "$REPLY" in
	*'"ok":true'*) echo "Skylight OS is installed: $(echo "$COUCH" | sed 's|//[^@/]*@|//|')/skylight/_design/skylight/_rewrite/" ;;
	*) echo "CouchDB said: $REPLY" >&2; exit 1 ;;
esac
