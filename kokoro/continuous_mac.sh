#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

gcloud components update
gcloud components install app-engine-java

cd github/appengine-plugins-core
./mvnw clean install cobertura:cobertura -B -U
# bash <(curl -s https://codecov.io/bash)
