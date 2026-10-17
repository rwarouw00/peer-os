#!/bin/bash
# Uploads apps to a CouchDB app store (CouchDB 1.6 or later).
# Usage: tools/push-apps.sh http://admin:password@127.0.0.1:5984/skylight-apps [app ...]
#
# Each folder under apps/ becomes one document, named after the folder, with
# every file in it as an attachment: apps/notes/app.fxml is served as
# .../skylight-apps/notes/app.fxml. apps/apps.txt (the home menu) and the
# store's rule (only administrators may write) go in _design/store.
# With app names, only those apps are uploaded. Needs bash, curl and base64.
set -e
STORE="${1%/}"
shift || true
if [ -z "$STORE" ]; then
	echo "Usage: $0 http://admin:password@127.0.0.1:5984/skylight-apps [app ...]" >&2
	exit 1
fi
cd "$(dirname "$0")/.."

json_string() {   # a file's text as a JSON string
	printf '"'
	sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' -e 's/\t/\\t/g' -e 's/\r$//' "$1" | awk 'NR > 1 { printf "\\n" } { printf "%s", $0 }'
	printf '"'
}

content_type() {
	case "$1" in
		*.fxml) echo 'application/xml; charset=utf-8' ;;
		*.js) echo 'application/javascript; charset=utf-8' ;;
		*.properties|*.txt) echo 'text/plain; charset=iso-8859-1' ;;
		*.css) echo 'text/css; charset=utf-8' ;;
		*.png) echo 'image/png' ;;
		*.jpg|*.jpeg) echo 'image/jpeg' ;;
		*.gif) echo 'image/gif' ;;
		*.jar) echo 'application/java-archive' ;;
		*) echo 'application/octet-stream' ;;
	esac
}

revision() {      # the _rev of a document, or nothing
	curl -s "$1" | sed -n 's/^{"_id":"[^"]*","_rev":"\([^"]*\)".*/\1/p'
}

attachment() {    # one "name": {...} entry of _attachments
	printf '"%s":{"content_type":"%s","data":"' "$1" "$(content_type "$1")"
	base64 < "$2" | tr -d '\n'
	printf '"}'
}

put() {           # PUT a JSON file; stop with CouchDB's answer if it fails
	REPLY=$(curl -s -X PUT "$1" -H 'Content-Type: application/json' --data-binary @"$2")
	case "$REPLY" in
		*'"ok":true'*) ;;
		*) echo "CouchDB said: $REPLY" >&2; exit 1 ;;
	esac
}

# Must be an administrator; the store is created if it doesn't exist.
SERVER="${STORE%/*}"
if ! curl -sf "$SERVER/_session" | grep -q '"_admin"'; then
	echo "Can't log in to $SERVER as a CouchDB administrator." >&2
	exit 1
fi
curl -s -X PUT "$STORE" > /dev/null    # 412 "file_exists" is fine

DOC=$(mktemp)
trap 'rm -f "$DOC"' EXIT

# _design/store: the rule, and the home menu's list.
REV=$(revision "$STORE/_design/store")
{
	printf '{"_id":"_design/store",'
	[ -n "$REV" ] && printf '"_rev":"%s",' "$REV"
	printf '"validate_doc_update":'; json_string store/validate_doc_update.js
	printf ',"_attachments":{'; attachment apps.txt apps/apps.txt; printf '}}'
} > "$DOC"
put "$STORE/_design/store" "$DOC"

# One document per app.
if [ $# -eq 0 ]; then
	set -- $(cd apps && for d in */; do echo "${d%/}"; done)
fi
for APP in "$@"; do
	if [ ! -f "apps/$APP/app.properties" ]; then
		echo "apps/$APP/ has no app.properties; skipped." >&2
		continue
	fi
	TITLE=$(sed -n 's/^title=//p' "apps/$APP/app.properties" | head -1 | sed 's/\\/\\\\/g; s/"/\\"/g')
	REV=$(revision "$STORE/$APP")
	{
		printf '{"_id":"%s","type":"app","title":"%s",' "$APP" "$TITLE"
		[ -n "$REV" ] && printf '"_rev":"%s",' "$REV"
		printf '"_attachments":{'
		FIRST=1
		# Everything in the folder except sources and build output.
		for FILE in $(cd "apps/$APP" && find . -type f -not -path './src/*' -not -path './classes/*' | sed 's|^\./||' | sort); do
			[ $FIRST = 1 ] || printf ','
			FIRST=0
			attachment "$FILE" "apps/$APP/$FILE"
		done
		printf '}}'
	} > "$DOC"
	put "$STORE/$APP" "$DOC"
	echo "Uploaded $APP"
done
echo "The app store is ready: $(echo "$STORE" | sed 's|//[^@/]*@|//|')/"
