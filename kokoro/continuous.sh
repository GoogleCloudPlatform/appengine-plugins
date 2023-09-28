#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

sudo /opt/google-cloud-sdk/bin/gcloud components update
sudo /opt/google-cloud-sdk/bin/gcloud components install app-engine-java

cd github/appengine-plugins-core

unset JAVA_TOOL_OPTIONS
update-java-alternatives -s /usr/lib/jvm/java-1.8.0-openjdk-amd64
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64

if [ "$EUID" -ne 0 ]
then
  # not running as root
  ./mvnw clean install cobertura:cobertura -B -U -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss:SSS
else
  # running as root - skip file permissions tests that don't work on Docker
  ./mvnw clean install cobertura:cobertura -B -U -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss:SSS -Dtest=!FilePermissionsTest
fi
