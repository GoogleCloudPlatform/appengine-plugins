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
import java.util.Scanner;

/**
 * Default process runner that allows synchronous or asynchronous execution. It also allows
 * monitoring output and checking the exit code of the child process.
 */
public class DefaultProcessRunner implements ProcessRunner {

  private ProcessBuilder processBuilder;
  private boolean async = false;
  private ProcessOutputLineListener stdOutLineListener;
  private ProcessOutputLineListener stdErrLineListener;
  private ProcessExitListener exitListener;

  /**
   * Create the process runner with a default process builder, with inheritIO enabled.
   */
  public DefaultProcessRunner() {
    this(new ProcessBuilder());
    processBuilder.inheritIO();
  }

  /**
   * Create the process runner with the provided process builder.
   */
  public DefaultProcessRunner(ProcessBuilder processBuilder) {
    this.processBuilder = processBuilder;
  }

  /**
   * Executes a shell command.
   *
   * @param command The shell command to execute
   */
  public void run(String[] command) throws ProcessRunnerException {

    processBuilder.command(makeOsSpecific(command));

    try {
      final Process process = processBuilder.start();

      handleStdOut(process);
      handleErrOut(process);

      if (async) {
        asyncRun(process);
      } else {
        shutdownProcessHook(process);
        syncRun(process);
      }


    } catch (IOException | InterruptedException e) {
      throw new ProcessRunnerException(e);
    }
  }

  /**
   * Sets the process execution to be asynchronous
   *
   * @param async False by default.
   */
  public void setAsync(boolean async) {
    this.async = async;
  }

  /**
   * Set the listener for standard output of the subprocess. Note that this will not work if you set
   * inheritIO.
   *
   * @param stdOutLineListener Can be null.
   */
  public void setStdOutLineListener(ProcessOutputLineListener stdOutLineListener) {
    this.stdOutLineListener = stdOutLineListener;
  }

  /**
   * Set the listener for standard error output of the subprocess. Note that this will not work if
   * you set inheritIO.
   *
   * @param stdErrLineListener Can be null.
   */
  public void setStdErrLineListener(ProcessOutputLineListener stdErrLineListener) {
    this.stdErrLineListener = stdErrLineListener;
  }

  /**
   * Sets the subprocess exit listener for collecting the exit code of the subprocess.
   *
   * @param exitListener Can be nul.
   */
  public void setExitListener(ProcessExitListener exitListener) {
    this.exitListener = exitListener;
  }

  private void handleErrOut(final Process process) {
    if (stdErrLineListener != null) {
      final Scanner stdErr = new Scanner(process.getErrorStream());
      Thread stdErrThread = new Thread("standard-err") {
        public void run() {
          while (stdErr.hasNextLine() && !Thread.interrupted()) {
            String line = stdErr.nextLine();
            stdErrLineListener.outputLine(line);
          }
        }
      };
      stdErrThread.setDaemon(true);
      stdErrThread.start();
    }
  }

  private void handleStdOut(final Process process) {
    if (stdOutLineListener != null) {
      final Scanner stdOut = new Scanner(process.getInputStream());
      Thread stdOutThread = new Thread("standard-out") {
        public void run() {
          while (stdOut.hasNextLine() && !Thread.interrupted()) {
            String line = stdOut.nextLine();
            stdOutLineListener.outputLine(line);
          }
        }
      };
      stdOutThread.setDaemon(true);
      stdOutThread.start();
    }
  }

  private void syncRun(final Process process) throws InterruptedException {
    int exitCode = process.waitFor();
    if (exitListener != null) {
      exitListener.exit(exitCode);
    }
  }

  private void asyncRun(final Process process) throws InterruptedException {
    if (exitListener != null) {
      Thread exitThread = new Thread("wait-for-exit") {
        @Override
        public void run() {
          try {
            process.waitFor();
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            exitListener.exit(process.exitValue());
          }
        }
      };
      exitThread.setDaemon(true);
      exitThread.start();
    }
  }

  private void shutdownProcessHook(final Process process) {
    Runtime.getRuntime().addShutdownHook(new Thread("destroy-process") {
      @Override
      public void run() {
        if (process != null) {
          process.destroy();
        }
      }
    });
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
