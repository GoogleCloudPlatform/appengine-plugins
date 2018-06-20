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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * AsyncWrapper to handle stream consumption on a separate thread. Do not re-use this on streams -
 * it can only handle one stream per instance.
 */
class AsyncByteConsumer implements AsyncStreamSaver {

  private final ByteHandler byteHandler;
  private final ListeningExecutorService executorService;
  private final SettableFuture<String> result;
  private static final int BUFFER_SIZE = 1024;

  /** Create a new instance. */
  AsyncByteConsumer(ByteHandler byteHandler) {
    this(
        Preconditions.checkNotNull(byteHandler),
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()),
        SettableFuture.<String>create());
  }

  @VisibleForTesting
  AsyncByteConsumer(
      ByteHandler byteHandler,
      ListeningExecutorService executorService,
      SettableFuture<String> result) {
    this.byteHandler = byteHandler;
    this.executorService = executorService;
    this.result = result;
  }

  /** Handle an input stream on a separate thread. */
  @Override
  public void handleStream(final InputStream inputStream) {
    if (executorService.isShutdown()) {
      throw new IllegalStateException("Cannot re-use " + this.getClass().getName());
    }
    result.setFuture(executorService.submit(() -> consumeBytes(inputStream)));
    executorService.shutdown();
  }

  @VisibleForTesting
  String consumeBytes(final InputStream inputStream) throws IOException {
    byte[] byteBuffer = new byte[BUFFER_SIZE];
    int bytesRead;
    try (InputStream in = inputStream) {
      while ((bytesRead = in.read(byteBuffer)) != -1) {
        byteHandler.bytes(byteBuffer, bytesRead);
      }
    }
    return byteHandler.getResult();
  }

  @Override
  public ListenableFuture<String> getResult() {
    return result;
  }
}
