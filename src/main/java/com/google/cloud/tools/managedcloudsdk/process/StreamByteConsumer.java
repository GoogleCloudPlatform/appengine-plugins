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

import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * Stream consumer that reads bytes as they come in.
 *
 * @param <T> use {@code Void} if you don't want to store the result
 */
public class StreamByteConsumer<T> implements Callable<T> {
  private final InputStream inputStream;
  private final ByteHandler<T> byteHandler;

  /** Instantiated by {@link StreamByteConsumerFactory}. */
  StreamByteConsumer(InputStream inputStream, ByteHandler<T> byteHandler) {
    this.inputStream = inputStream;
    this.byteHandler = byteHandler;
  }

  @Override
  public T call() throws Exception {
    byte[] byteBuffer = new byte[1024];
    int bytesRead;
    try (InputStream in = inputStream) {
      while ((bytesRead = in.read(byteBuffer)) != -1) {
        byteHandler.bytes(byteBuffer, bytesRead);
      }
    }
    return byteHandler.getResult();
  }
}
