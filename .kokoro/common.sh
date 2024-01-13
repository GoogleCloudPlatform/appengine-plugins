#!/bin/bash
# Copyright 2023 Google LLC
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA

set -eov pipefail

# Get secrets from keystore and set and environment variables
setup_environment_secrets() {
  GPG_PASSPHRASE=$(cat "${KOKORO_KEYSTORE_DIR}"/70247_maven-gpg-passphrase)
  export GPG_PASSPHRASE
  export GPG_HOMEDIR=${TMPDIR}/gpg
  mkdir "${GPG_HOMEDIR}"
  mv "${KOKORO_KEYSTORE_DIR}"/70247_maven-gpg-pubkeyring "${GPG_HOMEDIR}"/pubring.gpg
  mv "${KOKORO_KEYSTORE_DIR}"/70247_maven-gpg-keyring "${GPG_HOMEDIR}"/secring.gpg
  SONATYPE_USERNAME=$(cat "${KOKORO_KEYSTORE_DIR}"/70247_sonatype-credentials | cut -f1 -d'|')
  export SONATYPE_USERNAME
  SONATYPE_PASSWORD=$(cat "${KOKORO_KEYSTORE_DIR}"/70247_sonatype-credentials | cut -f2 -d'|')
  export SONATYPE_PASSWORD
}

create_settings_xml_file() {
  echo "<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>${SONATYPE_USERNAME}</username>
      <password>${SONATYPE_PASSWORD}</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>${SONATYPE_USERNAME}</username>
      <password>${SONATYPE_PASSWORD}</password>
    </server>
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>${SONATYPE_USERNAME}</username>
      <password>${SONATYPE_PASSWORD}</password>
    </server>
  </servers>
</settings>" > "$1"
}

create_gradle_properties_file() {
  echo "
signing.gnupg.executable=gpg
signing.gnupg.homeDir=${GPG_HOMEDIR}
signing.gnupg.keyName=${GPG_KEY_ID}
signing.gnupg.passphrase=${GPG_PASSPHRASE}

ossrhUsername=${SONATYPE_USERNAME}
ossrhPassword=${SONATYPE_PASSWORD}" > $1
}
