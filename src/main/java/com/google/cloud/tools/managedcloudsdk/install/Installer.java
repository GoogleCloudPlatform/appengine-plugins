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
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Installer for running install scripts in a Cloud SDK download. */
final class Installer {

  private final Path installedSdkRoot;
  private final InstallScriptProvider installScriptProvider;
  private final boolean usageReporting;
  private final ProgressListener progressListener;
  private final ConsoleListener consoleListener;
  private final CommandRunner commandRunner;

  /** Instantiated by {@link InstallerFactory}. */
  Installer(
      Path installedSdkRoot,
      InstallScriptProvider installScriptProvider,
      boolean usageReporting,
      ProgressListener progressListener,
      ConsoleListener consoleListener,
      CommandRunner commandRunner) {
    this.installedSdkRoot = installedSdkRoot;
    this.installScriptProvider = installScriptProvider;
    this.usageReporting = usageReporting;
    this.progressListener = progressListener;
    this.consoleListener = consoleListener;
    this.commandRunner = commandRunner;
  }

  /** Install a cloud sdk (only run this on LATEST). */
  public void install()
      throws CommandExitException, CommandExecutionException, InterruptedException {
    List<String> command =
        new ArrayList<>(installScriptProvider.getScriptCommandLine(installedSdkRoot));
    command.add("--path-update=false"); // don't update user's path
    command.add("--command-completion=false"); // don't add command completion
    command.add("--quiet"); // don't accept user input during install
    command.add("--usage-reporting=" + usageReporting); // usage reporting passthrough

    Path workingDirectory = installedSdkRoot.getParent();
    Map<String, String> installerEnvironment = installScriptProvider.getScriptEnvironment();

    progressListener.start("Installing Cloud SDK", ProgressListener.UNKNOWN);
    commandRunner.run(command, workingDirectory, installerEnvironment, consoleListener);
    progressListener.done();
  }

  @VisibleForTesting
  InstallScriptProvider getInstallScriptProvider() {
    return installScriptProvider;
  }
}
