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

package com.google.cloud.tools.appengine.experimental.internal.process.io;

import com.google.cloud.tools.appengine.experimental.OutputHandler;

import java.io.PrintStream;

/**
 * Write output to a PrintStream like System.out or System.err
 */
public class PrintStreamOutputHandler implements OutputHandler {

  private PrintStream stream;

  public PrintStreamOutputHandler(PrintStream stream) {
    this.stream = stream;
  }

  @Override
  public void handleLine(String line) {
    stream.println(line);
  }
}
