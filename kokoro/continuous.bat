@echo on

REM Java 9 does not work with our builds right now, force java 8
set JAVA_HOME=c:\program files\java\jdk1.8.0_152
set PATH=%JAVA_HOME%\bin;%PATH%

call curl -O https://dl.google.com/dl/cloudsdk/channels/rapid/google-cloud-sdk-windows-x86_64-bundled-python.zip
call unzip google-cloud-sdk-windows-x86_64-bundled-python.zip
call cmd.exe /c T:\tmp\google-cloud-sdk\install.bat

cd github/appengine-plugins-core

call gcloud.cmd components update --quiet
call gcloud.cmd components install app-engine-java --quiet

call mvnw.cmd clean install cobertura:cobertura -B -U
REM curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
