@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
title NRO - Game Server 14445
if "%~1"=="" (
  powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0Run-Tools.ps1" -Mode Game
) else (
  powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0Run-Tools.ps1" -Mode Game -ProjectRoot "%~1"
)
echo.
pause
