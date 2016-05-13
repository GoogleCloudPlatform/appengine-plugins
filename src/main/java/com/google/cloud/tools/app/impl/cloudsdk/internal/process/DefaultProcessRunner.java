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
import java.util.Map;
import java.util.Scanner;

/**
 * Default process runner that allows synchronous or asynchronous execution. It also allows
 * monitoring output and checking the exit code of the child process.
 */
public class DefaultProcessRunner implements ProcessRunner {
  private boolean async;
  private ProcessOutputLineListener stdOutLineListener;
  private ProcessOutputLineListener stdErrLineListener;
  private ProcessExitListener exitListener;
  private AsyncProcessStartWaiter asyncProcessStartWaiter;

  private Process process;

  private Map<String, String> environment;

  private DefaultProcessRunner(Builder builder) {
    this.async = builder.async;
    this.stdOutLineListener = builder.stdOutLineListener;
    this.stdErrLineListener = builder.stdErrLineListener;
    this.exitListener = builder.exitListener;
    if (async) {
      this.asyncProcessStartWaiter = builder.asyncProcessStartWaiter;
    }
  }

  /**
   * Executes a shell command.
   *
   * @param command The shell command to execute
   */
  public void run(String[] command) throws ProcessRunnerException {
    try {

      if (asyncProcessStartWaiter != null) {
        asyncProcessStartWaiter.reset();
      }

      final ProcessBuilder processBuilder = new ProcessBuilder();
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

      if (asyncProcessStartWaiter != null) {
        asyncProcessStartWaiter.await();
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
   * @param exitListener Can be null.
   */
  public void setExitListener(ProcessExitListener exitListener) {
    this.exitListener = exitListener;
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
      if (stdErrLineListener != null) {
        stdErrLineListener.outputLine(line);
      }
    } else {
      if (stdOutLineListener != null) {
        stdOutLineListener.outputLine(line);
      }
    }

    if (asyncProcessStartWaiter != null) {
      asyncProcessStartWaiter.inputLine(line);
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

  public static class Builder {
    private boolean async = false;
    private ProcessOutputLineListener stdOutLineListener;
    private ProcessOutputLineListener stdErrLineListener;
    private ProcessExitListener exitListener;
    private AsyncProcessStartWaiter asyncProcessStartWaiter;

    /**
     * Whether to run commands asynchronously.
     */
    public Builder async(boolean async) {
      this.async = async;
      return this;
    }

    /**
     * The client consumer of process standard output.
     */
    public Builder stdOutLineListener(ProcessOutputLineListener stdOutLineListener) {
      this.stdOutLineListener = stdOutLineListener;
      return this;
    }

    /**
     * The client consumer of process error output.
     */
    public Builder stdErrLineListener(ProcessOutputLineListener stdErrLineListener) {
      this.stdErrLineListener = stdErrLineListener;
      return this;
    }

    /**
     * The client listener of the process exit with code.
     */
    public Builder exitListener(ProcessExitListener exitListener) {
      this.exitListener = exitListener;
      return this;
    }

    /**
     * {@link AsyncProcessStartWaiter} used to block the thread until the asynchronous process has
     * started successfully.
     */
    public Builder asyncProcessStartWaiter(AsyncProcessStartWaiter asyncProcessStartWaiter) {
      this.asyncProcessStartWaiter = asyncProcessStartWaiter;
      return this;
    }

    /**
     * Create a new instance of {@link DefaultProcessRunner}.
     */
    public DefaultProcessRunner build() {
      return new DefaultProcessRunner(this);
    }

  }

}
