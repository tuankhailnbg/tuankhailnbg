@echo off
setlocal EnableExtensions
cd /d "%~dp0"
set "SERVER_DIR=%~dp0..\..\Server"
set "DB_NAME=ngocrong"
set "DB_USER=nro_app"
set "DB_HOST=127.0.0.1"
set "DB_PORT=3306"
set "SQL_FILE=%~dp0install_18_costumes_and_repair_storage.sql"
set "BACKUP_DIR=%~dp0backup"
set "MYSQL=mariadb.exe"
set "DUMP=mariadb-dump.exe"

where mariadb.exe >nul 2>nul
if errorlevel 1 set "MYSQL=mysql.exe"
where mariadb-dump.exe >nul 2>nul
if errorlevel 1 set "DUMP=mysqldump.exe"
where %MYSQL% >nul 2>nul
if errorlevel 1 (
  echo [ERROR] MariaDB/MySQL CLI not found in PATH.
  exit /b 1
)
where %DUMP% >nul 2>nul
if errorlevel 1 (
  echo [ERROR] MariaDB/MySQL dump tool not found in PATH.
  exit /b 1
)
if not exist "%SERVER_DIR%\resources\normal\image\1\icon" (
  echo [ERROR] Wrong Server folder: "%SERVER_DIR%"
  exit /b 1
)
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
set "BACKUP_FILE=%BACKUP_DIR%\ngocrong_before_costumes_%RANDOM%_%RANDOM%.sql"

echo [1/5] Backup database. MariaDB may ask for password.
"%DUMP%" --host=%DB_HOST% --port=%DB_PORT% --user=%DB_USER% --password --default-character-set=utf8mb4 --single-transaction %DB_NAME% > "%BACKUP_FILE%"
if errorlevel 1 (
  echo [ERROR] Database backup failed. Import has NOT started.
  exit /b 1
)
for %%F in ("%BACKUP_FILE%") do if %%~zF LSS 1024 (
  echo [ERROR] Database backup is unexpectedly small. Import has NOT started.
  exit /b 1
)

echo [2/5] Download costume images A
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0batch01a\download_assets.ps1" -ServerFolder "%SERVER_DIR%"
if errorlevel 1 exit /b 1

echo [3/5] Download costume images B
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0batch01b\download_assets.ps1" -ServerFolder "%SERVER_DIR%"
if errorlevel 1 exit /b 1

echo [4/5] Download costume images C
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0batch01c\download_assets.ps1" -ServerFolder "%SERVER_DIR%"
if errorlevel 1 exit /b 1

echo [5/5] Apply transaction-guarded SQL to database. MariaDB may ask for password.
"%MYSQL%" --host=%DB_HOST% --port=%DB_PORT% --user=%DB_USER% --password --default-character-set=utf8mb4 %DB_NAME% < "%SQL_FILE%"
if errorlevel 1 (
  echo [ERROR] SQL import was rejected or failed. Original database backup: "%BACKUP_FILE%"
  echo If it rejected base row counts, DO NOT force it; review current VPS database first.
  exit /b 1
)
echo [SUCCESS] SQL completed. Backup: "%BACKUP_FILE%"
echo IMPORTANT: Pull this GitHub branch, rebuild Server using Maven and restart it.
echo NOTE: A successful SQL import does not prove client graphics; test in game before gifts.
endlocal
