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

package com.google.cloud.tools.managedcloudsdk.gcloud;

import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutor;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutorFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GcloudCommand {
  private final Path gcloud;
  private final List<String> parameters;
  private final CommandExecutorFactory commandExecutorFactory;
  private final AsyncStreamHandler<Void> stdOutListener;
  private final AsyncStreamHandler<Void> stdErrListener;

  GcloudCommand(
      Path gcloud,
      List<String> parameters,
      CommandExecutorFactory commandExecutorFactory,
      AsyncStreamHandler<Void> stdOutListener,
      AsyncStreamHandler<Void> stdErrListener) {
    this.gcloud = gcloud;
    this.parameters = parameters;
    this.commandExecutorFactory = commandExecutorFactory;
    this.stdOutListener = stdOutListener;
    this.stdErrListener = stdErrListener;
  }

  /** Run the command. */
  public void run() throws IOException, ExecutionException, GcloudCommandExitException {

    List<String> command = new ArrayList<>();
    command.add(gcloud.toString());
    command.addAll(parameters);

    CommandExecutor commandExecutor = commandExecutorFactory.newCommandExecutor();

    int exitCode = commandExecutor.run(command, stdOutListener, stdErrListener);
    if (exitCode != 0) {
      throw new GcloudCommandExitException("gcloud exited with non-zero exit code: " + exitCode);
    }
    try {
      stdErrListener.getResult().get();
      stdOutListener.getResult().get();
    } catch (InterruptedException e) {
      throw new ExecutionException("Output consumers interrupted.", e);
    }
  }
}
