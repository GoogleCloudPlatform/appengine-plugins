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

package com.google.cloud.tools.managedcloudsdk.process;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * AsyncWrapper to handle stream consumption on a separate thread. Do not re-use this on streams -
 * it can only handle one stream per instance.
 *
 * @param <T> the type of the returned Future
 */
public class AsyncStreamHandler<T> {

  private final ListeningExecutorService executorService;
  private final StreamConsumerFactory<T> streamConsumerFactory;
  private final SettableFuture<T> result;

  /** Create a new instance. */
  public AsyncStreamHandler(StreamConsumerFactory<T> streamConsumerFactory) {
    this(
        streamConsumerFactory,
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()),
        SettableFuture.<T>create());
  }

  AsyncStreamHandler(
      StreamConsumerFactory<T> streamConsumerFactory,
      ListeningExecutorService executorService,
      SettableFuture<T> result) {
    this.streamConsumerFactory = streamConsumerFactory;
    this.executorService = executorService;
    this.result = result;
  }

  /** Handle an input stream on a separate thread. */
  public void handleStream(final InputStream inputStream) {
    if (executorService.isShutdown()) {
      throw new IllegalStateException("Cannot re-use " + this.getClass().getName());
    }
    result.setFuture(executorService.submit(streamConsumerFactory.newConsumer(inputStream)));
    executorService.shutdown();
  }

  public ListenableFuture<T> getResult() {
    return result;
  }
}
