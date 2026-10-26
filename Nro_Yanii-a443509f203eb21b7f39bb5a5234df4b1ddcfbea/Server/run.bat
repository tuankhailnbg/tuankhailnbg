@echo off
setlocal
cd /d "%~dp0"

set "SERVER_JAR="
for /f "usebackq delims=" %%F in (`powershell -NoProfile -Command "$f=Get-ChildItem '.\target\*-jar-with-dependencies.jar' -File -ErrorAction SilentlyContinue ^| Sort-Object LastWriteTime -Descending ^| Select-Object -First 1; if($f){$f.FullName}"`) do set "SERVER_JAR=%%F"

if defined SERVER_JAR (
    echo [NRO] Chay JAR build moi nhat:
    echo %SERVER_JAR%
    java -jar "%SERVER_JAR%"
    exit /b %ERRORLEVEL%
)

if exist "Server.jar" (
    echo [CANH BAO] Khong tim thay JAR moi trong target.
    echo [NRO] Tam chay Server.jar:
    java -jar "Server.jar"
    exit /b %ERRORLEVEL%
)

echo [LOI] Khong tim thay JAR de chay.
echo Hay build bang Maven truoc.
pause
exit /b 1
