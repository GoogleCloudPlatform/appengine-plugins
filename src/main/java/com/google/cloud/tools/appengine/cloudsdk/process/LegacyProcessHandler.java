/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

package com.google.cloud.tools.appengine.cloudsdk.process;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.WaitingProcessOutputLineListener;
import com.google.common.annotations.VisibleForTesting;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Process handler that mimics the previous behavior of ProcessRunner. */
public class LegacyProcessHandler implements ProcessHandler {
  private final List<ProcessOutputLineListener> stdOutLineListeners;
  private final List<ProcessOutputLineListener> stdErrLineListeners;
  private final List<ProcessExitListener> exitListeners;
  private final List<ProcessStartListener> startListeners;
  @Nullable private final WaitingProcessOutputLineListener waitingProcessOutputLineListener;
  private final boolean async;

  // TODO: historically it looks like this code hasn't been testable, we need to pass an
  // TODO: executor service and get rid of the separate thread instantiation that we've done here.

  /**
   * Non-public constructor, but waitingProcessOutputLineListener must be part of the other
   * listeners (stdout, stderr, processExit) to be triggered correctly.
   */
  LegacyProcessHandler(
      boolean async,
      List<ProcessOutputLineListener> stdOutLineListeners,
      List<ProcessOutputLineListener> stdErrLineListeners,
      List<ProcessStartListener> processStartListeners,
      List<ProcessExitListener> processExitListeners,
      @Nullable WaitingProcessOutputLineListener waitingProcessOutputLineListener) {
    this.async = async;
    this.stdOutLineListeners = stdOutLineListeners;
    this.stdErrLineListeners = stdErrLineListeners;
    this.exitListeners = processExitListeners;
    this.startListeners = processStartListeners;
    this.waitingProcessOutputLineListener = waitingProcessOutputLineListener;
  }

  @Override
  public void handleProcess(Process process) throws ProcessHandlerException {
    Thread stdOutHandler = null;
    Thread stdErrHandler = null;
    try {

      // Only handle stdout or stderr if there are listeners.
      if (!stdOutLineListeners.isEmpty()) {
        stdOutHandler = handleStdOut(process);
      }
      if (!stdErrLineListeners.isEmpty()) {
        stdErrHandler = handleErrOut(process);
      }

      for (ProcessStartListener startListener : startListeners) {
        startListener.onStart(process);
      }

      if (async) {
        asyncRun(process, stdOutHandler, stdErrHandler);
      } else {
        shutdownProcessHook(process);
        syncRun(process, stdOutHandler, stdErrHandler);
      }

    } catch (InterruptedException | AppEngineException ex) {
      throw new ProcessHandlerException(ex);
    }
  }

