@echo off
echo [CLEAN] Cleaning CampusGuide Build Targets and Caches...

if exist "%~dp0..\backend\target\" (
    echo Cleaning backend target...
    rmdir /s /q "%~dp0..\backend\target"
)

if exist "%~dp0..\frontend\dist\" (
    echo Cleaning frontend dist...
    rmdir /s /q "%~dp0..\frontend\dist"
)

if exist "%~dp0..\frontend\.vite\" (
    echo Cleaning frontend .vite cache...
    rmdir /s /q "%~dp0..\frontend\.vite"
)

echo [CLEAN] Cleanup Complete.
