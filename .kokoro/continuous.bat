@echo on

REM Use Java 8 for build
echo %JAVA_HOME%

cd git/appengine-plugins

if not exist "%HOME%\.m2" mkdir "%HOME%\.m2"
copy settings.xml "%HOME%\.m2"

call mvnw.cmd clean install -B -U -DskipSurefire=true -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss:SSS

exit /b %ERRORLEVEL%
