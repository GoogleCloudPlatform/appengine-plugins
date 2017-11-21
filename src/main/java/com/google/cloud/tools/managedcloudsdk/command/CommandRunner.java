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

package com.google.cloud.tools.managedcloudsdk.command;

import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutor;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutorFactory;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Execute a command and redirect output to handlers. */
public class CommandRunner implements CommandExecutor<Void> {
  private final List<String> command;
  private final Path workingDirectory;
  private final Map<String, String> environment;
  private final ProcessExecutorFactory processExecutorFactory;
  private final AsyncStreamHandler stdOutListener;
  private final AsyncStreamHandler stdErrListener;

  /**
   * Create a new Command Runner.
   *
   * @param command a command to run
   * @param workingDirectory the working directory to run in, can be {@code null}
   * @param environment map of environment variables, can be {@code null}
   */
  public CommandRunner(
      List<String> command,
      Path workingDirectory,
      Map<String, String> environment,
      ProcessExecutorFactory processExecutorFactory,
      AsyncStreamHandler stdOutListener,
      AsyncStreamHandler stdErrListener) {
    this.command = ImmutableList.copyOf(command);
    this.workingDirectory = workingDirectory;
    this.environment = environment;
    this.processExecutorFactory = processExecutorFactory;
    this.stdOutListener = stdOutListener;
    this.stdErrListener = stdErrListener;
  }

  /** Run the command and wait for completion. */
  @Override
  public Void execute() throws IOException, ExecutionException, CommandExitException {
    ProcessExecutor processExecutor = processExecutorFactory.newCommandExecutor();

    int exitCode =
        processExecutor.run(command, workingDirectory, environment, stdOutListener, stdErrListener);
    if (exitCode != 0) {
      throw new CommandExitException("Process exited with non-zero exit code: " + exitCode);
    }

    return null;
  }
}
