@echo off
setlocal enabledelayedexpansion

echo [HYGIENE] Auditing Repository Hygiene...

set "FAILED=0"

rem Human-readable regex pattern for absolute machine paths (Windows C:\ / D:/, macOS /Users/, Linux /home/)
set "ABSOLUTE_PATH_PATTERN=\b[A-Za-z]:[/\\]|/Users/|/home/"

rem Check for absolute Windows/Unix paths in tracked files across the repository, excluding hygiene check files
git grep -nE "!ABSOLUTE_PATH_PATTERN!" -- "." ":(exclude)scripts/check-hygiene.sh" ":(exclude)scripts/check-hygiene.bat" ":(exclude).github/workflows/repository-hygiene.yml" ":(exclude).agents/skills/repository-hygiene/SKILL.md" > nul 2>&1
if !errorlevel! equ 0 (
    echo [HYGIENE ERROR] Absolute machine paths detected in tracked files!
    git grep -nE "!ABSOLUTE_PATH_PATTERN!" -- "." ":(exclude)scripts/check-hygiene.sh" ":(exclude)scripts/check-hygiene.bat" ":(exclude).github/workflows/repository-hygiene.yml" ":(exclude).agents/skills/repository-hygiene/SKILL.md"
    set "FAILED=1"
)

rem Check for sensitive files
if exist "%~dp0..\.env" (
    git ls-files --error-unmatch "%~dp0..\.env" > nul 2>&1
    if !errorlevel! equ 0 (
        echo [HYGIENE ERROR] .env file is tracked in Git!
        set "FAILED=1"
    )
)

if !FAILED! equ 1 (
    echo [HYGIENE FAILED] Fix issues above.
    exit /b 1
) else (
    echo [HYGIENE] Audit Passed Cleanly.
    exit /b 0
)
