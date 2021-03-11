/*
 * Copyright 2017 Google LLC.
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

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.OsInfo;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/** {@link Installer} Factory. */
final class InstallerFactory {

  private final OsInfo osInfo;
  private final boolean usageReporting;
  private final @Nullable Set<String> overrideComponents;

  /**
   * Creates a new factory.
   *
   * @param osInfo the operating system of the computer this script is running on
   * @param usageReporting enable or disable client side usage reporting. {@code true} is enabled,
   *     {@code false} is disabled
   * @param overrideComponents gcloud components to install instead of the defaults
   */
  InstallerFactory(
      OsInfo osInfo, boolean usageReporting, @Nullable Set<String> overrideComponents) {
    this.osInfo = osInfo;
    this.usageReporting = usageReporting;
    this.overrideComponents = overrideComponents;
  }

  /**
   * Creates a new factory.
   *
   * @param osInfo the operating system of the computer this script is running on
   * @param usageReporting enable or disable client side usage reporting. {@code true} is enabled,
   *     {@code false} is disabled
   */
  InstallerFactory(OsInfo osInfo, boolean usageReporting) {
    this(osInfo, usageReporting, null);
  }

  /**
   * Returns a new {@link Installer} instance.
   *
   * @param installedSdkRoot path to the Cloud SDK directory
   * @param progressListener listener on installer script output
   * @param environmentVariables Additional environment variables to be passed on to the installer
   *     process (proxy settings, etc.)
   * @return a {@link Installer} instance.
   */
  Installer newInstaller(
      Path installedSdkRoot,
      ProgressListener progressListener,
      ConsoleListener consoleListener,
      Map<String, String> environmentVariables) {

    return new Installer(
        installedSdkRoot,
        getInstallScriptProvider(environmentVariables),
        usageReporting,
        overrideComponents,
        progressListener,
        consoleListener,
        CommandRunner.newRunner());
  }

  Installer newInstaller(
      Path installedSdkRoot, ProgressListener progressListener, ConsoleListener consoleListener) {
    return newInstaller(
        installedSdkRoot, progressListener, consoleListener, Collections.emptyMap());
  }

  private InstallScriptProvider getInstallScriptProvider(Map<String, String> environmentVariables) {
    switch (osInfo.name()) {
      case WINDOWS:
        return new WindowsInstallScriptProvider(environmentVariables);
      case MAC:
      case LINUX:
      default:
        return new UnixInstallScriptProvider(environmentVariables);
    }
  }
}
