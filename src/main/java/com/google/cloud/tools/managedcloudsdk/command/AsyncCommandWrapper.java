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

/** Wrapper around {@link CommandExecutor} to asynchronous execution. */
public class AsyncCommandWrapper {

  private final SdkExecutorServiceFactory executorServiceFactory;

  AsyncCommandWrapper(SdkExecutorServiceFactory executorServiceFactory) {
    this.executorServiceFactory = executorServiceFactory;
  }

  /**
   * Executes a {@link CommandExecutor} asynchronously and returns a future.
   *
   * Using {@link CommandRunner} will return a resultless Void future.
   * Using {@link CommandCaller} will return a string future.
   */
  public <T> ListenableFuture<T> execute(final CommandExecutor<T> commandExecutor) {
    ListeningExecutorService executorService = executorServiceFactory.newExecutorService();
    ListenableFuture<T> resultFuture =
        executorService.submit(
            new Callable<T>() {
              @Override
              public T call() throws Exception {
                return commandExecutor.execute();
              }
            });
    executorService.shutdown();
    return resultFuture;
  }

  /** Static factory, creates a new async wrapper for commands. */
  public static AsyncCommandWrapper newCommandWrapper() {
    SdkExecutorServiceFactory executorServiceFactory = new SingleThreadExecutorServiceFactory();
    return new AsyncCommandWrapper(executorServiceFactory);
  }
}
