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

package com.google.cloud.tools.appengine.experimental.internal.cloudsdk;

import com.google.cloud.tools.appengine.experimental.AppEngineRequestFuture;
import com.google.cloud.tools.appengine.experimental.internal.process.CliProcessManager;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CloudSdkRequestFuture<T> implements AppEngineRequestFuture<T> {
  private final Future<T> future;
  private final InputStream inputStream;

  public CloudSdkRequestFuture(CliProcessManager<T> cliProcessManager) {
    future = cliProcessManager;
    inputStream = cliProcessManager.getInputStream();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return future.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return future.isCancelled();
  }

  @Override
  public boolean isDone() {
    return future.isDone();
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    return future.get();
  }

  @Override
  public T get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(timeout, unit);
  }

  public InputStream getInputStream() {
    return inputStream;
  }
}
