cd github/appengine-plugins-core

wget https://dl.google.com/dl/cloudsdk/channels/rapid/GoogleCloudSDKInstaller.exe
GoogleCloudSDKInstaller.exe /S /noreporting /nostartmenu /nodesktop /logtofile /D=T:\google
call t:\google\google-cloud-sdk\bin\gcloud.cmd components copy-bundled-python>>python_path.txt && SET /p CLOUDSDK_PYTHON=<python_path.txt && DEL python_path.txt
call t:\google\google-cloud-sdk\bin\gcloud.cmd components update --quiet
call t:\google\google-cloud-sdk\bin\gcloud.cmd components install app-engine-java --quiet

mvn clean install cobertura:cobertura -B -U
curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
