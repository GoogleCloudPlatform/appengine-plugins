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

package com.google.cloud.tools.managedcloudsdk.update;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutor;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutorFactory;
import com.google.common.base.Joiner;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Gcloud wrapper for updating the Cloud SDK. */
final class Updater {

  private final Path gcloud;
  private final MessageListener messageListener;
  private final CommandExecutorFactory commandExecutorFactory;
  private final AsyncStreamHandler<Void> stdOutListener;
  private final AsyncStreamHandler<Void> stdErrListener;

  /** Instantiated by {@link UpdaterFactory}. */
  Updater(
      Path gcloud,
      MessageListener messageListener,
      CommandExecutorFactory commandExecutorFactory,
      AsyncStreamHandler<Void> stdOutListener,
      AsyncStreamHandler<Void> stdErrListener) {
    this.gcloud = gcloud;
    this.messageListener = messageListener;
    this.commandExecutorFactory = commandExecutorFactory;
    this.stdOutListener = stdOutListener;
    this.stdErrListener = stdErrListener;
  }

  /** Update a cloud sdk on a separate thread. */
  public void update() throws IOException, ExecutionException {

    List<String> command = new ArrayList<>();
    // now configure parameters (not OS specific)
    command.add(gcloud.toString()); // use full path
    command.add("components");
    command.add("update");
    command.add("--quiet");

    CommandExecutor commandExecutor = commandExecutorFactory.newCommandExecutor();

    messageListener.message("Running command : " + Joiner.on(" ").join(command) + "\n");
    int exitCode = commandExecutor.run(command, stdOutListener, stdErrListener);
    if (exitCode != 0) {
      throw new ExecutionException(
          "Updater exited with non-zero exit code: " + exitCode, new Throwable());
    }
    try {
      stdErrListener.getResult().get();
      stdOutListener.getResult().get();
    } catch (InterruptedException e) {
      messageListener.message("Output interrupted...\n");
    }
  }
}
