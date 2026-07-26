@echo off
setlocal enabledelayedexpansion

echo [HYGIENE] Auditing Repository Hygiene...

set "FAILED=0"

rem Check for absolute Windows/Unix paths in tracked files
git grep -rnE "[C|D]:\\|/Users/" -- "github" "docs" "backend" "frontend" "scripts" ".agents" > nul 2>&1
if %errorlevel% equ 0 (
    echo [HYGIENE ERROR] Absolute machine paths detected in tracked files!
    git grep -rnE "[C|D]:\\|/Users/" -- "github" "docs" "backend" "frontend" "scripts" ".agents"
    set "FAILED=1"
)

rem Check for sensitive files
if exist "%~dp0..\.env" (
    git ls-files --error-unmatch "%~dp0..\.env" > nul 2>&1
    if %errorlevel% equ 0 (
        echo [HYGIENE ERROR] .env file is tracked in Git!
        set "FAILED=1"
    )
)

if %FAILED% equ 1 (
    echo [HYGIENE FAILED] Fix issues above.
    exit /b 1
) else (
    echo [HYGIENE] Audit Passed Cleanly.
    exit /b 0
)
