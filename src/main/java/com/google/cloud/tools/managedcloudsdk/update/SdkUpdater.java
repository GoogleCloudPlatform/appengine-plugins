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

package com.google.cloud.tools.managedcloudsdk.update;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.executors.SingleThreadExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.gcloud.GcloudCommandFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/** Update an SDK. */
public class SdkUpdater {

  private final GcloudCommandFactory commandFactory;
  private final SdkExecutorServiceFactory executorServiceFactory;

  /** Use {@link #newUpdater} to instantiate. */
  SdkUpdater(
      GcloudCommandFactory commandFactory, SdkExecutorServiceFactory executorServiceFactory) {
    this.commandFactory = commandFactory;
    this.executorServiceFactory = executorServiceFactory;
  }

  /**
   * Update the Cloud SDK.
   *
   * @param messageListener listener to receive feedback on
   * @return a resultless future for controlling the process
   */
  public ListenableFuture<Void> update(final MessageListener messageListener) {
    ListeningExecutorService executorService = executorServiceFactory.newExecutorService();
    ListenableFuture<Void> resultFuture =
        executorService.submit(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                commandFactory.newCommand(getParameters(), messageListener).run();
                return null;
              }
            });
    executorService.shutdown(); // shutdown executor after install
    return resultFuture;
  }

  List<String> getParameters() {
    List<String> command = new ArrayList<>();
    // now configure parameters (not OS specific)
    command.add("components");
    command.add("update");
    command.add("--quiet");

    return command;
  }

  /**
   * Configure and create a new Updater instance.
   *
   * @param gcloud path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk updater
   */
  public static SdkUpdater newUpdater(Path gcloud) {
    GcloudCommandFactory gcloudCommandFactory = new GcloudCommandFactory(gcloud);
    SdkExecutorServiceFactory executorServiceFactory = new SingleThreadExecutorServiceFactory();

    return new SdkUpdater(gcloudCommandFactory, executorServiceFactory);
  }
}
