#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "==================================================="
echo "  CampusGuide Developer Environment Setup Helper"
echo "==================================================="

echo "[1/4] Checking Java JDK..."
java -version

echo "[2/4] Checking Maven..."
mvn -version

echo "[3/4] Checking Node.js & npm..."
node -v
npm -v

echo "[4/4] Installing Frontend Dependencies..."
if [ -d "$ROOT_DIR/frontend" ]; then
    cd "$ROOT_DIR/frontend"
    npm install
    cd "$ROOT_DIR"
fi

echo "==================================================="
echo "  Environment setup complete! Ready for development."
echo "==================================================="
