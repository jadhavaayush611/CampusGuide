#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "==================================================="
echo "  CampusGuide Full Development Verification Suite"
echo "==================================================="

echo "[1/3] Running Repository Hygiene Check..."
"$SCRIPT_DIR/check-hygiene.sh"

echo "[2/3] Building & Testing Backend (Spring Boot)..."
cd "$ROOT_DIR/backend"
mvn clean verify
cd "$ROOT_DIR"

echo "[3/3] Building Frontend Application (Vite / React)..."
if [ -d "$ROOT_DIR/frontend" ]; then
    cd "$ROOT_DIR/frontend"
    if [ ! -d "node_modules" ]; then
        echo "--> Installing Frontend Dependencies..."
        npm install
    fi
    npm run build
    cd "$ROOT_DIR"
fi

echo "==================================================="
echo "  SUCCESS: All verification checks passed!"
echo "==================================================="
