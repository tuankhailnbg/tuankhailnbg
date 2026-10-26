@echo off
setlocal
cd /d "%~dp0"

set "SERVER_JAR=%CD%\target\TeaMobi-1.0-RELEASE-jar-with-dependencies.jar"

if not exist "%SERVER_JAR%" (
    echo [LOI] Khong tim thay JAR moi:
    echo %SERVER_JAR%
    echo Hay chay CHAY_NRO_LOGIN_SERVER_AUTO.bat de build truoc.
    pause
    exit /b 1
)

echo [NRO] Dang chay JAR moi nhat:
echo %SERVER_JAR%
java -jar "%SERVER_JAR%"
exit /b %ERRORLEVEL%
