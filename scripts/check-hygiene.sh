#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "---> Auditing Repository Hygiene..."

FAILED=0

cd "$ROOT_DIR"

# Human-readable regex pattern for absolute machine paths:
# - Windows drive paths: e.g. C:\..., D:/...
# - macOS user paths: e.g. /Users/...
# - Linux user paths: e.g. /home/...
ABSOLUTE_PATH_PATTERN='\b[A-Za-z]:[/\\]|/Users/|/home/'

# Exclude hygiene checking scripts/workflows and intentional documentation examples from self-matching
EXCLUSIONS=(
    ":^scripts/check-hygiene.sh"
    ":^scripts/check-hygiene.bat"
    ":^.github/workflows/repository-hygiene.yml"
    ":^.agents/skills/repository-hygiene/SKILL.md"
)

# Audit absolute machine paths in tracked files across the repository
if git grep -nE "$ABSOLUTE_PATH_PATTERN" -- "." "${EXCLUSIONS[@]}" > /dev/null 2>&1; then
    echo "[HYGIENE ERROR] Absolute machine paths detected in tracked files!"
    git grep -nE "$ABSOLUTE_PATH_PATTERN" -- "." "${EXCLUSIONS[@]}"
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
