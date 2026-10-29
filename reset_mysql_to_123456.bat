@echo off
:: Self-elevate to Administrator
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
if '%errorlevel%' NEQ '0' (
    echo [INFO] Requesting Administrator privileges to set MySQL password to 123456...
    powershell -Command "Start-Process -FilePath '%~dpnx0' -Verb RunAs"
    exit /b
)

echo ==========================================================
echo   Setting MySQL Root Password to 123456 ^& Creating DB
echo ==========================================================

echo [1/4] Stopping MySQL97 service...
net stop MySQL97 >nul 2>&1
taskkill /f /im mysqld.exe >nul 2>&1

echo [2/4] Preparing SQL initialization script...
(
echo ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
echo FLUSH PRIVILEGES;
echo CREATE DATABASE IF NOT EXISTS banking_db;
) > "%TEMP%\init_mysql.sql"

echo [3/4] Applying password '123456' to MySQL Server...
start "" "C:\Program Files\MySQL\MySQL Server 9.7\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 9.7\my.ini" --init-file="%TEMP%\init_mysql.sql" --console
timeout /t 6 /nobreak >nul
taskkill /f /im mysqld.exe >nul 2>&1
del "%TEMP%\init_mysql.sql" >nul 2>&1

echo [4/4] Starting MySQL97 service...
net start MySQL97

echo.
echo ==========================================================
echo   SUCCESS! MySQL root password is now: 123456
echo   Database 'banking_db' has been created!
echo ==========================================================
echo You can now close this window and run the banking app.
pause
