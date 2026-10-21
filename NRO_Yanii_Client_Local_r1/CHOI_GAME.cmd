@echo off
setlocal
cd /d "%~dp0"
title NRO Yanii - Client Local r1
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start-Client.ps1"
if errorlevel 1 (
  echo.
  echo Chup noi dung loi hoac gui file trong thu muc Logs.
)
echo.
pause
