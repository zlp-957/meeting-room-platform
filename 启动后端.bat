@echo off
setlocal

REM ============================================
REM  Meeting Room Platform - Backend Launcher
REM ============================================

set "MAVEN_HOME=%~dp0tools\apache-maven-3.9.9"
set "M2=%MAVEN_HOME%\bin"

REM Database password injected via environment variable (B-12)
REM Please fill in your real MySQL root password below.
set "MYSQL_PASSWORD=请填写你的MySQL密码"

echo ============================================
echo  Starting backend Spring Boot on port 8080...
echo  JDK: %JAVA_HOME%
echo  Maven: %MAVEN_HOME%
echo ============================================
echo.

cd /d "%~dp0backend"
call "%M2%\mvn.cmd" -s "%~dp0tools\settings.xml" spring-boot:run "-Dspring-boot.run.arguments=--server.port=8080"

echo.
echo ============================================
echo  Backend stopped. See messages above.
echo ============================================
pause
