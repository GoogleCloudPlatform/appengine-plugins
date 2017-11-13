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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * Stream consumer that reads lines as they come in.
 *
 * @param <T> use {@code Void} if you don't want to store the result
 */
public class StreamLineConsumer<T> implements Callable<T> {
  private final InputStream inputStream;
  private final LineHandler<T> lineHandler;

  public StreamLineConsumer(InputStream inputStream, LineHandler<T> lineHandler) {
    this.inputStream = inputStream;
    this.lineHandler = lineHandler;
  }

  @Override
  public T call() throws Exception {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line = br.readLine();
      while (line != null) {
        lineHandler.line(line);
        line = br.readLine();
      }
    }
    return lineHandler.getResult();
  }
}
