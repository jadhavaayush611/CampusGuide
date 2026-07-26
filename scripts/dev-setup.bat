@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   CampusGuide Developer Environment Setup Helper
echo ===================================================

echo [1/4] Checking Java JDK...
java -version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java JDK is not installed or not in PATH.
    exit /b 1
)

echo [2/4] Checking Maven...
mvn -version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Apache Maven is not installed or not in PATH.
    exit /b 1
)

echo [3/4] Checking Node.js & npm...
node -v > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js is not installed or not in PATH.
    exit /b 1
)

echo [4/4] Installing Frontend Dependencies...
if exist "%~dp0..\frontend\package.json" (
    cd /d "%~dp0..\frontend"
    call npm install
    cd /d "%~dp0.."
)

echo ===================================================
echo   Environment setup complete! Ready for development.
echo ===================================================
