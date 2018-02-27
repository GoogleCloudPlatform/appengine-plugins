/*
 * Copyright 2018 Google Inc.
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
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandCaller;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WindowsUpdater implements SdkUpdater {

  private final Path gcloud;
  private final CommandCaller commandCaller;
  private final CommandRunner commandRunner;
  private final List<String> updaterParams;

  /** Use {@link SdkUpdater#newUpdater} to instantiate. */
  WindowsUpdater(
      Path gcloud,
      CommandCaller commandCaller,
      CommandRunner commandRunner,
      List<String> updaterParams) {
    this.gcloud = gcloud;
    this.commandCaller = commandCaller;
    this.commandRunner = commandRunner;
    this.updaterParams = updaterParams;
  }

  /**
   * Update the Cloud SDK.
   *
   * @param progressListener listener to action progress feedback
   * @param consoleListener listener to process console feedback
   */
  public void update(ProgressListener progressListener, ConsoleListener consoleListener)
      throws InterruptedException, CommandExitException, CommandExecutionException {
    progressListener.start("Updating Cloud SDK", ProgressListener.UNKNOWN);

    List<String> copyPythonCommand =
        Arrays.asList(gcloud.toString(), "components", "copy-bundled-python");
    String tempPythonLocation = commandCaller.call(copyPythonCommand, null, null);
    Map<String, String> environment = ImmutableMap.of("CLOUDSDK_PYTHON", tempPythonLocation);

    List<String> command = new ArrayList<>();
    command.add(gcloud.toString());
    command.addAll(updaterParams);
    commandRunner.run(command, null, environment, consoleListener);
    progressListener.done();
  }
}
