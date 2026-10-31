@echo off
echo ===================================================
echo Starting Brightloop Spring Boot Backend (Java 24)...
echo URL: http://localhost:8080
echo ===================================================

cd /d "%~dp0..\backend"
call .\mvnw.cmd spring-boot:run
