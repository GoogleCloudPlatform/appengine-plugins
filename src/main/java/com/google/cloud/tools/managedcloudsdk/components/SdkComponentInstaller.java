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

package com.google.cloud.tools.managedcloudsdk.components;

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

/** Install an SDK component. */
public class SdkComponentInstaller {

  private final GcloudCommandFactory commandFactory;
  private final SdkExecutorServiceFactory executorServiceFactory;

  /** Use {@link #newComponentInstaller} to instantiate. */
  SdkComponentInstaller(
      GcloudCommandFactory commandFactory, SdkExecutorServiceFactory executorServiceFactory) {
    this.commandFactory = commandFactory;
    this.executorServiceFactory = executorServiceFactory;
  }

  /**
   * Install a component on a separate thread.
   *
   * @param component component to install
   * @param messageListener listener to receive feedback
   * @return a resultless future for controlling the process
   */
  public ListenableFuture<Void> installComponent(
      final SdkComponent component, final MessageListener messageListener) {
    ListeningExecutorService executorService = executorServiceFactory.newExecutorService();
    ListenableFuture<Void> resultFuture =
        executorService.submit(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                commandFactory.newCommand(getParameters(component), messageListener).run();
                return null;
              }
            });
    executorService.shutdown(); // shutdown executor after install
    return resultFuture;
  }

  List<String> getParameters(SdkComponent component) {
    List<String> command = new ArrayList<>();
    command.add("components");
    command.add("install");
    command.add(component.toString());
    command.add("--quiet");
    return command;
  }

  /**
   * Configure and create a new Component Installer instance.
   *
   * @param gcloud full path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk component installer
   */
  public static SdkComponentInstaller newComponentInstaller(Path gcloud) {

    GcloudCommandFactory componentInstallerFactory = new GcloudCommandFactory(gcloud);
    SdkExecutorServiceFactory executorServiceFactory = new SingleThreadExecutorServiceFactory();

    return new SdkComponentInstaller(componentInstallerFactory, executorServiceFactory);
  }
}
