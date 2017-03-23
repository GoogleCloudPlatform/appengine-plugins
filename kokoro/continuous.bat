cd github/appengine-plugins-core
gcloud components update
gcloud components install app-engine-java
mvn clean install cobertura:cobertura -B -U
curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
