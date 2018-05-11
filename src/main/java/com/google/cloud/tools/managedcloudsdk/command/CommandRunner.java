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

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutor;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutorFactory;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Execute a command and redirect output to handlers. */
public class CommandRunner {
  private final ProcessExecutorFactory processExecutorFactory;
  private final AsyncStreamHandlerFactory streamHandlerFactory;

  @VisibleForTesting
  CommandRunner(
      ProcessExecutorFactory processExecutorFactory,
      AsyncStreamHandlerFactory streamHandlerFactory) {
    this.processExecutorFactory = processExecutorFactory;
    this.streamHandlerFactory = streamHandlerFactory;
  }

  /** Run the command and wait for completion. */
  public void run(
      List<String> command,
      @Nullable Path workingDirectory,
      @Nullable Map<String, String> environment,
      ConsoleListener consoleListener)
      throws InterruptedException, CommandExitException, CommandExecutionException {
    ProcessExecutor processExecutor = processExecutorFactory.newProcessExecutor();
    try {
      int exitCode =
          processExecutor.run(
              command,
              workingDirectory,
              environment,
              streamHandlerFactory.newHandler(consoleListener),
              streamHandlerFactory.newHandler(consoleListener));
      if (exitCode != 0) {
        throw new CommandExitException(exitCode);
      }
    } catch (IOException ex) {
      throw new CommandExecutionException(ex);
    }
  }

  public static CommandRunner newRunner() {
    return new CommandRunner(new ProcessExecutorFactory(), new AsyncStreamHandlerFactory());
  }
}
