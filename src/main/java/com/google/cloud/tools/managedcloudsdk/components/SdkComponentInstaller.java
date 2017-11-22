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
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Install an SDK component. */
public class SdkComponentInstaller {

  private final Path gcloud;
  private final CommandRunner commandRunner;

  /** Use {@link #newComponentInstaller} to instantiate. */
  SdkComponentInstaller(Path gcloud, CommandRunner commandRunner) {
    this.gcloud = gcloud;
    this.commandRunner = commandRunner;
  }

  /**
   * Install a component.
   *
   * @param component component to install
   * @param messageListener listener to receive feedback
   */
  public void installComponent(SdkComponent component, MessageListener messageListener)
      throws InterruptedException, CommandExitException, CommandExecutionException {
    List<String> command =
        Arrays.asList(gcloud.toString(), "components", "install", component.toString(), "--quiet");
    commandRunner.run(command, null, null, messageListener);
  }

  /**
   * Configure and create a new Component Installer instance.
   *
   * @param gcloud full path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk component installer
   */
  public static SdkComponentInstaller newComponentInstaller(Path gcloud) {
    return new SdkComponentInstaller(gcloud, CommandRunner.newRunner());
  }
}
