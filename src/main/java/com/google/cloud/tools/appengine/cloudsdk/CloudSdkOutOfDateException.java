/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;

/**
 * The Cloud SDK that was found is too old (generally before 133.0.0).
 */
public class CloudSdkOutOfDateException extends AppEngineException {

  private CloudSdkVersion requiredVersion;

  public CloudSdkOutOfDateException(String message, CloudSdkVersion requiredVersion) {
    super(message);
    this.requiredVersion = requiredVersion;
  }

  public CloudSdkVersion getRequiredVersion() {
    return requiredVersion;
  }

}
