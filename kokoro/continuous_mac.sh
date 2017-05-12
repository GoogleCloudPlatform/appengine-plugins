#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

cd github/appengine-plugins-core
gcloud components update
gcloud components install app-engine-java
mvn clean install cobertura:cobertura -B -U
# bash <(curl -s https://codecov.io/bash)
