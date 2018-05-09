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

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.OsInfo;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.nio.file.Path;

/** {@link Installer} Factory. */
final class InstallerFactory {

  private final OsInfo osInfo;
  private final boolean usageReporting;

  /**
   * Creates a new factory.
   *
   * @param osInfo the operating system of the computer this script is running on
   * @param usageReporting enable or disable client side usage reporting {@code true} is enabled,
   *     {@code false} is disabled
   */
  InstallerFactory(OsInfo osInfo, boolean usageReporting) {
    this.osInfo = osInfo;
    this.usageReporting = usageReporting;
  }

  /**
   * Returns a new {@link Installer} instance.
   *
   * @param installedSdkRoot path to the Cloud SDK directory
   * @param progressListener listener on installer script output
   * @return a {@link Installer} instance.
   */
  Installer newInstaller(
      Path installedSdkRoot, ProgressListener progressListener, ConsoleListener consoleListener) {

    return new Installer(
        installedSdkRoot,
        getInstallScriptProvider(),
        usageReporting,
        progressListener,
        consoleListener,
        CommandRunner.newRunner());
  }

  private InstallScriptProvider getInstallScriptProvider() {
    switch (osInfo.name()) {
      case WINDOWS:
        return new WindowsInstallScriptProvider();
      case MAC:
      case LINUX:
      default:
        return new UnixInstallScriptProvider();
    }
  }
}
