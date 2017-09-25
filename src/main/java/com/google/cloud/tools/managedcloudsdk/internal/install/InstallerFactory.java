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

package com.google.cloud.tools.managedcloudsdk.internal.install;

import com.google.cloud.tools.managedcloudsdk.Version;
import com.google.cloud.tools.managedcloudsdk.internal.OsType;
import java.nio.file.Path;

/** {@link Installer} Factory. */
public final class InstallerFactory {

  private final Version version;
  private final OsType os;
  private final boolean usageReporting;

  /**
   * Creates a new factory.
   *
   * @param version Version of the Cloud SDK
   * @param os The operating system of the computer this script is running on
   * @param usageReporting Enable or disable client side usage reporting {@code true} is enabled,
   *     {@code false} is disabled
   */
  public InstallerFactory(Version version, OsType os, boolean usageReporting) {
    this.version = version;
    this.os = os;
    this.usageReporting = usageReporting;
  }

  /**
   * Returns a new {@link Installer} instance.
   *
   * @param installedSdkRoot Path to the Cloud SDK directory
   * @param installInstallProcessStreamHandler Listener on installer script output
   * @return a {@link LatestInstaller} instance if factory configured with 'LATEST' version or
   *     {@link NoOpInstaller} if factory is configured with a fixed version.
   */
  public Installer newInstaller(
      Path installedSdkRoot, InstallProcessStreamHandler installInstallProcessStreamHandler) {

    if (version.equals(Version.LATEST)) {
      return new LatestInstaller(
          installedSdkRoot,
          getInstallScriptProvider(),
          usageReporting,
          installInstallProcessStreamHandler,
          new ProcessBuilderFactory());
    }
    return new NoOpInstaller(installedSdkRoot);
  }

  private InstallScriptProvider getInstallScriptProvider() {
    switch (os) {
      case WINDOWS:
        return new WindowsInstallScriptProvider();
      case MAC:
      case LINUX:
        return new NixInstallScriptProvider();
    }

    throw new IllegalStateException("Programmer Error");
  }
}
