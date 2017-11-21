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

import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.executors.SingleThreadExecutorServiceFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.concurrent.Callable;

/** Wrapper around {@link CommandRunner}/{@link CommandCaller} to asynchronous execution. */
public class AsyncCommandWrapper {

  private final SdkExecutorServiceFactory executorServiceFactory;

  AsyncCommandWrapper(SdkExecutorServiceFactory executorServiceFactory) {
    this.executorServiceFactory = executorServiceFactory;
  }

  /** Wrap a command runner and return a resultless future. */
  public ListenableFuture<Void> run(final CommandRunner commandRunner) {
    ListeningExecutorService executorService = executorServiceFactory.newExecutorService();
    ListenableFuture<Void> resultFuture =
        executorService.submit(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                commandRunner.run();
                return null;
              }
            });
    executorService.shutdown();
    return resultFuture;
  }

  /** Wrap a {@link CommandCaller} and return a string result future. */
  public ListenableFuture<String> call(final CommandCaller commandCaller) {
    ListeningExecutorService executorService = executorServiceFactory.newExecutorService();
    ListenableFuture<String> resultFuture =
        executorService.submit(
            new Callable<String>() {
              @Override
              public String call() throws Exception {
                return commandCaller.call();
              }
            });
    executorService.shutdown();
    return resultFuture;
  }

  /** Static factory, creates a new runner wrapper for gcloud commands. */
  public static AsyncCommandWrapper newRunnerWrapper() {
    CommandFactory commandFactory = new CommandFactory();
    SdkExecutorServiceFactory executorServiceFactory = new SingleThreadExecutorServiceFactory();

    return new AsyncCommandWrapper(executorServiceFactory);
  }
}
