#!/bin/bash
# Installs or updates apps in Skylight OS, without touching anything else.
# Usage: tools/push-app.sh http://admin:password@127.0.0.1:5984 app [app ...]
#
# Each app is a folder in apps/ (apps/notes/ is the app "notes"). It becomes
# one document in the "skylight" database: its app.json as the "manifest"
# field, and every other file in the folder as an attachment. Users get the
# new version the next time they open the app; a new app with a "menu"
# number in its app.json appears in the home menu at their next login.
# Needs bash, curl and base64.
set -e
COUCH="${1%/}"
shift || true
if [ -z "$COUCH" ] || [ $# -eq 0 ]; then
	echo "Usage: $0 http://admin:password@127.0.0.1:5984 app [app ...]" >&2
	exit 1
fi
cd "$(dirname "$0")/.."

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

DOC=$(mktemp)
trap 'rm -f "$DOC"' EXIT
for APP in "$@"; do
	APP="${APP%/}"
	APP="${APP#apps/}"
	if [ ! -f "apps/$APP/app.json" ]; then
		echo "apps/$APP/ has no app.json; skipped." >&2
		continue
	fi
	REV=$(curl -s "$COUCH/skylight/$APP" | sed -n 's/^{"_id":"[^"]*","_rev":"\([^"]*\)".*/\1/p')
	{
		printf '{"_id":"%s","type":"app",' "$APP"
		[ -n "$REV" ] && printf '"_rev":"%s",' "$REV"
		printf '"manifest":'; cat "apps/$APP/app.json"
		printf ',"_attachments":{'
		FIRST=1
		for FILE in $(cd "apps/$APP" && find . -type f ! -name app.json | sed 's|^\./||' | sort); do
			[ $FIRST = 1 ] || printf ','
			FIRST=0
			printf '"%s":{"content_type":"%s","data":"' "$FILE" "$(content_type "$FILE")"
			base64 < "apps/$APP/$FILE" | tr -d '\n'
			printf '"}'
		done
		printf '}}'
	} > "$DOC"
	REPLY=$(curl -s -X PUT "$COUCH/skylight/$APP" -H 'Content-Type: application/json' --data-binary @"$DOC")
	case "$REPLY" in
		*'"ok":true'*) echo "Pushed $APP" ;;
		*) echo "CouchDB said: $REPLY" >&2; exit 1 ;;
	esac
done
