#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "---> Cleaning CampusGuide Build Targets and Caches..."

rm -rf "$ROOT_DIR/backend/target"
rm -rf "$ROOT_DIR/frontend/dist"
rm -rf "$ROOT_DIR/frontend/.vite"

echo "---> Cleanup Complete."
