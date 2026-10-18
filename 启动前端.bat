@echo off
setlocal

REM ============================================
REM  Meeting Room Platform - Frontend Launcher
REM ============================================

REM Put standard Node.js first in PATH so 'node' resolves correctly
set "PATH=C:\Program Files\nodejs;%PATH%"
set "NPM=C:\Program Files\nodejs\npm.cmd"

echo ============================================
echo  Starting frontend Vue dev server on port 8081...
echo ============================================
echo.

cd /d "%~dp0frontend"

if not exist node_modules (
    echo.
    echo  First run: installing dependencies, please wait...
    call "%NPM%" install
    if errorlevel 1 (
        echo.
        echo  [ERROR] npm install failed. Please check network and retry.
        pause
        exit /b 1
    )
)

echo.
echo  Starting Vue dev server...
call "%NPM%" run serve

echo.
echo ============================================
echo  Frontend stopped. See messages above.
echo ============================================
pause
