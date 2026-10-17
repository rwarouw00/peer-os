#!/bin/bash
# Adds a Skylight OS user, or changes an existing user's password.
# Usage: tools/add-user.sh http://admin:password@127.0.0.1:5984 <username> <password>
#
# Creates the CouchDB user, waits for couch_peruser to create their private
# database, and installs the checks (validate_doc_update) in it.
set -e
COUCH="${1%/}"
NAME="$2"
PASSWORD="$3"
if [ -z "$COUCH" ] || [ -z "$PASSWORD" ] || ! [[ "$NAME" =~ ^[A-Za-z0-9_-]{1,32}$ ]]; then
	echo "Usage: $0 http://admin:password@127.0.0.1:5984 <username> <password>" >&2
	echo "Usernames may use letters, digits, '-' and '_' (up to 32 characters)." >&2
	exit 1
fi
cd "$(dirname "$0")/.."

json_text() {   # stdin as a JSON string
	printf '"'
	sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' -e 's/\t/\\t/g' -e 's/\r$//' | awk 'NR > 1 { printf "\\n" } { printf "%s", $0 }'
	printf '"'
}

revision() {    # the _rev of a document, or nothing
	curl -s "$1" | sed -n 's/.*"_rev":"\([^"]*\)".*/\1/p'
}

USER_URL="$COUCH/_users/org.couchdb.user:$NAME"
REV=$(revision "$USER_URL")
BODY="{\"name\":\"$NAME\",\"type\":\"user\",\"roles\":[],\"password\":$(printf '%s' "$PASSWORD" | json_text)${REV:+,\"_rev\":\"$REV\"}}"
REPLY=$(curl -s -X PUT "$USER_URL" -H 'Content-Type: application/json' --data-binary "$BODY")
case "$REPLY" in
	*'"ok":true'*) ;;
	*) echo "CouchDB said: $REPLY" >&2; exit 1 ;;
esac
[ -n "$REV" ] && echo "Changed the password for $NAME" || echo "Added user $NAME"

# couch_peruser creates userdb-<name in hex> a moment later.
DB="$COUCH/userdb-$(printf '%s' "$NAME" | od -An -tx1 | tr -d ' \n')"
for TRY in $(seq 1 40); do
	curl -sf "$DB" > /dev/null && break
	sleep 0.25
done
if ! curl -sf "$DB" > /dev/null; then
	echo "The user's database wasn't created. Is couch_peruser enabled? (tools/push.sh turns it on.)" >&2
	exit 1
fi

# couch_peruser makes the user an admin of their database, which would let
# them remove the checks below. Once it has, keep them as a member only.
for TRY in $(seq 1 40); do
	curl -s "$DB/_security" | grep -q "\"$NAME\"" && break
	sleep 0.25
done
REPLY=$(curl -s -X PUT "$DB/_security" -H 'Content-Type: application/json' \
	--data-binary "{\"admins\":{\"names\":[],\"roles\":[]},\"members\":{\"names\":[\"$NAME\"],\"roles\":[]}}")
case "$REPLY" in
	*'"ok":true'*) ;;
	*) echo "CouchDB said: $REPLY" >&2; exit 1 ;;
esac

DESIGN="$DB/_design/skylight"
REV=$(revision "$DESIGN")
BODY="{\"validate_doc_update\":$(json_text < userdb/validate_doc_update.js)${REV:+,\"_rev\":\"$REV\"}}"
REPLY=$(curl -s -X PUT "$DESIGN" -H 'Content-Type: application/json' --data-binary "$BODY")
case "$REPLY" in
	*'"ok":true'*) echo "Their database is ready: userdb-${DB##*userdb-}" ;;
	*) echo "CouchDB said: $REPLY" >&2; exit 1 ;;
esac
