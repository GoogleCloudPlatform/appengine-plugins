/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.process;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Executes a shell command. */
public class CommandExecutor {

  static final int TIMEOUT_SECONDS = 5;

  private ProcessBuilderFactory processBuilderFactory = new ProcessBuilderFactory();
  private ExecutorServiceFactory executorServiceFactory = new ExecutorServiceFactory();
  private MessageListener messageListener;
  private Map<String, String> environment;
  private Path workingDirectory;

  public CommandExecutor setMessageListener(MessageListener messageListener) {
    this.messageListener = messageListener;
    return this;
  }

  /**
   * Sets the environment variables to run the command with.
   */
  public CommandExecutor setEnvironment(Map<String, String> environmentMap) {
    this.environment = environmentMap;
    return this;
  }

  public CommandExecutor setWorkingDirectory(Path workingDirectory) {
    this.workingDirectory = workingDirectory;
    return this;
  }

  @VisibleForTesting
  static class ProcessBuilderFactory {
    ProcessBuilder createProcessBuilder() {
      return new ProcessBuilder();
    }
  }

  @VisibleForTesting
  CommandExecutor setProcessBuilderFactory(ProcessBuilderFactory processBuilderFactory) {
    this.processBuilderFactory = processBuilderFactory;
    return this;
  }

  @VisibleForTesting
  static class ExecutorServiceFactory {
    ExecutorService createExecutorService() {
      return Executors.newFixedThreadPool(2);
    }
  }

  @VisibleForTesting
  CommandExecutor setExecutorServiceFactory(ExecutorServiceFactory executorServiceFactory) {
    this.executorServiceFactory = executorServiceFactory;
    return this;
  }

  /**
   * Runs the command.
   *
   * @param command the list of command line tokens
   * @return exitcode from the process
   */
  public Result run(List<String> command) throws IOException, ExecutionException {
    messageListener.message("Running command : " + Joiner.on(" ").join(command));

    // Builds the command to execute.
    ProcessBuilder processBuilder = processBuilderFactory.createProcessBuilder();
    processBuilder.command(command);
    if (workingDirectory != null) {
      processBuilder.directory(workingDirectory.toFile());
    }
    if (environment != null) {
      processBuilder.environment().putAll(environment);
    }
    final Process process = processBuilder.start();

    ExecutorService executor = executorServiceFactory.createExecutorService();
    Future<String> stdoutFuture = executor.submit(savingOutputConsumer(process.getInputStream()));
    Future<String> stderrFuture = executor.submit(savingOutputConsumer(process.getErrorStream()));
    // Tell executor to shutdown after output consuming processes end.
    executor.shutdown();

    String stdout = null;
    String stderr = null;


    int exitCode;
    try {
      exitCode = process.waitFor();
      stdout = stdoutFuture.get();
      stderr = stderrFuture.get();
    } catch (InterruptedException ex) {
      process.destroy();
      throw new ExecutionException("Process cancelled.", ex);
    }

    return new Result(stdout, stderr, exitCode);
  }

  private Callable<String> savingOutputConsumer(final InputStream inputStream) {
    return new Callable<String>() {
      @Override
      public String call() {
        StringBuilder x = new StringBuilder("");
        try (BufferedReader br =
                 new BufferedReader(new InputStreamReader(inputStream))) {
          String line = br.readLine();
          while (line != null) {
            messageListener.message(line);
            x.append(line).append(System.lineSeparator());
            line = br.readLine();
          }
        } catch (IOException ex) {
          messageListener.message("IO Exception reading process output");
        }
        return x.toString();
      }
    };
  }

  public static class Result {
    private final String stdout;
    private final String stderr;
    private final int exitCode;

    Result(String stdout, String stderr, int exitCode) {
      this.stdout = stdout;
      this.stderr = stderr;
      this.exitCode = exitCode;
    }

    public String getStdout() {
      return stdout;
    }

    public String getStderr() {
      return stderr;
    }

    public int getExitCode() {
      return exitCode;
    }
  }
}

