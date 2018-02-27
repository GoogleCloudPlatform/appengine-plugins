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

package com.google.cloud.tools.managedcloudsdk.install;

import static com.google.cloud.tools.managedcloudsdk.OsInfo.Architecture.X86_64;

import com.google.cloud.tools.managedcloudsdk.OsInfo;
import com.google.cloud.tools.managedcloudsdk.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/** Factory for {@link FileResourceProvider}. * */
class FileResourceProviderFactory {

  static final String LATEST_BASE_URL = "https://dl.google.com/dl/cloudsdk/channels/rapid/";
  static final String VERSIONED_BASE_URL = "https://storage.googleapis.com/cloud-sdk-release/";

  private final Version version;
  private final OsInfo os;
  private final Path managedSdkDirectory;

  /**
   * Create a new factory.
   *
   * @param version cloud SDK version
   * @param osInfo the target operating system information
   * @param managedSdkDirectory the managed sdk base directory
   */
  public FileResourceProviderFactory(Version version, OsInfo osInfo, Path managedSdkDirectory) {
    this.version = version;
    this.os = osInfo;
    this.managedSdkDirectory = managedSdkDirectory;
  }

  public FileResourceProvider newFileResourceProvider() throws MalformedURLException {
    Path downloads = managedSdkDirectory.resolve("downloads");
    if (version.equals(Version.LATEST)) {
      return new FileResourceProvider(
          new URL(LATEST_BASE_URL + getLatestFilename()),
          downloads.resolve(getLatestFilename()),
          managedSdkDirectory.resolve(version.getVersion()),
          getGcloudExecutableName());
    } else { // versioned
      return new FileResourceProvider(
          new URL(VERSIONED_BASE_URL + getVersionedFilename()),
          downloads.resolve(getVersionedFilename()),
          managedSdkDirectory.resolve(version.getVersion()),
          getGcloudExecutableName());
    }
  }

  private String getLatestFilename() {
    switch (os.name()) {
      case WINDOWS:
        String architecture = os.arch().equals(X86_64) ? "-x86_64-" : "-";
        return "google-cloud-sdk-windows" + architecture + "bundled-python.zip";
      default:
        return "google-cloud-sdk.tar.gz";
    }
  }

  private String getVersionedFilename() {
    return "google-cloud-sdk-" + version.getVersion() + "-" + getVersionedOsExtension();
  }

  private String getVersionedOsExtension() {
    String architecture = os.arch().equals(X86_64) ? "x86_64" : "x86";
    switch (os.name()) {
      case WINDOWS:
        return "windows-" + architecture + "-bundled-python.zip";
      case MAC:
        return "darwin-" + architecture + ".tar.gz";
      case LINUX:
        return "linux-" + architecture + ".tar.gz";
      default:
        // we can't actually get here
        throw new RuntimeException();
    }
  }

  private String getGcloudExecutableName() {
    switch (os.name()) {
      case WINDOWS:
        return "gcloud.cmd";
      default:
        return "gcloud";
    }
  }
}
