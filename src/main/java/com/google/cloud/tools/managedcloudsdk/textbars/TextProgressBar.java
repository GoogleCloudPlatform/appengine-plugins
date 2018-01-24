/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.textbars;

import com.google.cloud.tools.managedcloudsdk.MessageListener;

public class TextProgressBar {

  private static final long BARS = 58;

  private final MessageListener messageListener;
  private final double updateThreshold;
  private double totalRead = 0;
  private double progress = 0;

  TextProgressBar(MessageListener messageListener, long total) {
    this.messageListener = messageListener;
    this.updateThreshold = ((double) total) / BARS;
  }

  /** Call when started to writing the starting characters to the progress bar. */
  public void start() {
    messageListener.message("#=");
  }

  /**
   * Update the progress bar.
   *
   * @param value change since last update call
   */
  public void update(long value) {
    totalRead = totalRead + value;
    double diff = totalRead - (progress * updateThreshold);
    if (diff > updateThreshold) {
      int newBars = (int) (diff / updateThreshold);
      for (int i = 0; i < newBars; i++) {
        messageListener.message("=");
      }
      progress = progress + newBars;
    }
  }

  /** Call when done to close out the progress bar. */
  public void done() {
    while (progress < BARS) {
      messageListener.message("=");
      progress++;
    }
    messageListener.message("=#\n");
  }
}
