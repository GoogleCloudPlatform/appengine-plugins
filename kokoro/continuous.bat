@echo on

REM Java 9 does not work with our builds right now, force java 8
set JAVA_HOME=c:\program files\java\jdk1.8.0_152
set PATH=%JAVA_HOME%\bin;%PATH%

cd github/appengine-plugins-core

call gcloud.cmd components update --quiet
call gcloud.cmd components install app-engine-java --quiet

REM Latest Cloud SDK picks up c:\Python27\python.exe
REM https://github.com/GoogleCloudPlatform/appengine-plugins-core/issues/851
set CLOUDSDK_PYTHON=c:\Python37\python.exe

call mvnw.cmd clean install cobertura:cobertura -B -U
REM curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
