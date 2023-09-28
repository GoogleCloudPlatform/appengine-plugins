@echo on

REM Use Java 8 for build
echo %JAVA_HOME%

cd github/appengine-plugins-core

call gcloud.cmd components update --quiet
call gcloud.cmd components install app-engine-java --quiet

call mvnw.cmd clean install cobertura:cobertura -B -U -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss:SSS

exit /b %ERRORLEVEL%
