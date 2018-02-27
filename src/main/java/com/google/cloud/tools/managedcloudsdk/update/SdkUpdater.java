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

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.OsInfo;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandCaller;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Update an SDK. */
public interface SdkUpdater {

  void update(ProgressListener progressListener, ConsoleListener consoleListener)
      throws InterruptedException, CommandExitException, CommandExecutionException;

  /**
   * Configure and create a new Updater instance.
   *
   * @param gcloud path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk updater
   */
  static SdkUpdater newUpdater(OsInfo.Name osName, Path gcloud) {
    List<String> updaterParams = Arrays.asList("components", "update", "--quiet");
    switch (osName) {
      case WINDOWS:
        return new WindowsUpdater(
            gcloud, CommandCaller.newCaller(), CommandRunner.newRunner(), updaterParams);
      default:
        return new UnixUpdater(gcloud, CommandRunner.newRunner(), updaterParams);
    }
  }
}
