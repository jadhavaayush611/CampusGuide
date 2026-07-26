@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   CampusGuide Full Development Verification Suite
echo ===================================================

echo [1/3] Running Repository Hygiene Check...
call "%~dp0check-hygiene.bat"
if errorlevel 1 (
    echo [ERROR] Repository hygiene check failed.
    exit /b 1
)

echo [2/3] Building & Testing Backend (Spring Boot)...
cd /d "%~dp0..\backend"
call mvn clean verify
if errorlevel 1 (
    echo [ERROR] Backend compilation or tests failed.
    exit /b 1
)
cd /d "%~dp0.."

echo [3/3] Building Frontend Application (Vite / React)...
if exist "%~dp0..\frontend\package.json" (
    cd /d "%~dp0..\frontend"
    if not exist "node_modules\" (
        echo --> Installing Frontend Dependencies...
        call npm install
    )
    call npm run build
    if errorlevel 1 (
        echo [ERROR] Frontend build failed.
        exit /b 1
    )
    cd /d "%~dp0.."
)

echo ===================================================
echo   SUCCESS: All verification checks passed!
echo ===================================================
