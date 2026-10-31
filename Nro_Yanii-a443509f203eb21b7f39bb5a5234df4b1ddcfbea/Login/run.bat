@echo off
setlocal
cd /d "%~dp0"

REM Always run the freshly built Login artifact, not an old Login.jar copy.
set "LOGIN_JAR=%CD%\target\ServerLogin-1.0-RELEASE-jar-with-dependencies.jar"
if not exist "%LOGIN_JAR%" (
    echo [ERROR] Missing Login build: "%LOGIN_JAR%"
    echo Run the Login Maven build before starting the server.
    exit /b 1
)

echo [NRO] Starting the built Login server: "%LOGIN_JAR%"
java -jar "%LOGIN_JAR%"
exit /b %ERRORLEVEL%
