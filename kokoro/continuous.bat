cd github/appengine-plugins-core

rem call gcloud.cmd components copy-bundled-python>>python_path.txt && SET /p CLOUDSDK_PYTHON=<python_path.txt && DEL python_path.txt
call gcloud.cmd components update --quiet
call gcloud.cmd components install app-engine-java --quiet
rem set GOOGLE_CLOUD_SDK_HOME=t:\google\google-cloud-sdk

mvn --version
mvn clean install cobertura:cobertura -B -U
REM curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
