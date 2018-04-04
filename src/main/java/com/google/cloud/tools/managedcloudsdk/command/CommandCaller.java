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

import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutor;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutorFactory;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/** Execute a command synchronously and save and return stdout. */
public class CommandCaller {
  private final ProcessExecutorFactory processExecutorFactory;
  private final AsyncStreamSaverFactory streamSaverFactory;

  @VisibleForTesting
  CommandCaller(
      ProcessExecutorFactory processExecutorFactory, AsyncStreamSaverFactory streamSaverFactory) {
    this.processExecutorFactory = processExecutorFactory;
    this.streamSaverFactory = streamSaverFactory;
  }

  /** Runs the command and returns process's stdout stream as a string. */
  public String call(
      List<String> command,
      @Nullable Path workingDirectory,
      @Nullable Map<String, String> environment)
      throws CommandExitException, CommandExecutionException, InterruptedException {
    ProcessExecutor processExecutor = processExecutorFactory.newProcessExecutor();

    AsyncStreamSaver stdOutSaver = streamSaverFactory.newSaver();
    AsyncStreamSaver stdErrSaver = streamSaverFactory.newSaver();

    try {
      int exitCode =
          processExecutor.run(command, workingDirectory, environment, stdOutSaver, stdErrSaver);
      if (exitCode != 0) {
        String stdOut;
        String stdErr;
        try {
          stdOut = stdOutSaver.getResult().get();
        } catch (InterruptedException ignored) {
          stdOut = "stdout collection interrupted";
        }
        try {
          stdErr = stdErrSaver.getResult().get();
        } catch (InterruptedException ignored) {
          stdErr = "stderr collection interrupted";
        }
        throw new CommandExitException(exitCode, stdOut + "\n" + stdErr);
      }
      return stdOutSaver.getResult().get();
    } catch (IOException | ExecutionException ex) {
      throw new CommandExecutionException(ex);
    }
  }

  public static CommandCaller newCaller() {
    return new CommandCaller(new ProcessExecutorFactory(), new AsyncStreamSaverFactory());
  }
}
