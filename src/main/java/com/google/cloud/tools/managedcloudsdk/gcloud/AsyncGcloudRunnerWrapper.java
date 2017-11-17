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

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.executors.SingleThreadExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.process.CommandRunner;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/** Wrapper around {@link CommandRunner} command to run gcloud commands asynchronously. */
public class AsyncGcloudRunnerWrapper {

  private final GcloudCommandFactory commandFactory;
  private final SdkExecutorServiceFactory executorServiceFactory;

  AsyncGcloudRunnerWrapper(
      GcloudCommandFactory commandFactory, SdkExecutorServiceFactory executorServiceFactory) {
    this.commandFactory = commandFactory;
    this.executorServiceFactory = executorServiceFactory;
  }

  /** Run a command and return a resultless control future. */
  public ListenableFuture<Void> runCommand(
      final List<String> parameters, final MessageListener messageListener) {
    ListeningExecutorService executorService = executorServiceFactory.newExecutorService();
    ListenableFuture<Void> resultFuture =
        executorService.submit(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                commandFactory.newRunner(parameters, messageListener).run();
                return null;
              }
            });
    executorService.shutdown(); // shutdown executor after install
    return resultFuture;
  }

  /** Static factory, creates a new runner wrapper for gcloud commands. */
  public static AsyncGcloudRunnerWrapper newRunnerWrapper(Path gcloud) {
    GcloudCommandFactory commandFactory = new GcloudCommandFactory(gcloud);
    SdkExecutorServiceFactory executorServiceFactory = new SingleThreadExecutorServiceFactory();

    return new AsyncGcloudRunnerWrapper(commandFactory, executorServiceFactory);
  }
}
