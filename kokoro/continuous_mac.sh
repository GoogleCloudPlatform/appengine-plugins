#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-8-latest/Contents/Home

gcloud components update
gcloud components install app-engine-java

# Use GCP Maven Mirror
mkdir -p ${HOME}/.m2
cp settings.xml ${HOME}/.m2

cd github/appengine-plugins-core
./mvnw clean install cobertura:cobertura -B -U
