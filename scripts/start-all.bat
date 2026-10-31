@echo off
echo ===================================================
echo Starting Brightloop Subscription System Ecosystem
echo ===================================================

echo [1/2] Starting MySQL Database Daemon...
start "Brightloop MySQL 8.4" cmd /k "%~dp0start-mysql.bat"

timeout /t 3 >nul

echo [2/2] Starting Spring Boot REST Backend...
start "Brightloop Backend" cmd /k "%~dp0start-backend.bat"

echo.
echo Both services launched in separate windows!
echo Open frontend\index.html in your browser.
