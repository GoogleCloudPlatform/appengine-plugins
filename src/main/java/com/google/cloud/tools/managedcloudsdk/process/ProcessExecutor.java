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

package com.google.cloud.tools.managedcloudsdk.process;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Executes a shell command. */
public class ProcessExecutor {

  private static final Logger logger = Logger.getLogger(ProcessExecutor.class.getName());

  private ProcessBuilderFactory processBuilderFactory = new ProcessBuilderFactory();

  @VisibleForTesting
  ProcessExecutor setProcessBuilderFactory(ProcessBuilderFactory processBuilderFactory) {
    this.processBuilderFactory = processBuilderFactory;
    return this;
  }

  @VisibleForTesting
  static class ProcessBuilderFactory {
    ProcessBuilder createProcessBuilder() {
      return new ProcessBuilder();
    }
  }

  /**
   * Runs the command.
   *
   * @param command list of command line tokens
   * @param workingDirectory the working directory to run the command from
   * @param environment a map of environment variables
   * @param stdout a stdout stream handler that must run on a separate thread
   * @param stderr a stderr stream handler that must run on a separate thread
   * @return exit code from the process
   */
  public int run(
      List<String> command,
      @Nullable Path workingDirectory,
      @Nullable Map<String, String> environment,
      AsyncStreamHandler stdout,
      AsyncStreamHandler stderr)
      throws IOException, InterruptedException {

    logger.fine("Running command : " + command);
    if (workingDirectory != null) {
      logger.fine("In working directory : " + workingDirectory.toString());
    }
    if (environment != null && environment.size() > 0) {
      logger.fine("With environment : " + environment);
    }

    // Builds the command to execute.
    ProcessBuilder processBuilder = processBuilderFactory.createProcessBuilder();
    processBuilder.command(command);
    if (workingDirectory != null) {
      processBuilder.directory(workingDirectory.toFile());
    }
    if (environment != null) {
      processBuilder.environment().putAll(environment);
    }
    final Process process = processBuilder.start();

    stdout.handleStream(process.getInputStream());
    stderr.handleStream(process.getErrorStream());

    int exitCode;
    try {
      exitCode = process.waitFor();
    } catch (InterruptedException ex) {
      process.destroy();
      throw ex; // rethrow after cleanup
    }

    return exitCode;
  }
}
