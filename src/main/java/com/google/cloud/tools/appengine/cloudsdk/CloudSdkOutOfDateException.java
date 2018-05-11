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
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;

/** The Cloud SDK that was found is too old (generally before 133.0.0). */
public class CloudSdkOutOfDateException extends AppEngineException {

  private static final String MESSAGE = "Cloud SDK versions before %s are not supported";

  @Nullable private CloudSdkVersion installedVersion;
  private final CloudSdkVersion requiredVersion;

  /**
   * Installed version is too old.
   *
   * @param installedVersion version of the Cloud SDK we found
   * @param requiredVersion minimum version of the Cloud SDK we want
   */
  public CloudSdkOutOfDateException(
      CloudSdkVersion installedVersion, CloudSdkVersion requiredVersion) {
    super(
        "Requires version " + requiredVersion + " or later but found version " + installedVersion);
    this.requiredVersion = Preconditions.checkNotNull(requiredVersion);
    this.installedVersion = Preconditions.checkNotNull(installedVersion);
  }

  /**
   * Installed version predates version files in the Cloud SDK.
   *
   * @param requiredVersion minimum version of the Cloud SDK we want
   */
  public CloudSdkOutOfDateException(CloudSdkVersion requiredVersion) {
    super(String.format(MESSAGE, requiredVersion.toString()));
    this.requiredVersion = requiredVersion;
  }

  /**
   * Returns the minimum required version of the cloud SDK for the current operation.
   *
   * @return minimum acceptable version of the Cloud SDK
   */
  public CloudSdkVersion getRequiredVersion() {
    return requiredVersion;
  }

  /**
   * Returns the version of the local cloud SDK if known, otherwise null.
   *
   * @return actual version of the Cloud SDK, or null if it's really old.
   */
  @Nullable
  public CloudSdkVersion getInstalledVersion() {
    return installedVersion;
  }
}
