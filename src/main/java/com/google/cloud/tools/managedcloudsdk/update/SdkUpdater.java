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
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandFactory;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Update an SDK. */
public class SdkUpdater {

  private final Path gcloud;
  private final CommandFactory commandFactory;

  /** Use {@link #newUpdater} to instantiate. */
  SdkUpdater(Path gcloud, CommandFactory commandFactory) {
    this.gcloud = gcloud;
    this.commandFactory = commandFactory;
  }

  /**
   * Update the Cloud SDK.
   *
   * @param messageListener listener to receive feedback on
   */
  public void update(final MessageListener messageListener)
      throws InterruptedException, CommandExitException, CommandExecutionException {
    commandFactory.newRunner(getParameters(), null, null, messageListener).run();
  }

  List<String> getParameters() {
    return Arrays.asList(gcloud.toString(), "components", "update", "--quiet");
  }

  /**
   * Configure and create a new Updater instance.
   *
   * @param gcloud path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk updater
   */
  public static SdkUpdater newUpdater(Path gcloud) {
    return new SdkUpdater(gcloud, new CommandFactory());
  }
}
