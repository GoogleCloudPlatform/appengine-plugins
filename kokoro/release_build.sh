#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

sudo -E /opt/google-cloud-sdk/bin/gcloud components update -q
sudo -E /opt/google-cloud-sdk/bin/gcloud components install app-engine-java -q

cd github/appengine-plugins-core
mvn -Prelease -B -U verify -Dtest=!FilePermissionsTest -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss:SSS

# copy pom with the name expected in the Maven repository
ARTIFACT_ID=$(mvn -B help:evaluate -Dexpression=project.artifactId 2>/dev/null | grep -v "^\[")
PROJECT_VERSION=$(mvn -B help:evaluate -Dexpression=project.version 2>/dev/null| grep -v "^\[")
cp pom.xml target/${ARTIFACT_ID}-${PROJECT_VERSION}.pom

