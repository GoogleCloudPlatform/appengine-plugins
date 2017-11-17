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

import static com.google.cloud.tools.managedcloudsdk.OsInfo.Name.WINDOWS;

import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponentInstaller;
import com.google.cloud.tools.managedcloudsdk.gcloud.GcloudCommandFactory;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstaller;
import com.google.cloud.tools.managedcloudsdk.process.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.update.SdkUpdater;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** A managed Cloud Sdk for installing, configuring and updating the Cloud Sdk. */
public class ManagedCloudSdk {

  private final Version version;
  private final Path managedSdkDirectory;
  private final OsInfo osInfo;

  /** Instantiated with {@link ManagedCloudSdkFactory}. */
  ManagedCloudSdk(Version version, Path managedSdkDirectory, OsInfo osInfo) {
    this.version = version;
    this.managedSdkDirectory = managedSdkDirectory;
    this.osInfo = osInfo;
  }

  public Path getSdkHome() {
    return managedSdkDirectory.resolve(version.getVersion()).resolve("google-cloud-sdk");
  }

  /** Returns a path to gcloud executable (operating system specific). */
  public Path getGcloud() {
    return getSdkHome()
        .resolve("bin")
        .resolve(osInfo.name().equals(WINDOWS) ? "gcloud.cmd" : "gcloud");
  }

  /** Simple check to verify Cloud SDK installed by verifying the existence of gcloud. */
  public boolean isInstalled() {
    if (getSdkHome() == null) {
      return false;
    }
    if (!Files.isDirectory(getSdkHome())) {
      return false;
    }
    if (!Files.isRegularFile(getGcloud())) {
      return false;
    }
    return true;
  }

  /**
   * Query gcloud to see if component is installed. Gcloud makes a call to the server to check this,
   * in future version we can explore the use of '--local-state-only' but that's a relatively new
   * flag and may not work for all cases.
   */
  public boolean hasComponent(SdkComponent component)
      throws IOException, ExecutionException, CommandExitException {
    if (!isInstalled()) {
      return false;
    }
    List<String> listComponentCommand =
        Arrays.asList(
            "components",
            "list",
            "--format=json",
            "--filter=id:" + component.toString() + " AND state.name:Not Installed");

    String result = new GcloudCommandFactory(getGcloud()).newCaller(listComponentCommand).call();
    return !result.contains(component.toString());
  }

  /** Query gcloud to see if sdk is up to date. Gcloud makes a call to the server to check this. */
  public boolean isUpToDate() throws IOException, ExecutionException, CommandExitException {
    if (!isInstalled()) {
      return false;
    }
    if (version != Version.LATEST) {
      return true;
    }

    List<String> updateAvailableCommand =
        Arrays.asList(
            "components", "list", "--format=json", "--filter=state.name:Update Available");

    String result = new GcloudCommandFactory(getGcloud()).newCaller(updateAvailableCommand).call();
    return !result.contains("Update Available");
  }

  // TODO : fix passthrough for useragent and client side usage reporting
  public SdkInstaller newInstaller() throws UnsupportedOsException {
    String userAgentString = "google-cloud-tools-java";
    return SdkInstaller.newInstaller(managedSdkDirectory, version, osInfo, userAgentString, false);
  }

  public SdkComponentInstaller newComponentInstaller() {
    return SdkComponentInstaller.newComponentInstaller(getGcloud());
  }

  /** Return a new updater if a 'LATEST' sdk, returns {@code null} for versioned SDKs. */
  public SdkUpdater newUpdater() {
    if (version != Version.LATEST) {
      return null;
    }
    return SdkUpdater.newUpdater(getGcloud());
  }
}
