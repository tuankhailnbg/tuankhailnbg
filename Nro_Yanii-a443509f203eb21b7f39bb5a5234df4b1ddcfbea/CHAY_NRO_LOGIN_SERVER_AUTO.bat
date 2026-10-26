@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul
title NRO - LOGIN + SERVER AUTO RESTART

REM =========================================================
REM DAT FILE NAY O THU MUC GOC CHUA:
REM   Login\
REM   Server\
REM =========================================================
cd /d "%~dp0"
set "ROOT=%CD%"
set "LOGIN_DIR=%ROOT%\Login"
set "SERVER_DIR=%ROOT%\Server"

REM ===== CAU HINH MAY CUA BAN =====
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MAVEN=C:\Tools\maven\bin\mvn.cmd"
if not exist "%MAVEN%" set "MAVEN=D:\Tools\maven\bin\mvn.cmd"

REM ===== CONG NRO =====
set "LOGIN_PORT=14445"
set "SERVER_PORT=3105"

echo.
echo ========================================================
echo       NRO - STOP OLD / BUILD / RUN LOGIN + SERVER
echo ========================================================
echo ROOT: %ROOT%
echo.

REM =========================================================
REM KIEM TRA THU MUC
REM =========================================================
if not exist "%LOGIN_DIR%" (
    echo [LOI] Khong tim thay thu muc Login:
    echo %LOGIN_DIR%
    goto :FAIL
)

if not exist "%SERVER_DIR%" (
    echo [LOI] Khong tim thay thu muc Server:
    echo %SERVER_DIR%
    goto :FAIL
)

REM =========================================================
REM TIM FILE RUN CUA LOGIN
REM =========================================================
set "LOGIN_RUN="
if exist "%LOGIN_DIR%\run.bat" set "LOGIN_RUN=%LOGIN_DIR%\run.bat"
if not defined LOGIN_RUN if exist "%LOGIN_DIR%\RUN.bat" set "LOGIN_RUN=%LOGIN_DIR%\RUN.bat"
if not defined LOGIN_RUN if exist "%LOGIN_DIR%\run.cmd" set "LOGIN_RUN=%LOGIN_DIR%\run.cmd"
if not defined LOGIN_RUN if exist "%LOGIN_DIR%\RUN.cmd" set "LOGIN_RUN=%LOGIN_DIR%\RUN.cmd"

if not defined LOGIN_RUN (
    echo [LOI] Khong tim thay run.bat/run.cmd trong Login.
    goto :FAIL
)

REM =========================================================
REM TIM FILE RUN CUA SERVER
REM =========================================================
set "SERVER_RUN="
if exist "%SERVER_DIR%\run.bat" set "SERVER_RUN=%SERVER_DIR%\run.bat"
if not defined SERVER_RUN if exist "%SERVER_DIR%\RUN.bat" set "SERVER_RUN=%SERVER_DIR%\RUN.bat"
if not defined SERVER_RUN if exist "%SERVER_DIR%\run.cmd" set "SERVER_RUN=%SERVER_DIR%\run.cmd"
if not defined SERVER_RUN if exist "%SERVER_DIR%\RUN.cmd" set "SERVER_RUN=%SERVER_DIR%\RUN.cmd"

if not defined SERVER_RUN (
    echo [LOI] Khong tim thay run.bat/run.cmd trong Server.
    goto :FAIL
)

REM =========================================================
REM KIEM TRA JAVA + MAVEN
REM =========================================================
echo [1/7] Kiem tra Java...
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [LOI] Khong tim thay Java:
    echo %JAVA_HOME%\bin\java.exe
    goto :FAIL
)
"%JAVA_HOME%\bin\java.exe" -version
if errorlevel 1 goto :FAIL

echo.
echo [2/7] Kiem tra Maven...
if not exist "%MAVEN%" (
    echo [LOI] Khong tim thay Maven:
    echo %MAVEN%
    goto :FAIL
)
call "%MAVEN%" -version
if errorlevel 1 goto :FAIL

