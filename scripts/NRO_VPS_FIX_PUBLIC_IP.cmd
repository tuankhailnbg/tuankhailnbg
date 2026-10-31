@echo off
setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0.."
set "NRO_ROOT=%CD%"
set "NRO_CFG=%NRO_ROOT%\Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea\Server\resources\config\server.properties"
set "NRO_PUBLIC_IP=103.74.102.34"

if not exist "%NRO_CFG%" (
    echo [ERROR] Khong tim thay server.properties
    echo %NRO_CFG%
    exit /b 2
)

echo Dang cap nhat dia chi public tren VPS ...
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -Command ^
  "$p=$env:NRO_CFG; $v='server.sv1=Yanii:' + $env:NRO_PUBLIC_IP + ':14445:0,0,0'; $t=[System.IO.File]::ReadAllText($p); if($t -notmatch '(?m)^server\.sv1='){Write-Host '[ERROR] Khong tim thay server.sv1'; exit 2}; $u=[regex]::Replace($t,'(?m)^server\.sv1=.*$',$v); if($u -ne $t){Copy-Item -LiteralPath $p -Destination ($p+'.bak') -Force; [System.IO.File]::WriteAllText($p,$u,(New-Object System.Text.UTF8Encoding($false)))}; Write-Host ('[OK] '+$v)"
if errorlevel 1 (
    echo [ERROR] Khong sua duoc IP. Khong khoi dong lai server.
    exit /b 1
)
echo.
echo Dia chi duoc quang ba cho client:
findstr /B /C:"server.sv1=" "%NRO_CFG%"
echo.
echo Chi sao luu/sua IP; khong thay doi database, mat khau, tai khoan.
echo Hay chay CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat sau khi cap nhat.
exit /b 0
