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

import static java.lang.ProcessBuilder.Redirect;

import java.io.IOException;
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

  private Process process;

  private Map<String, String> environment;

  /**
   * @param async               Whether to run commands asynchronously
   * @param stdOutLineListeners Client consumers of process standard output. If empty, output will
   *                            be inherited by parent process.
   * @param stdErrLineListeners Client consumers of process error output. If empty, output will be
   *                            inherited by parent process.
   * @param exitListener        Client consumer of process exit event.
   */
  public DefaultProcessRunner(boolean async, List<ProcessOutputLineListener> stdOutLineListeners,
                              List<ProcessOutputLineListener> stdErrLineListeners,
                              ProcessExitListener exitListener) {
    this.async = async;
    this.stdOutLineListeners = stdOutLineListeners;
    this.stdErrLineListeners = stdErrLineListeners;
    this.exitListener = exitListener;
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
      processBuilder.command(makeOsSpecific(command));

      synchronized (this) {
        // check if the previous process is still executing
        if (process != null) {
          // will throw IllegalThreadStateException, if process is still running
          process.exitValue();
        }

        process = processBuilder.start();
      }

      handleStdOut(process);
      handleErrOut(process);

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

  /**
   * The process that is currently executing or was the last one to be started using the run
   * method.
   */
  public Process getProcess() {
    return this.process;
  }

  private void handleErrOut(final Process process) {
    final Scanner stdErr = new Scanner(process.getErrorStream());
    Thread stdErrThread = new Thread("standard-err") {
      public void run() {
        while (stdErr.hasNextLine() && !Thread.interrupted()) {
          String line = stdErr.nextLine();
          consumeLine(line, true);
        }
      }
    };
    stdErrThread.setDaemon(true);
    stdErrThread.start();
  }

  private void handleStdOut(final Process process) {
    final Scanner stdOut = new Scanner(process.getInputStream());
    Thread stdOutThread = new Thread("standard-out") {
      public void run() {
        while (stdOut.hasNextLine() && !Thread.interrupted()) {
          String line = stdOut.nextLine();
          consumeLine(line, false);
        }
      }
    };
    stdOutThread.setDaemon(true);
    stdOutThread.start();
  }


  private void consumeLine(String line, boolean errorStream) {
    if (errorStream) {
      for (ProcessOutputLineListener stdErrLineListener : stdErrLineListeners) {
        stdErrLineListener.outputLine(line);
      }
    } else {
      for (ProcessOutputLineListener stdOutLineListener : stdOutLineListeners) {
        stdOutLineListener.outputLine(line);
      }
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
