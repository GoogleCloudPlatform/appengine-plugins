#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

mvn clean install -B -U
