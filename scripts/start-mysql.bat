@echo off
echo ===================================================
echo Starting Brightloop MySQL 8.4 Server Daemon...
echo Data Directory: D:\mysql-data
echo Port: 3306
echo ===================================================

if not exist "D:\mysql-data" (
    echo [ERROR] MySQL data directory D:\mysql-data not found!
    pause
    exit /b 1
)

"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --datadir="D:\mysql-data" --port=3306 --console
