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

package com.google.cloud.tools.appengine.experimental.internal.process;

import com.google.cloud.tools.appengine.experimental.OutputHandler;
import com.google.cloud.tools.appengine.experimental.internal.process.io.StringResultConverter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The cli process mechanism takes a process and starts 3 threads on an executor to manage it
 * 1. The processMain thread waits on the process to end and stores the exit code
 * 2. The StdErr thread processes the err stream and writes output to the outputListener
 * 3. The StdOut thread processes the out stream and stores output to return as result
 * The future implementation considers the process main (1) thread to be the primary thread
 * to watch. It forwards all future interface calls to the future returned when starting that
 * thread (1).
 *
 * @param <T> the process return type
 */
public class CliProcessManager<T> implements Future<T> {

  private final ExecutorService executor;
  private final OutputHandler outputHandler;
  private final StringResultConverter<T> stringResultConverter;
  private final Process process;
  private ListenableFutureTask<CliProcessResult<T>> processMain;
  private ListenableFutureTask<String> processStdOut;
  private FutureTask<Void> processStdErr;

  private CliProcessManager(Process process, OutputHandler outputHandler,
      StringResultConverter<T> stringResultConverter) {

    this.process = process;
    this.executor = MoreExecutors.listeningDecorator(MoreExecutors.getExitingExecutorService(
        (ThreadPoolExecutor) Executors.newFixedThreadPool(3), 2, TimeUnit.SECONDS));
    this.outputHandler = outputHandler;
    this.stringResultConverter = stringResultConverter;
  }

  // Main entry point, adds three managing threads to the executor
  private Future<T> manage() {

    processStdOut = ListenableFutureTask.create(new Callable<String>() {
      StringBuilder result = new StringBuilder("");
      @Override
      public String call() throws Exception {
        final Scanner stdOut = new Scanner(process.getInputStream(), "UTF-8");
        while (stdOut.hasNextLine() && !Thread.interrupted()) {
          String line = stdOut.nextLine();
          result.append(line);
          result.append(System.getProperty("line.separator"));
        }
        return result.toString();
      }
    });


    processStdErr = new FutureTask<Void>(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        final Scanner stdOut = new Scanner(process.getErrorStream(), "UTF-8");
        while (stdOut.hasNextLine() && !Thread.interrupted()) {
          String line = stdOut.nextLine();
          outputHandler.handleLine(line);
        }
        return null;
      }
    });

    // processMain does some special handling to get the result of the stdout and store
    // exit code and result in a single return object
    processMain = ListenableFutureTask.create(new Callable<CliProcessResult<T>>() {
      @Override
      public CliProcessResult<T> call() throws Exception {
        int exitCode = process.waitFor();
        T result = stringResultConverter.getResult(processStdOut.get());
        processStdErr.get();
        return new CliProcessResult<T>(exitCode,result);
      }
    });

    executor.submit(processStdOut);
    executor.submit(processStdErr);
    executor.submit(processMain);
    return this;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    // killing processMain should kill the threads that were simply processing output
    // as those streams will EOF
    process.destroy();
    return processMain.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return processMain.isCancelled();
  }

  @Override
  public boolean isDone() {
    return processMain.isDone();
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    CliProcessResult<T> result = processMain.get();
    if (result.getExitCode() != 0) {
      throw new ExecutionException("Process failed with exit code : " + result.getExitCode(), null);
    }
    return result.getResult();
  }

  @Override
  public T get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    CliProcessResult<T> result = processMain.get(timeout, unit);
    if (result.getExitCode() != 0) {
      throw new ExecutionException("Process failed with exit code : " + result.getExitCode(), null);
    }
    return result.getResult();
  }

  private static class CliProcessResult<R> {
    private final int exitCode;
    private final R result;

    public CliProcessResult(int exitCode, R result) {
      this.exitCode = exitCode;
      this.result = result;
    }

    public int getExitCode() {
      return exitCode;
    }

    public R getResult() {
      return result;
    }
  }

  public static class Provider<T> implements CliProcessManagerProvider<T> {

    @Override
    public Future<T> manage(Process process, StringResultConverter<T> stringResultConverter,
        OutputHandler outputHandler) {
      return new CliProcessManager<T>(process, outputHandler, stringResultConverter).manage();
    }
  }
}
