@echo off
echo === CampusGuide Verification Script ===

echo --> Building Backend...
cd backend
call mvn clean verify
if errorlevel 1 exit /b 1
cd ..

if exist "frontend\node_modules\" (
    echo --> Building Frontend...
    cd frontend
    call npm run build
    if errorlevel 1 exit /b 1
    cd ..
)

echo === Verification Succeeded ===
