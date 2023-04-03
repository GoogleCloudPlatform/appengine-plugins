@echo on

REM Java 9 does not work with our builds right now, force java 8
REM set JAVA_HOME=c:\program files\java\jdk1.8.0_152
REM set PATH=%JAVA_HOME%\bin;%PATH%

REM Java 8 - JAVA_HOME C:\Program Files\Java\jdk1.8.0_211
echo %JAVA_HOME%

cd github/appengine-plugins-core

call gcloud.cmd components update --quiet
call gcloud.cmd components install app-engine-java --quiet

call mvnw.cmd clean install cobertura:cobertura -B -U

exit /b %ERRORLEVEL%
