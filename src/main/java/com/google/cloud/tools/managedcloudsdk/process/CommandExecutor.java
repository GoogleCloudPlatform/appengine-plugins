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
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Executes a shell command. */
public class CommandExecutor {

  static final int TIMEOUT_SECONDS = 5;

  private ProcessBuilderFactory processBuilderFactory = new ProcessBuilderFactory();
  private ExecutorServiceFactory executorServiceFactory = new ExecutorServiceFactory();
  private MessageListener messageListener;
  private Map<String, String> environment;

  public CommandExecutor setMessageListener(MessageListener messageListener) {
    this.messageListener = messageListener;
    return this;
  }

  /** Sets the environment variables to run the command with. */
  public CommandExecutor setEnvironment(Map<String, String> environmentMap) {
    this.environment = environmentMap;
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
      return Executors.newSingleThreadExecutor();
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
  public int run(List<String> command) throws IOException, ExecutionException {
    messageListener.message("Running command : " + Joiner.on(" ").join(command));

    // Builds the command to execute.
    ProcessBuilder processBuilder = processBuilderFactory.createProcessBuilder();
    processBuilder.command(command);
    processBuilder.redirectErrorStream(true);
    if (environment != null) {
      processBuilder.environment().putAll(environment);
    }
    final Process process = processBuilder.start();

    ExecutorService executor = executorServiceFactory.createExecutorService();
    executor.execute(outputConsumer(process));
    int exitCode;
    try {
      exitCode = process.waitFor();
    } catch (InterruptedException ex) {
      process.destroy();
      throw new ExecutionException("Process cancelled.", ex);
    }

    // Shuts down the executor.
    executor.shutdown();

    try {
      executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      messageListener.message("Process output monitor termination interrupted.");
    }

    return exitCode;
  }

  private Runnable outputConsumer(final Process process) {
    return new Runnable() {
      @Override
      public void run() {
        try (BufferedReader br =
            new BufferedReader(new InputStreamReader(process.getInputStream()))) {
          String line = br.readLine();
          while (line != null) {
            messageListener.message(line);
            line = br.readLine();
          }
        } catch (IOException ex) {
          messageListener.message("IO Exception reading process output");
        }
      }
    };
  }
}
