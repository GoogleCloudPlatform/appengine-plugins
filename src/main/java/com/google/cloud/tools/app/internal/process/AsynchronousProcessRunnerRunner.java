/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.app.internal.process;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Helper class to execute commands in asynchronous mode.
 */
public class AsynchronousProcessRunnerRunner {

  /**
   * Run command asynchronously and return a future to access the result/exceptions.
   */
  public Future<Integer> runAsynchronous(final String[] command, final ProcessRunner processRunner) {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Callable<Integer> asyncProcess = new Callable<Integer>() {
      @Override
      public Integer call() throws ProcessRunnerException {
        return processRunner.run(command);
      }
    };

    Future<Integer> result = executorService.submit(asyncProcess);
    executorService.shutdown();
    return result;
  }

  /**
   * Run command asynchronously and callback when finished.
   */
  public void runAsynchronous(final String[] command, final ProcessRunner processRunner, final Callback callback) {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Runnable asyncProcess = new Runnable() {
      @Override
      public void run() {
        try {
          int result = processRunner.run(command);
          callback.onCompleted(result);
        } catch (ProcessRunnerException e) {
          callback.onFailedWithException(e);
        }
      }
    };

    executorService.submit(asyncProcess);
    executorService.shutdown();
  }

  public interface Callback {
    void onCompleted(int exitCode);
    void onFailedWithException(ProcessRunnerException ex);
  }

}
