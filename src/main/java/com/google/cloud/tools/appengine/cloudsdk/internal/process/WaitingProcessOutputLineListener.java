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

package com.google.cloud.tools.appengine.cloudsdk.internal.process;

import com.google.cloud.tools.appengine.cloudsdk.process.ProcessExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Provides a mechanism to wait for a successful start of a process by monitoring the process output
 * and checking for a specific message in it.
 */
public class WaitingProcessOutputLineListener
    implements ProcessOutputLineListener, ProcessExitListener {
  private final String message;
  private final int timeoutSeconds;
  private CountDownLatch waitLatch;
  private volatile boolean exited;

  /**
   * Creates a listener that waits for a message for a specified amount of time.
   *
   * @param message the message to look for in the output of the process to consider it to be
   *     successfully started. If the message is not seen within the specified timeout, a {@link
   *     ProcessHandlerException} will be thrown. The message is assumed to be a regular expression.
   * @param timeoutSeconds the maximum number of seconds to wait for the message to be seen until
   *     giving up. If set to 0, will skip waiting.
   */
  public WaitingProcessOutputLineListener(String message, int timeoutSeconds) {
    this.message = message;
    this.timeoutSeconds = timeoutSeconds;
    this.waitLatch = new CountDownLatch(1);
  }

  /** Prepares the internal latch for monitoring messages and waiting. */
  public void reset() {
    waitLatch.countDown();
    waitLatch = new CountDownLatch(1);
  }

  /**
   * Blocks the executing thread until the specified message is seen through {@link
   * #onOutputLine(String)}. If the message is not seen within the specified timeout, {@link
   * ProcessHandlerException} will be thrown.
   */
  public void await() throws ProcessHandlerException {
    try {
      if (message != null
          && timeoutSeconds != 0
          && !waitLatch.await(timeoutSeconds, TimeUnit.SECONDS)) {
        throw new ProcessHandlerException(
            "Timed out waiting for the success message: '" + message + "'");
      }
      if (exited) {
        throw new ProcessHandlerException("Process exited before success message");
      }
    } catch (InterruptedException e) {
      throw new ProcessHandlerException(e);
    } finally {
      waitLatch.countDown();
    }
  }

  /** Monitors the output of the process to check whether the wait condition is satisfied. */
  @Override
  public void onOutputLine(String line) {
    if (waitLatch.getCount() > 0 && message != null && line.matches(message)) {
      waitLatch.countDown();
    }
  }

  @Override
  public void onExit(int exitCode) {
    this.exited = true;
    waitLatch.countDown();
  }
}
