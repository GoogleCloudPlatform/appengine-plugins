#!/bin/bash -
# Usage: ./release <release version> <next version>

set -e

Die() {
	echo $1
	exit 1
}

VERSION=$1
NEXT_VERSION=$2

echo '===== RELEASE SETUP SCRIPT ====='

if [[ $(git status -uno --porcelain) ]]; then
    Die 'There are uncommitted changes.'
fi

# Checks out a new branch for this version release (eg. 1.5.7).
git checkout -b ${VERSION}

# Updates the pom.xml with the version to release.
mvn versions:set versions:commit -DnewVersion=${VERSION}

# Tags a new commit for this release.
git commit -am "preparing release ${VERSION}"
git tag v${VERSION}

# Updates the pom.xml with the next snapshot version.
# For example, when releasing 1.5.7, the next snapshot version would be 1.5.8-SNAPSHOT.
NEXT_SNAPSHOT=${NEXT_VERSION}-SNAPSHOT
mvn versions:set versions:commit -DnewVersion=${NEXT_SNAPSHOT}

# Pushs the release branch to Github.
git push --tags --set-upstream ${VERSION} origin/${VERSION}

# File a PR on Github for the new branch. Have someone LGTM it, which gives you permission to continue.
echo 'File a PR for the new release branch:'
echo https://github.com/GoogleCloudPlatform/appengine-plugins-core/compare/${VERSION}