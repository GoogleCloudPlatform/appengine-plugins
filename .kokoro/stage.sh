#!/bin/bash

# Fail on any error.
set -e

echo "${KOKORO_KEYSTORE_DIR}"

cd github/appengine-plugins

# Use GCP Maven Mirror
mkdir -p "${HOME}"/.m2
cp settings.xml "${HOME}"/.m2

mvn -Prelease -B -U -DskipSurefire=true install -Dtest=!FilePermissionsTest -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss:SSS
