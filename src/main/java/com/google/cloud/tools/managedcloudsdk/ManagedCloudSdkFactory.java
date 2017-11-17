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

package com.google.cloud.tools.managedcloudsdk;

import java.nio.file.Path;
import java.nio.file.Paths;

/** {@link ManagedCloudSdk} factory. */
public class ManagedCloudSdkFactory {

  private Path managedSdkDirectory;
  private OsInfo osInfo;

  /** Create a new factory with hardcoded managed sdk root directory. */
  public ManagedCloudSdkFactory() throws UnsupportedOsException {
    managedSdkDirectory =
        Paths.get(System.getProperty("user.home"), ".google-cloud-tools-java", "managed-cloud-sdk");
    osInfo = OsInfo.getSystemOsInfo();
  }

  /** Get a new {@link ManagedCloudSdk} instance for @{link Version} specified. */
  public ManagedCloudSdk getManagedSdk(Version version) throws UnsupportedOsException {
    return new ManagedCloudSdk(version, managedSdkDirectory, osInfo);
  }

  /** Convenience method to obtain a new LATEST {@link ManagedCloudSdk} instance. */
  public ManagedCloudSdk getManagedSdk() throws UnsupportedOsException {
    return getManagedSdk(Version.LATEST);
  }
}
