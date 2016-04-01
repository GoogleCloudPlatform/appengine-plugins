/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.tools.app.internal.process.io.impl;

import com.google.cloud.tools.app.internal.process.io.StreamConsumer;
import com.google.cloud.tools.app.internal.process.io.StreamInspector;
import com.google.cloud.tools.app.internal.process.io.StreamWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InputStreamConsumer implements StreamConsumer {

  private final ExecutorService executorService;
  private final StreamWriter streamWriter;
  private final StreamInspector streamInspector;

  public InputStreamConsumer(StreamWriter writer, StreamInspector inspector) {
    streamWriter = writer;
    streamInspector = inspector;
    executorService = Executors.newSingleThreadExecutor();
  }

  /**
   * Caution : You must redirect error stream to input stream when using this class.
   */
  @Override
  public void consumeStreams(final Process process) {

    executorService.submit(new Runnable() {
      @Override
      public void run() {
        try {
          try (BufferedReader streamReader = new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while(null != (line = streamReader.readLine())){
              if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
              }
              if (streamInspector != null) {
                streamInspector.inspectLine(line);
              }
              if (streamWriter != null) {
                streamWriter.writeLine(line);
              }
            }
          }
        } catch (InterruptedException e) {
          // do nothing, executor probably killed us
        } catch (IOException e) {
          // print this for now?
          e.printStackTrace();
        }
      }
    });

    executorService.shutdown();
  }

  @Override
  public void stop() {
    try {
      // give the service a second to finish reading output
      executorService.awaitTermination(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // if we get interrupted by something, just kill it
      executorService.shutdownNow();
    }
  }
}
