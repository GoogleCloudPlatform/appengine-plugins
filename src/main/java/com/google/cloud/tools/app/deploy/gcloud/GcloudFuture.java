/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.app.deploy.gcloud;

import com.google.cloud.tools.app.deploy.process.OutputHandler;
import com.google.cloud.tools.app.deploy.process.StreamConsumer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// should we just be putting the process on an executor and retrieving the result instead of this?
// will that even allow background devappserver runs?
public class GcloudFuture<T> implements Future<T> {

  private List<String> result = Lists.newArrayList();
  private StringResultConverter<T> resultConverter;
  private final Process process;
  private boolean cancelled = false;

  public GcloudFuture(Process process, StringResultConverter<T> resultConverter) {
    this.process = process;
    this.resultConverter = resultConverter;

    StreamConsumer.startNewConsumer(process.getInputStream(), new OutputHandler() {
      @Override
      public void handleLine(String line) {
        result.add(line);
      }
    });
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (!mayInterruptIfRunning) {
      try {
        process.exitValue();
        return cancelled = false;
      } catch(IllegalThreadStateException e) {
        // we can continue to interrupt now
      }
    }
    process.destroy();
    return cancelled = true;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isDone() {
    try {
      process.exitValue();
      return true;
    } catch(IllegalThreadStateException e) {
      return false;
    }
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new ExecutionException("Process exited with code : " + exitCode, new Throwable());
    }
    return resultConverter.getResult(Joiner.on("\n").join(result));
  }

  @Override
  public T get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    // whatever, we can implement this later
    throw new UnsupportedOperationException("Process.waitFor has handing in java8, wait till then");
  }

}