  private Thread handleStdOut(Process process) {
    final Scanner stdOut = new Scanner(process.getInputStream(), StandardCharsets.UTF_8.name());
    Thread stdOutThread =
        new Thread("standard-out") {
          @Override
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
    return stdOutThread;
  }

  private Thread handleErrOut(Process process) {
    final Scanner stdErr = new Scanner(process.getErrorStream(), StandardCharsets.UTF_8.name());
    Thread stdErrThread =
        new Thread("standard-err") {
          @Override
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
    return stdErrThread;
  }

  private void syncRun(
      Process process, @Nullable Thread stdOutThread, @Nullable Thread stdErrThread)
      throws InterruptedException, AppEngineException {
    int exitCode = process.waitFor();
    // https://github.com/GoogleCloudPlatform/appengine-plugins-core/issues/269
    if (stdOutThread != null) {
      stdOutThread.join();
    }
    if (stdErrThread != null) {
      stdErrThread.join();
    }

    for (ProcessExitListener exitListener : exitListeners) {
      exitListener.onExit(exitCode);
    }
  }

  private static final Logger logger = Logger.getLogger(LegacyProcessHandler.class.getName());

  private void asyncRun(
      final Process process,
      @Nullable final Thread stdOutHandler,
      @Nullable final Thread stdErrHandler)
      throws ProcessHandlerException {
    if (!exitListeners.isEmpty()
        || !stdOutLineListeners.isEmpty()
        || !stdErrLineListeners.isEmpty()) {
      Thread exitThread =
          new Thread("wait-for-process-exit-and-output-handlers") {
            @Override
            public void run() {
              try {
                syncRun(process, stdOutHandler, stdErrHandler);
              } catch (InterruptedException | AppEngineException ex) {
                logger.log(
                    Level.INFO, "wait-for-process-exit-and-output-handlers exited early", ex);
              }
            }
          };
      exitThread.setDaemon(true);
      exitThread.start();
      if (waitingProcessOutputLineListener != null) {
        waitingProcessOutputLineListener.await();
      }
    }
  }

  private static void shutdownProcessHook(final Process process) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread("destroy-process") {
              @Override
              public void run() {
                if (process != null) {
                  process.destroy();
                }
              }
            });
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final List<ProcessOutputLineListener> stdOutLineListeners;
    private final List<ProcessOutputLineListener> stdErrLineListeners;
    private final List<ProcessExitListener> exitListeners;
    private final List<ProcessStartListener> startListeners;
    private final DevAppServerAsyncOutputWatcherFactory devAppServerAsyncOutputWatcherFactory;

    private boolean async;

    private Builder() {
      this(
          new ArrayList<>(),
          new ArrayList<>(),
          new ArrayList<>(),
          new ArrayList<>(),
          new DevAppServerAsyncOutputWatcherFactory());
    }

    @VisibleForTesting
    Builder(
        List<ProcessOutputLineListener> stdOut,
        List<ProcessOutputLineListener> stdErr,
        List<ProcessStartListener> start,
        List<ProcessExitListener> exit,
        DevAppServerAsyncOutputWatcherFactory watcher) {
      this.devAppServerAsyncOutputWatcherFactory = watcher;
      this.stdOutLineListeners = stdOut;
      this.stdErrLineListeners = stdErr;
      this.startListeners = start;
      this.exitListeners = exit;
    }

    public Builder addStdOutLineListener(ProcessOutputLineListener listener) {
      stdOutLineListeners.add(listener);
      return this;
    }

    public Builder addStdErrLineListener(ProcessOutputLineListener listener) {
      stdErrLineListeners.add(listener);
      return this;
    }

    /** Set/override exit listener configuration. */
    public Builder setExitListener(ProcessExitListener listener) {
      exitListeners.clear();
      exitListeners.add(listener);
      return this;
    }

    /** Set/override start listener configuration. */
    public Builder setStartListener(ProcessStartListener listener) {
      startListeners.clear();
      startListeners.add(listener);
      return this;
    }

    public Builder async(boolean async) {
      this.async = async;
      return this;
    }

    public LegacyProcessHandler build() {
      return new LegacyProcessHandler(
          async, stdOutLineListeners, stdErrLineListeners, startListeners, exitListeners, null);
    }

    /**
     * Use the build for the devappserver async mode, it adds the correct listener to the process
     * output and configures 'waiting'.
     */
    public LegacyProcessHandler buildDevAppServerAsync(int timeout) {
      WaitingProcessOutputLineListener devAppServerOutputListener =
          devAppServerAsyncOutputWatcherFactory.newLineListener(timeout);
      stdOutLineListeners.add(devAppServerOutputListener);
      stdErrLineListeners.add(devAppServerOutputListener);
      exitListeners.add(devAppServerOutputListener);
      return new LegacyProcessHandler(
          true,
          stdOutLineListeners,
          stdErrLineListeners,
          startListeners,
          exitListeners,
          devAppServerOutputListener);
    }

    static class DevAppServerAsyncOutputWatcherFactory {
      WaitingProcessOutputLineListener newLineListener(int timeout) {
        return new WaitingProcessOutputLineListener(
            ".*(Dev App Server is now running|INFO:oejs\\.Server:main: Started).*", timeout);
      }
    }
  }
}
