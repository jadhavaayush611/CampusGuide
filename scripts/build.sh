#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== CampusGuide Verification Script ==="

echo "--> Auditing Repository Hygiene..."
"$SCRIPT_DIR/check-hygiene.sh"

echo "--> Building Backend..."
cd "$ROOT_DIR/backend"
mvn clean verify
cd "$ROOT_DIR"

if [ -d "$ROOT_DIR/frontend" ]; then
    echo "--> Building Frontend..."
    cd "$ROOT_DIR/frontend"
    if [ -d "node_modules" ]; then
        npm run build
    fi
    cd "$ROOT_DIR"
fi

echo "=== Verification Succeeded ==="
