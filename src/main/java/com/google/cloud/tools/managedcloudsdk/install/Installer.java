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

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamConsumer;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutor;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutorFactory;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Installer for running install scripts in a Cloud SDK download. */
final class Installer<T extends InstallScriptProvider> {

  private final Path installedSdkRoot;
  private final InstallScriptProvider installScriptProvider;
  private final boolean usageReporting;
  private final MessageListener messageListener;
  private final CommandExecutorFactory commandExecutorFactory;
  private final AsyncStreamConsumer<Void> stdOutListener;
  private final AsyncStreamConsumer<Void> stdErrListener;

  /** Instantiated by {@link InstallerFactory}. */
  Installer(
      Path installedSdkRoot,
      InstallScriptProvider installScriptProvider,
      boolean usageReporting,
      MessageListener messageListener,
      CommandExecutorFactory commandExecutorFactory,
      AsyncStreamConsumer<Void> stdOutListener,
      AsyncStreamConsumer<Void> stdErrListener) {
    this.installedSdkRoot = installedSdkRoot;
    this.installScriptProvider = installScriptProvider;
    this.usageReporting = usageReporting;
    this.messageListener = messageListener;
    this.commandExecutorFactory = commandExecutorFactory;
    this.stdOutListener = stdOutListener;
    this.stdErrListener = stdErrListener;
  }

  /** Install a cloud sdk (only run this on LATEST). */
  public void install() throws IOException, ExecutionException {

    List<String> command = new ArrayList<>(installScriptProvider.getScriptCommandLine());
    // now configure parameters (not OS specific)
    command.add("--path-update=false"); // don't update user's path
    command.add("--command-completion=false"); // don't add command completion
    command.add("--quiet"); // don't accept user input during install
    command.add("--usage-reporting=" + usageReporting); // usageReportingPassthrough

    CommandExecutor commandExecutor = commandExecutorFactory.newCommandExecutor(messageListener);
    commandExecutor.setWorkingDirectory(installedSdkRoot);

    int exitCode = commandExecutor.run(command, stdOutListener, stdErrListener);
    if (exitCode != 0) {
      throw new ExecutionException(
          "Installer exited with non-zero exit code: " + exitCode, new Throwable());
    }
    try {
      stdErrListener.getResult().get();
      stdOutListener.getResult().get();
    } catch (InterruptedException e) {
      messageListener.messageLn("Output interrupted...");
    }
  }

  @VisibleForTesting
  InstallScriptProvider getInstallScriptProvider() {
    return installScriptProvider;
  }
}
