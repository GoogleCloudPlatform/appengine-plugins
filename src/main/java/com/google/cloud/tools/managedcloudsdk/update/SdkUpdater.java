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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/** Update an SDK. */
public class SdkUpdater {

  private final Path gcloud;
  private final UpdaterFactory updaterFactory;
  private final SdkExecutorServiceFactory executorServiceFactory;

  /** Use {@link #newUpdater} to instantiate. */
  SdkUpdater(
      Path gcloud,
      UpdaterFactory updaterFactory,
      SdkExecutorServiceFactory executorServiceFactory) {
    this.gcloud = gcloud;
    this.updaterFactory = updaterFactory;
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
                updaterFactory.newUpdater(gcloud, messageListener).update();
                return null;
              }
            });
    executorService.shutdown(); // shutdown executor after install
    return resultFuture;
  }

  /**
   * Configure and create a new Updater instance.
   *
   * @param gcloud path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk updater
   */
  public static SdkUpdater newUpdater(Path gcloud) {

    UpdaterFactory updaterFactory = new UpdaterFactory();
    SdkExecutorServiceFactory executorServiceFactory = new SingleThreadExecutorServiceFactory();

    return new SdkUpdater(gcloud, updaterFactory, executorServiceFactory);
  }
}
