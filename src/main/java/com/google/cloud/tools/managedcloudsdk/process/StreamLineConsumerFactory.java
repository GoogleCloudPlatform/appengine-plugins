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
 * {@link StreamLineConsumer} factory.
 *
 * @param <T> use {@code Void} if you do not want to store the result
 */
public class StreamLineConsumerFactory<T> implements StreamConsumerFactory<T> {

  private final LineHandler<T> lineHandler;

  public StreamLineConsumerFactory(LineHandler<T> lineHandler) {
    this.lineHandler = lineHandler;
  }

  @Override
  public Callable<T> newConsumer(InputStream inputStream) {
    return new StreamLineConsumer<>(inputStream, lineHandler);
  }
}
