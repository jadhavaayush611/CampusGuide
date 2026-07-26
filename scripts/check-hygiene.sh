#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "---> Auditing Repository Hygiene..."

FAILED=0

cd "$ROOT_DIR"

# Audit absolute machine paths in tracked files
if git grep -rnE "[C|D]:\\\\|/Users/" -- ".github" "docs" "backend" "frontend" "scripts" ".agents" > /dev/null 2>&1; then
    echo "[HYGIENE ERROR] Absolute machine paths detected in tracked files!"
    git grep -rnE "[C|D]:\\\\|/Users/" -- ".github" "docs" "backend" "frontend" "scripts" ".agents"
    FAILED=1
fi

# Audit tracked secrets
if git ls-files --error-unmatch .env > /dev/null 2>&1; then
    echo "[HYGIENE ERROR] .env file is tracked in Git!"
    FAILED=1
fi

if [ $FAILED -ne 0 ]; then
    echo "[HYGIENE FAILED] Fix issues above."
    exit 1
else
    echo "---> Hygiene Audit Passed Cleanly."
    exit 0
fi
