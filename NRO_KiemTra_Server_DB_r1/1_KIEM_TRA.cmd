@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
title NRO - Kiem tra database va server
if "%~1"=="" (
  powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0Run-Tools.ps1" -Mode Check
) else (
  powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0Run-Tools.ps1" -Mode Check -ProjectRoot "%~1"
)
echo.
pause
