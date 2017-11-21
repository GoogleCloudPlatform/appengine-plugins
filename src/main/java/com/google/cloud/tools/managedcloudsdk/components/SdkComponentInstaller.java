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
import com.google.cloud.tools.managedcloudsdk.command.AsyncCommandWrapper;
import com.google.cloud.tools.managedcloudsdk.command.CommandFactory;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Install an SDK component. */
public class SdkComponentInstaller {

  private final AsyncCommandWrapper asyncCommandWrapper;
  private final CommandFactory commandFactory;
  private final Path gcloud;

  /** Use {@link #newComponentInstaller} to instantiate. */
  SdkComponentInstaller(
      Path gcloud, CommandFactory commandFactory, AsyncCommandWrapper asyncCommandWrapper) {
    this.gcloud = gcloud;
    this.commandFactory = commandFactory;
    this.asyncCommandWrapper = asyncCommandWrapper;
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
    return asyncCommandWrapper.execute(
        commandFactory.newRunner(getCommand(component), null, null, messageListener));
  }

  private List<String> getCommand(SdkComponent component) {
    return Arrays.asList(
        gcloud.toString(), "components", "install", component.toString(), "--quiet");
  }

  /**
   * Configure and create a new Component Installer instance.
   *
   * @param gcloud full path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk component installer
   */
  public static SdkComponentInstaller newComponentInstaller(Path gcloud) {
    return new SdkComponentInstaller(
        gcloud, new CommandFactory(), AsyncCommandWrapper.newCommandWrapper());
  }
}
