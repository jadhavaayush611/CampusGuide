@echo off
echo === CampusGuide Verification Script ===

echo [1/3] Auditing Repository Hygiene...
call "%~dp0check-hygiene.bat"
if errorlevel 1 exit /b 1

echo [2/3] Building Backend...
cd "%~dp0..\backend"
call mvn clean verify
if errorlevel 1 exit /b 1
cd "%~dp0.."

if exist "%~dp0..\frontend\package.json" (
    echo [3/3] Building Frontend...
    cd "%~dp0..\frontend"
    if exist "node_modules\" (
        call npm run build
        if errorlevel 1 exit /b 1
    )
    cd "%~dp0.."
)

echo === Verification Succeeded ===
