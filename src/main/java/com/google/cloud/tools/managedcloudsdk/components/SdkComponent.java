/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.managedcloudsdk.components;

/** Components in the Cloud SDK. */
public enum SdkComponent {
  ALPHA("alpha"),
  APP_ENGINE_GO("app-engine-go"),
  APP_ENGINE_JAVA("app-engine-java"),
  APP_ENGINE_PHP("app-engine-php"),
  APP_ENGINE_PYTHON("app-engine-python"),
  BETA("beta"),
  BIGTABLE("bigtable"),
  BQ("bq"),
  CBT("cbt"),
  CLOUD_DATASTORE_EMULATOR("cloud-datastore-emulator"),
  CONTAINER_BUILDER_LOCAL("container-builder-local"),
  CORE("core"),
  DATALAB("datalab"),
  DOCKER_CREDENTIAL_GCR("docker-credential-gcr"),
  EMULATOR_REVERSE_PROXY("emulator-reverse-proxy"),
  GCD_EMULATOR("gcd-emulator"),
  GSUTIL("gsutil"),
  KUBECTL("kubectl"),
  PUBSUB_EMULATOR("pubsub-emulator");

  private final String value;

  SdkComponent(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
