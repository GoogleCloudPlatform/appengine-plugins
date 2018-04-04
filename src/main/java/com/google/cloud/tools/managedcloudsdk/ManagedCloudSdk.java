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

import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkComponent;
import com.google.cloud.tools.managedcloudsdk.command.CommandCaller;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponentInstaller;
import com.google.cloud.tools.managedcloudsdk.components.SdkUpdater;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstaller;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/** A manager for installing, configuring and updating the Cloud SDK. */
public class ManagedCloudSdk {

  private static final Logger logger = Logger.getLogger(ManagedCloudSdk.class.getName());

  private final Version version;
  private final Path managedSdkDirectory;
  private final OsInfo osInfo;

  /** Instantiated with {@link ManagedCloudSdk#newManagedSdk}. */
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
  public boolean isInstalled()
      throws ManagedSdkVerificationException, ManagedSdkVersionMismatchException {
    if (getSdkHome() == null) {
      return false;
    }
    if (!Files.isDirectory(getSdkHome())) {
      return false;
    }
    if (!Files.isRegularFile(getGcloud())) {
      return false;
    }
    // Verify the versions match up for fixed version installs
    if (version != Version.LATEST) {
      try {
        String versionFileContents =
            new String(Files.readAllBytes(getSdkHome().resolve("VERSION"))).trim();
        if (!versionFileContents.equals(version.getVersion())) {
          throw new ManagedSdkVersionMismatchException(
              "Installed sdk version: "
                  + versionFileContents
                  + " does not match expected version: "
                  + version.getVersion()
                  + ".");
        }
      } catch (IOException ex) {
        throw new ManagedSdkVerificationException(ex);
      }
    }
    return true;
  }

  /**
   * Query gcloud to see if component is installed. Uses gcloud's '--local-state-only' to avoid
   * network accesses.
   */
  public boolean hasComponent(SdkComponent component) throws ManagedSdkVerificationException {
    if (!Files.isRegularFile(getGcloud())) {
      return false;
    }

    List<String> listComponentCommand =
        Arrays.asList(
            getGcloud().toString(),
            "components",
            "list",
            "--only-local-state",
            "--format=json",
            "--filter=id:" + component);

    try {
      String result = CommandCaller.newCaller().call(listComponentCommand, null, null);
      List<CloudSdkComponent> components = CloudSdkComponent.fromJsonList(result);
      if (components.size() > 1) {
        // not a unique component id
        throw new ManagedSdkVerificationException("Invalid component " + component);
      }
      return !components.isEmpty();
    } catch (CommandExecutionException | InterruptedException | CommandExitException ex) {
      throw new ManagedSdkVerificationException(ex);
    }
  }

  /** Query gcloud to see if sdk is up to date. Gcloud makes a call to the server to check this. */
  public boolean isUpToDate() throws ManagedSdkVerificationException {
    if (!Files.isRegularFile(getGcloud())) {
      return false;
    }

    if (version != Version.LATEST) {
      return true;
    }

    List<String> updateAvailableCommand =
        Arrays.asList(
            getGcloud().toString(),
            "components",
            "list",
            "--format=json",
            "--filter=state.name:Update Available");

    try {
      String result = CommandCaller.newCaller().call(updateAvailableCommand, null, null);
      for (CloudSdkComponent component : CloudSdkComponent.fromJsonList(result)) {
        if (component.getState().getName().equals("Update Available")) {
          return false;
        }
      }
      return true;
    } catch (CommandExecutionException | InterruptedException | CommandExitException ex) {
      throw new ManagedSdkVerificationException(ex);
    }
  }

  // TODO : fix passthrough for useragent and client side usage reporting
  public SdkInstaller newInstaller() {
    String userAgentString = "google-cloud-tools-java";
    return SdkInstaller.newInstaller(managedSdkDirectory, version, osInfo, userAgentString, false);
  }

  public SdkComponentInstaller newComponentInstaller() {
    return SdkComponentInstaller.newComponentInstaller(osInfo.name(), getGcloud());
  }

  /**
   * For "LATEST" version SDKs, the client tooling must keep the SDK up-to-date manually, check with
   * {@link #isUpToDate()} before using, returns a new updater if sdk is "LATEST", it will throw a
   * {@link UnsupportedOperationException} if SDK is a fixed version (fixed versions should never be
   * udpated).
   */
  public SdkUpdater newUpdater() {
    if (version != Version.LATEST) {
      throw new UnsupportedOperationException("Cannot update a fixed version SDK.");
    }
    return SdkUpdater.newUpdater(osInfo.name(), getGcloud());
  }

  /** Get a new {@link ManagedCloudSdk} instance for @{link Version} specified. */
  public static ManagedCloudSdk newManagedSdk(Version version) throws UnsupportedOsException {
    OsInfo osInfo = OsInfo.getSystemOsInfo();
    return new ManagedCloudSdk(
        version,
        getOsSpecificManagedSdkHome(osInfo.name(), System.getProperties(), System.getenv()),
        osInfo);
  }

  /** Convenience method to obtain a new LATEST {@link ManagedCloudSdk} instance. */
  public static ManagedCloudSdk newManagedSdk() throws UnsupportedOsException {
    return newManagedSdk(Version.LATEST);
  }

  @VisibleForTesting
  static Path getOsSpecificManagedSdkHome(
      OsInfo.Name osName, Properties systemProperties, Map<String, String> environment) {
    Path userHome = Paths.get(systemProperties.getProperty("user.home"));
    Path cloudSdkPartialPath = Paths.get("google-cloud-tools-java", "managed-cloud-sdk");
    Path xdgPath = userHome.resolve(".cache").resolve(cloudSdkPartialPath);

    switch (osName) {
      case WINDOWS:
        String localAppDataEnv = environment.get("LOCALAPPDATA");
        if (localAppDataEnv == null || localAppDataEnv.trim().isEmpty()) {
          logger.warning("LOCALAPPDATA environment is invalid or missing");
          return xdgPath;
        }
        Path localAppData = Paths.get(localAppDataEnv);
        if (!Files.exists(localAppData)) {
          logger.warning(localAppData.toString() + " does not exist");
          return xdgPath;
        }
        return localAppData.resolve(cloudSdkPartialPath);

      case MAC:
        Path applicationSupport = userHome.resolve("Library").resolve("Application Support");
        if (!Files.exists(applicationSupport)) {
          logger.warning(applicationSupport.toString() + " does not exist");
          return xdgPath;
        }
        return applicationSupport.resolve(cloudSdkPartialPath);

      case LINUX:
        return xdgPath;

      default:
        // we can't actually get here unless we modify the enum OsInfo.Name
        throw new RuntimeException("OsName is not valid : " + osName);
    }
  }
}
