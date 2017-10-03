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

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of {@link Installer} for 'LATEST' version cloud SDK.
 *
 * <p>The archive downloaded provides an install scrip that downloads OS and architecture specific
 * components.
 */
public final class LatestInstaller<T extends InstallScriptProvider> implements Installer {

  private final Path installedSdkRoot;
  private final InstallScriptProvider installScriptProvider;
  private final boolean usageReporting;
  private final InstallProcessStreamHandler installProcessStreamHandler;
  private final ProcessBuilderFactory processBuilderFactory;

  /** Instantiated by {@link InstallerFactory}. */
  LatestInstaller(
      Path installedSdkRoot,
      InstallScriptProvider installScriptProvider,
      boolean usageReporting,
      InstallProcessStreamHandler installProcessStreamHandler,
      ProcessBuilderFactory processBuilderFactory) {
    this.installedSdkRoot = installedSdkRoot;
    this.installScriptProvider = installScriptProvider;
    this.usageReporting = usageReporting;
    this.installProcessStreamHandler = installProcessStreamHandler;
    this.processBuilderFactory = processBuilderFactory;
  }

  @Override
  public Path call() throws IOException, ExecutionException {

    List<String> command = new ArrayList<>(installScriptProvider.getScriptCommandLine());
    // now configure parameters (not OS specific)
    command.add("--path-update=false"); // don't update user's path
    command.add("--command-completion=false"); // don't add command completion
    command.add("--quiet"); // don't accept user input during install
    command.add("--usage-reporting=" + usageReporting); // usageReportingPassthrough

    ProcessBuilder pb = processBuilderFactory.newProcessBuilder();
    pb.command(command);
    pb.directory(installedSdkRoot.toFile());
    if (installProcessStreamHandler == null) {
      pb.inheritIO();
    }
    Process installProcess = pb.start();

    if (installProcessStreamHandler != null) {
      installProcessStreamHandler.handleStreams(
          installProcess.getInputStream(), installProcess.getErrorStream());
    }

    try {
      installProcess.waitFor();
    } catch (InterruptedException e) {
      installProcess.destroy();
      throw new ExecutionException("Process cancelled", new Throwable());
    }
    if (installProcess.exitValue() != 0) {
      throw new ExecutionException(
          "Process exited with non zero: " + installProcess.exitValue(), new Throwable());
    }

    return installedSdkRoot;
  }

  @VisibleForTesting
  InstallScriptProvider getInstallScriptProvider() {
    return installScriptProvider;
  }
}
