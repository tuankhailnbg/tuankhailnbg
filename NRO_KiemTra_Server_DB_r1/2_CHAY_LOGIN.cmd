@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
title NRO - Login 3105
if "%~1"=="" (
  powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0Run-Tools.ps1" -Mode Login
) else (
  powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0Run-Tools.ps1" -Mode Login -ProjectRoot "%~1"
)
echo.
pause
