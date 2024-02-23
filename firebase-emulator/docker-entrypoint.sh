#!/bin/bash

set -euo pipefail

export firebase_port_auth=${FIREBASE_PORT_AUTH:-9099}
export firebase_port_functions=${FIREBASE_PORT_FUNCTIONS:-5001}
export firebase_port_ui=${FIREBASE_PORT_UI:-4000}
export firebase_project_id=${FIREBASE_PROJECT_ID:-demo-project}
export firebase_auth_accounts=${FIREBASE_AUTH_ACCOUNTS:-}

:curl() {
    curl -vs -H "Authorization: Bearer owner" "$@"
}

:curl:accounts() {
    :curl "http://localhost:$firebase_port_auth/identitytoolkit.googleapis.com/v1/projects/$firebase_project_id/accounts" \
        -HContent-Type:application/json \
        "$@"
}

cd /firebase/

cat > firebase.json <<JSON
{
    "emulators": {
        "auth": {
            "host": "0.0.0.0",
            "port": $firebase_port_auth
        },
        "functions": {
            "host": "0.0.0.0",
            "port": $firebase_port_functions
        },
        "ui": {
            "enabled": true,
            "host": "0.0.0.0",
            "port": $firebase_port_ui
        }
    },
    "functions": [
        {
          "source": "functions",
          "codebase": "default",
          "runtime": "nodejs20",
          "ignore": [
            "node_modules",
            ".git",
            "firebase-debug.log",
            "firebase-debug.*.log"
          ]
        }
      ]
}
JSON

data_dir=emulator-data

(
    firebase emulators:start 2>&1 \
        $([ -d $data_dir ] && echo "--import $data_dir") \
        --project "$firebase_project_id" \
        --only 'auth,functions' \
            | sed -ur 's/^/:: [firebase] /'
) &
firebase_pid=$!

:wait:port() {
    while ! echo >/dev/tcp/localhost/$1; do
        sleep 0.1
    done 2>/dev/null
}

echo ":: waiting for firebase to start up — auth"
:wait:port $firebase_port_auth
echo ":: ok"

echo ":: waiting for firebase to start up — functions"
:wait:port $firebase_port_functions
echo ":: ok"

[[ ! -d $data_dir && "$firebase_auth_accounts" ]] && {
    while read entry; do
        [[ ! "$entry" ]] && break
        :curl:accounts -X POST --data-raw "$entry"
    done <<< "$firebase_auth_accounts"
}

echo ":: ready"

:stop() {
    echo
    echo ":: stopping"
    mkdir -p /data
    firebase emulators:export --project "$firebase_project_id" --only auth /data
    cp -rf /data/* $data_dir
    echo ":: data exported to $data_dir"
}

trap :stop INT TERM

wait $firebase_pid
