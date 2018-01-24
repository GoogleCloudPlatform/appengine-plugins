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

/**
 * Creates single line text progress indicators.
 *
 * <pre>
 * Start:    #=
 * Progress: #=========
 * Finish:   #============================================================#
 * </pre>
 */
public class TextProgressBar {

  private static final long BARS = 58;

  private final MessageListener messageListener;
  private final long total;
  private long currentProgress = 0;
  private long currentBars = 0;

  TextProgressBar(MessageListener messageListener, long total) {
    this.messageListener = messageListener;
    this.total = total;
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
    currentProgress += value;
    long targetBars = currentProgress * 58 / total;
    while (currentBars < targetBars && currentBars < BARS) {
      messageListener.message("=");
      currentBars++;
    }
  }

  /** Call when done to close out the progress bar. */
  public void done() {
    while (currentBars < BARS) {
      messageListener.message("=");
      currentBars++;
    }
    messageListener.message("=#\n");
  }
}
