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

package com.google.cloud.tools.appengine.cloudsdk.process;

/** A ProcessOutputLineListener that uses a StringBuffer to store the contents of all lines. */
public class StringBuilderProcessOutputLineListener implements ProcessOutputLineListener {

  private final String separator;
  // This used to be a StringBuilder but that wasn't thread safe.
  @SuppressWarnings("JdkObsolete")
  private final StringBuffer buffer = new StringBuffer();

  private StringBuilderProcessOutputLineListener(String separator) {
    this.separator = separator;
  }

  @Override
  public void onOutputLine(String line) {
    buffer.append(line).append(separator);
  }

  @Override
  public String toString() {
    return buffer.toString();
  }

  public static StringBuilderProcessOutputLineListener newListener() {
    return new StringBuilderProcessOutputLineListener("");
  }

  public static StringBuilderProcessOutputLineListener newListenerWithNewlines() {
    return new StringBuilderProcessOutputLineListener(System.getProperty("line.separator"));
  }
}
