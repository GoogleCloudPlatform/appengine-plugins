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

package com.google.cloud.tools.appengine.cloudsdk.internal.process;

import static java.lang.ProcessBuilder.Redirect;

import com.google.cloud.tools.appengine.cloudsdk.process.ProcessExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessStartListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * Default process runner that allows synchronous or asynchronous execution. It also allows
 * monitoring output and checking the exit code of the child process.
 */
public class DefaultProcessRunner implements ProcessRunner {
  private final boolean async;
  private final List<ProcessOutputLineListener> stdOutLineListeners;
  private final List<ProcessOutputLineListener> stdErrLineListeners;
  private final ProcessExitListener exitListener;
  private final ProcessStartListener startListener;

  private Map<String, String> environment;

  /**
   * @param async               Whether to run commands asynchronously
   * @param stdOutLineListeners Client consumers of process standard output. If empty, output will
   *                            be inherited by parent process.
   * @param stdErrLineListeners Client consumers of process error output. If empty, output will be
   *                            inherited by parent process.
   * @param exitListener        Client consumer of process onExit event.
   */
  public DefaultProcessRunner(boolean async, List<ProcessOutputLineListener> stdOutLineListeners,
                              List<ProcessOutputLineListener> stdErrLineListeners,
                              ProcessExitListener exitListener,
                              ProcessStartListener startListener) {
    this.async = async;
    this.stdOutLineListeners = stdOutLineListeners;
    this.stdErrLineListeners = stdErrLineListeners;
    this.exitListener = exitListener;
    this.startListener = startListener;
  }

  /**
   * Executes a shell command.
   *
   * <p>If any output listeners were configured, output will go to them only. Otherwise, process
   * output will be redirected to the caller via inheritIO.
   *
   * @param command The shell command to execute
   */
  public void run(String[] command) throws ProcessRunnerException {
    try {
      // configure process builder
      final ProcessBuilder processBuilder = new ProcessBuilder();
      if (stdOutLineListeners.isEmpty()) {
        processBuilder.redirectOutput(Redirect.INHERIT);
      }
      if (stdErrLineListeners.isEmpty()) {
        processBuilder.redirectError(Redirect.INHERIT);
      }
      if (environment != null) {
        processBuilder.environment().putAll(environment);
      }

      processBuilder.command(command);

      Process process = processBuilder.start();

      handleStdOut(process);
      handleErrOut(process);

      if (startListener != null) {
        startListener.onStart(process);
      }

      if (async) {
        asyncRun(process);
      } else {
        shutdownProcessHook(process);
        syncRun(process);
      }

    } catch (IOException | InterruptedException | IllegalThreadStateException e) {
      throw new ProcessRunnerException(e);
    }
  }

  /**
   * Environment variables to append to the current system environment variables.
   */
  public void setEnvironment(Map<String, String> environment) {
    this.environment = environment;
  }

  private void handleStdOut(final Process process) {
    final Scanner stdOut = new Scanner(process.getInputStream());
    Thread stdOutThread = new Thread("standard-out") {
      public void run() {
        while (stdOut.hasNextLine() && !Thread.interrupted()) {
          String line = stdOut.nextLine();
          for (ProcessOutputLineListener stdOutLineListener : stdOutLineListeners) {
            stdOutLineListener.onOutputLine(line);
          }
        }
      }
    };
    stdOutThread.setDaemon(true);
    stdOutThread.start();
  }

  private void handleErrOut(final Process process) {
    final Scanner stdErr = new Scanner(process.getErrorStream());
    Thread stdErrThread = new Thread("standard-err") {
      public void run() {
        while (stdErr.hasNextLine() && !Thread.interrupted()) {
          String line = stdErr.nextLine();
          for (ProcessOutputLineListener stdErrLineListener : stdErrLineListeners) {
            stdErrLineListener.onOutputLine(line);
          }
        }
      }
    };
    stdErrThread.setDaemon(true);
    stdErrThread.start();
  }

  private void syncRun(final Process process) throws InterruptedException {
    int exitCode = process.waitFor();
    if (exitListener != null) {
      exitListener.onExit(exitCode);
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
            exitListener.onExit(process.exitValue());
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
}
