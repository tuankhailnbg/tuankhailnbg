@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul
title NRO VPS - LOGIN + SERVER AUTO BUILD

cd /d "%~dp0"
set "ROOT=%CD%"
set "LOGIN_DIR=%ROOT%\Login"
set "SERVER_DIR=%ROOT%\Server"

set "LOGIN_PORT=3105"
set "SERVER_PORT=14445"

echo ========================================================
echo NRO VPS - AUTO STOP / BUILD / RUN
echo ROOT: %ROOT%
echo Login port : %LOGIN_PORT%
echo Server port: %SERVER_PORT%
echo ========================================================

if not exist "%LOGIN_DIR%" (
  echo [LOI] Khong tim thay %LOGIN_DIR%
  goto :FAIL
)
if not exist "%SERVER_DIR%" (
  echo [LOI] Khong tim thay %SERVER_DIR%
  goto :FAIL
)

REM ===== TIM JAVA JDK =====
set "JAVA_HOME="
for /f "usebackq delims=" %%J in (`powershell.exe -NoProfile -Command "$j=Get-Command javac.exe -ErrorAction SilentlyContinue; if($j){Split-Path (Split-Path $j.Source -Parent) -Parent}"`) do if not defined JAVA_HOME set "JAVA_HOME=%%J"
if not defined JAVA_HOME (
  for /f "usebackq delims=" %%J in (`powershell.exe -NoProfile -Command "$d=Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -Filter 'jdk-21*' -ErrorAction SilentlyContinue ^| Sort-Object Name -Descending ^| Select-Object -First 1; if($d){$d.FullName}"`) do if not defined JAVA_HOME set "JAVA_HOME=%%J"
)
if not defined JAVA_HOME (
  echo [LOI] Khong tim thay JDK 21.
  goto :FAIL
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo.
echo [1/8] Java
echo %JAVA_HOME%
java -version
if errorlevel 1 goto :FAIL

REM ===== TIM MAVEN =====
set "MAVEN="
for /f "delims=" %%M in ('where.exe mvn.cmd 2^>nul') do if not defined MAVEN set "MAVEN=%%M"
if not defined MAVEN if exist "C:\Tools\maven\bin\mvn.cmd" set "MAVEN=C:\Tools\maven\bin\mvn.cmd"
if not defined MAVEN if exist "D:\Tools\maven\bin\mvn.cmd" set "MAVEN=D:\Tools\maven\bin\mvn.cmd"
if not defined MAVEN (
  echo [LOI] Khong tim thay Maven.
  echo Chay lenh: where.exe mvn
  goto :FAIL
)

echo.
echo [2/8] Maven
echo %MAVEN%
call "%MAVEN%" -version
if errorlevel 1 goto :FAIL

REM ===== TAT LOGIN CU =====
echo.
echo [3/8] Tat Login cu port %LOGIN_PORT%
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ids=@(Get-NetTCPConnection -LocalPort %LOGIN_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique); foreach($pidValue in $ids){$p=Get-CimInstance Win32_Process -Filter ('ProcessId='+$pidValue) -ErrorAction SilentlyContinue; if($p -and $p.Name -in @('java.exe','javaw.exe')){Write-Host ('Stop Login PID '+$pidValue); Stop-Process -Id $pidValue -Force -ErrorAction SilentlyContinue}}"

REM ===== TAT SERVER CU =====
echo.
echo [4/8] Tat Game Server cu port %SERVER_PORT%
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ids=@(Get-NetTCPConnection -LocalPort %SERVER_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique); foreach($pidValue in $ids){$p=Get-CimInstance Win32_Process -Filter ('ProcessId='+$pidValue) -ErrorAction SilentlyContinue; if($p -and $p.Name -in @('java.exe','javaw.exe')){Write-Host ('Stop Server PID '+$pidValue); Stop-Process -Id $pidValue -Force -ErrorAction SilentlyContinue}}"
timeout /t 2 /nobreak >nul

REM ===== BUILD LOGIN + CAP NHAT Login.jar =====
echo.
echo [5/8] Build Login
pushd "%LOGIN_DIR%"
call "%MAVEN%" clean package -DskipTests
if errorlevel 1 (
  popd
  echo [BUILD FAILURE] Login
  goto :FAIL
)
powershell.exe -NoProfile -Command "$f=Get-ChildItem '.\target\*-jar-with-dependencies.jar' -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if(!$f){exit 2}; Copy-Item $f.FullName '.\Login.jar' -Force; Write-Host ('Login.jar <= '+$f.Name)"
if errorlevel 1 (
  popd
  goto :FAIL
)
popd

REM ===== BUILD SERVER + CAP NHAT Server.jar =====
echo.
echo [6/8] Build Game Server
pushd "%SERVER_DIR%"
call "%MAVEN%" clean package -DskipTests
if errorlevel 1 (
  popd
  echo [BUILD FAILURE] Server
  goto :FAIL
)
powershell.exe -NoProfile -Command "$f=Get-ChildItem '.\target\*-jar-with-dependencies.jar' -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if(!$f){exit 2}; Copy-Item $f.FullName '.\Server.jar' -Force; Write-Host ('Server.jar <= '+$f.Name)"
if errorlevel 1 (
  popd
  goto :FAIL
)
popd

REM ===== CHAY LOGIN =====
echo.
echo [7/8] Chay Login\run.bat
if not exist "%LOGIN_DIR%\run.bat" (
  echo [LOI] Thieu Login\run.bat
  goto :FAIL
)
start "NRO LOGIN" /D "%LOGIN_DIR%" cmd /k call "%LOGIN_DIR%\run.bat"

echo Cho Login mo port %LOGIN_PORT%...
powershell.exe -NoProfile -Command "$ok=$false; 1..20 | ForEach-Object {if(Get-NetTCPConnection -LocalPort %LOGIN_PORT% -State Listen -ErrorAction SilentlyContinue){$ok=$true; break}; Start-Sleep -Seconds 1}; if(!$ok){exit 1}"
if errorlevel 1 (
  echo [LOI] Login khong mo port %LOGIN_PORT%
  goto :FAIL
)

REM ===== CHAY SERVER =====
echo.
echo [8/8] Chay Server\run.bat
if not exist "%SERVER_DIR%\run.bat" (
  echo [LOI] Thieu Server\run.bat
  goto :FAIL
)
start "NRO GAME SERVER" /D "%SERVER_DIR%" cmd /k call "%SERVER_DIR%\run.bat"

echo Cho Game Server mo port %SERVER_PORT%...
powershell.exe -NoProfile -Command "$ok=$false; 1..30 | ForEach-Object {if(Get-NetTCPConnection -LocalPort %SERVER_PORT% -State Listen -ErrorAction SilentlyContinue){$ok=$true; break}; Start-Sleep -Seconds 1}; if(!$ok){exit 1}"
if errorlevel 1 (
  echo [LOI] Game Server khong mo port %SERVER_PORT%
  goto :FAIL
)

echo.
echo ========================================================
echo THANH CONG
echo Login noi bo : 127.0.0.1:%LOGIN_PORT%
echo Game public  : 118.68.174.34:%SERVER_PORT%
echo ========================================================
pause
exit /b 0

:FAIL
echo.
echo ========================================================
echo KHOI DONG THAT BAI - XEM LOI PHIA TREN
echo ========================================================
pause
exit /b 1
