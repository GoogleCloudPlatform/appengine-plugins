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

public class TextInfoBar {

  static final int AVAILABLE_SPACE = 56; // characters

  private final MessageListener messageListener;
  private final String text;

  TextInfoBar(MessageListener messageListener, String text) {
    this.messageListener = messageListener;
    this.text = text;
  }

  /** Display the text in the info bar, padding/truncating if necessary. */
  public void show() {
    String displayText;
    if (text.length() > AVAILABLE_SPACE) {
      displayText = text.substring(0, AVAILABLE_SPACE - 3) + "...";
    } else {
      displayText = String.format("%1$-" + AVAILABLE_SPACE + "s", text);
    }

    messageListener.message("#= " + displayText + " =#\n");
  }
}
