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
import com.google.common.base.Charsets;

import java.io.IOException;
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
  private final List<ProcessExitListener> exitListeners;
  private final List<ProcessStartListener> startListeners;
  private final boolean inheritProcessOutput;

  private Map<String, String> environment;

  /**
   * @param async                Whether to run commands asynchronously
   * @param stdOutLineListeners  Client consumers of process standard output. If empty, output will
   *                             be inherited by parent process.
   * @param stdErrLineListeners  Client consumers of process error output. If empty, output will be
   *                             inherited by parent process.
   * @param exitListeners        Client consumers of process onExit event.
   * @param startListeners       Client consumers of process onStart event.
   * @param inheritProcessOutput If true, redirects stdout and stderr to the parent process.
   */
  public DefaultProcessRunner(boolean async, List<ProcessOutputLineListener> stdOutLineListeners,
                              List<ProcessOutputLineListener> stdErrLineListeners,
                              List<ProcessExitListener> exitListeners,
                              List<ProcessStartListener> startListeners,
                              boolean inheritProcessOutput) {
    this.async = async;
    this.stdOutLineListeners = stdOutLineListeners;
    this.stdErrLineListeners = stdErrLineListeners;
    this.exitListeners = exitListeners;
    this.startListeners = startListeners;
    this.inheritProcessOutput = inheritProcessOutput;
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
      // Configure process builder.
      final ProcessBuilder processBuilder = new ProcessBuilder();

      // If there are no listeners, we might still want to redirect stdout and stderr to the parent
      // process, or not.
      if (stdOutLineListeners.isEmpty() && inheritProcessOutput) {
        processBuilder.redirectOutput(Redirect.INHERIT);
      }
      if (stdErrLineListeners.isEmpty() && inheritProcessOutput) {
        processBuilder.redirectError(Redirect.INHERIT);
      }
      if (environment != null) {
        processBuilder.environment().putAll(environment);
      }

      processBuilder.command(command);

      Process process = processBuilder.start();

      // Only handle stdout or stderr if there are listeners.
      if (!stdOutLineListeners.isEmpty()) {
        handleStdOut(process);
      }
      if (!stdErrLineListeners.isEmpty()) {
        handleErrOut(process);
      }

      for (ProcessStartListener startListener : startListeners) {
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
    final Scanner stdOut = new Scanner(process.getInputStream(), Charsets.UTF_8.name());
    Thread stdOutThread = new Thread("standard-out") {
      public void run() {
        while (stdOut.hasNextLine() && !Thread.interrupted()) {
          String line = stdOut.nextLine();
          for (ProcessOutputLineListener stdOutLineListener : stdOutLineListeners) {
            stdOutLineListener.onOutputLine(line);
          }
        }
        stdOut.close();
      }
    };
    stdOutThread.setDaemon(true);
    stdOutThread.start();
  }

  private void handleErrOut(final Process process) {
    final Scanner stdErr = new Scanner(process.getErrorStream(), Charsets.UTF_8.name());
    Thread stdErrThread = new Thread("standard-err") {
      public void run() {
        while (stdErr.hasNextLine() && !Thread.interrupted()) {
          String line = stdErr.nextLine();
          for (ProcessOutputLineListener stdErrLineListener : stdErrLineListeners) {
            stdErrLineListener.onOutputLine(line);
          }
        }
        stdErr.close();
      }
    };
    stdErrThread.setDaemon(true);
    stdErrThread.start();
  }

  private void syncRun(final Process process) throws InterruptedException {
    int exitCode = process.waitFor();
    for (ProcessExitListener exitListener : exitListeners) {
      exitListener.onExit(exitCode);
    }
  }

  private void asyncRun(final Process process) throws InterruptedException {
    if (exitListeners.size() > 0) {
      Thread exitThread = new Thread("wait-for-exit") {
        @Override
        public void run() {
          try {
            process.waitFor();
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            int exitCode = process.exitValue();
            for (ProcessExitListener exitListener : exitListeners) {
              exitListener.onExit(exitCode);
            }
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