REM =========================================================
REM TAT DUNG LOGIN CU THEO CONG 14445
REM Chi Stop neu process la java.exe/javaw.exe
REM =========================================================
echo.
echo [3/7] Tat Login cu tren port %LOGIN_PORT%...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$port=%LOGIN_PORT%; " ^
  "$pids = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique); " ^
  "foreach($pidValue in $pids){ " ^
  "  $p = Get-CimInstance Win32_Process -Filter ('ProcessId=' + $pidValue) -ErrorAction SilentlyContinue; " ^
  "  if($p -and $p.Name -in @('java.exe','javaw.exe')){ " ^
  "    Write-Host ('  Stop Login PID ' + $pidValue + ': ' + $p.CommandLine); " ^
  "    Stop-Process -Id $pidValue -Force -ErrorAction SilentlyContinue " ^
  "  } else { " ^
  "    Write-Host ('  Port %LOGIN_PORT% dang do process khac su dung, KHONG tat PID ' + $pidValue) " ^
  "  } " ^
  "}; Start-Sleep -Seconds 1"

REM =========================================================
REM TAT DUNG SERVER CU THEO CONG 3105 + TeaMobi JAR
REM =========================================================
echo.
echo [4/7] Tat Server cu tren port %SERVER_PORT%...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ids = New-Object System.Collections.Generic.HashSet[int]; " ^
  "Get-NetTCPConnection -LocalPort %SERVER_PORT% -State Listen -ErrorAction SilentlyContinue | ForEach-Object { [void]$ids.Add([int]$_.OwningProcess) }; " ^
  "Get-CimInstance Win32_Process | Where-Object { " ^
  "  ($_.Name -in @('java.exe','javaw.exe')) -and $_.CommandLine -and " ^
  "  $_.CommandLine -match 'TeaMobi-1\.0-RELEASE(?:-jar-with-dependencies)?\.jar' " ^
  "} | ForEach-Object { [void]$ids.Add([int]$_.ProcessId) }; " ^
  "foreach($pidValue in $ids){ " ^
  "  $p = Get-CimInstance Win32_Process -Filter ('ProcessId=' + $pidValue) -ErrorAction SilentlyContinue; " ^
  "  if($p -and $p.Name -in @('java.exe','javaw.exe')){ " ^
  "    Write-Host ('  Stop Server PID ' + $pidValue + ': ' + $p.CommandLine); " ^
  "    Stop-Process -Id $pidValue -Force -ErrorAction SilentlyContinue " ^
  "  } else { " ^
  "    Write-Host ('  KHONG tat PID ' + $pidValue + ' vi khong phai Java.') " ^
  "  } " ^
  "}; Start-Sleep -Seconds 2"

REM =========================================================
REM BUILD SERVER MOI NHAT
REM =========================================================
echo.
echo [5/7] Build Server moi nhat bang Maven...
pushd "%SERVER_DIR%"
call "%MAVEN%" clean package -DskipTests
set "BUILD_RESULT=%ERRORLEVEL%"
popd

if not "%BUILD_RESULT%"=="0" (
    echo.
    echo ========================================================
    echo [BUILD FAILURE]
    echo KHONG khoi dong Login/Server de tranh chay ban loi.
    echo ========================================================
    goto :FAIL
)

echo.
echo [BUILD SUCCESS]

set "SERVER_JAR=%SERVER_DIR%\target\TeaMobi-1.0-RELEASE-jar-with-dependencies.jar"
if not exist "%SERVER_JAR%" (
    echo [LOI] Build thanh cong nhung khong tim thay:
    echo %SERVER_JAR%
    goto :FAIL
)

echo JAR moi: %SERVER_JAR%

REM =========================================================
REM CHAY FILE RUN CUA LOGIN
REM =========================================================
echo.
echo [6/7] Khoi dong LOGIN...
echo RUN: %LOGIN_RUN%
start "NRO LOGIN" /D "%LOGIN_DIR%" cmd /k call "%LOGIN_RUN%"

echo Cho Login khoi dong...
timeout /t 4 /nobreak >nul

REM =========================================================
REM CHAY FILE RUN CUA SERVER
REM =========================================================
echo.
echo [7/7] Khoi dong SERVER...
echo RUN: %SERVER_RUN%
start "NRO SERVER" /D "%SERVER_DIR%" cmd /k call "%SERVER_RUN%"

echo.
echo ========================================================
echo DA HOAN TAT
echo ========================================================
echo Login : %LOGIN_RUN%
echo Server: %SERVER_RUN%
echo.
echo Moi lan sua code:
echo   1. Chay lai file BAT nay.
echo   2. BAT tu tat Login/Server cu.
echo   3. Build Server moi.
echo   4. Chay RUN cua Login.
echo   5. Chay RUN cua Server.
echo ========================================================
echo.
pause
exit /b 0

:FAIL
echo.
echo ========================================================
echo KHONG THE KHOI DONG NRO.
echo Kiem tra thong bao loi phia tren.
echo ========================================================
pause
exit /b 1
