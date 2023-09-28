@echo on

REM Use Java 8 for build
echo %JAVA_HOME%

cd github/appengine-plugins-core

if not exist "%HOME%\.m2" mkdir "%HOME%\.m2"
copy settings.xml "%HOME%\.m2"

call gcloud.cmd components update --quiet
call gcloud.cmd components install app-engine-java --quiet

call mvnw.cmd clean install cobertura:cobertura -B -U

exit /b %ERRORLEVEL%
