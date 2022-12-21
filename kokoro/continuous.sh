#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

sudo /opt/google-cloud-sdk/bin/gcloud components update
sudo /opt/google-cloud-sdk/bin/gcloud components install app-engine-java

cd github/appengine-plugins-core

if [ "$EUID" -ne 0 ]
then
  # not running as root
  ./mvnw clean install cobertura:cobertura -B -U
else
  # running as root - skip file permissions tests that don't work on Docker
  ./mvnw clean install cobertura:cobertura -B -U -Dtest=!FilePermissionsTest
fi
