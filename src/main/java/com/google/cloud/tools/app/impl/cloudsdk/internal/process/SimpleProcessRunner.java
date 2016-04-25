/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.app.impl.cloudsdk.internal.process;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Synchronous process runner that doesn't do anything special to process streams.
 */
public class SimpleProcessRunner implements ProcessRunner {

  /**
   * Executes a shell command synchronously.
   */
  public int run(String[] command) throws ProcessRunnerException {

    command = makeOsSpecific(command);

    final ProcessBuilder pb = new ProcessBuilder(command);
    pb.inheritIO();

    try {
      final Process process = pb.start();

      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          if (process != null) {
            process.destroy();
          }
        }
      });

      return process.waitFor();

    } catch (IOException | InterruptedException e) {
      throw new ProcessRunnerException(e);
    }
  }

  private String[] makeOsSpecific(String[] command) {
    String[] osCommand = command;

    if (System.getProperty("os.name").startsWith("Windows")) {
      List<String> windowsCommand = Arrays.asList(command);
      windowsCommand.add(0, "cmd.exe");
      windowsCommand.add(1, "/c");
      osCommand = windowsCommand.toArray(new String[windowsCommand.size()]);
    }
    return osCommand;
  }

}
