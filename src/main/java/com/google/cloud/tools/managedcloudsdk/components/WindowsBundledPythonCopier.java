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

import com.google.cloud.tools.managedcloudsdk.command.CommandCaller;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WindowsBundledPythonCopier implements BundledPythonCopier {

  private final Path gcloud;
  private final CommandCaller commandCaller;

  WindowsBundledPythonCopier(Path gcloud, CommandCaller commandCaller) {
    this.gcloud = gcloud;
    this.commandCaller = commandCaller;
  }

  @Override
  public Map<String, String> copyPython()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    List<String> copyPythonCommand =
        Arrays.asList(gcloud.toString(), "components", "copy-bundled-python");
    // The path returned from gcloud points to the "python.exe" binary, e.g.,
    // c:/users/ieuser/appdata/local/temp/tmpjmkt_z/python/python.exe
    // However, it may not copy but return an existing Python executable.
    //
    // A trim() required to remove newlines from call result. Using new lines in windows
    // environment passed through via ProcessBuilder will result in cryptic : "The syntax of
    // the command is incorrect."
    String pythonExePath = commandCaller.call(copyPythonCommand, null, null).trim();

    if (isUnderTempDirectory(pythonExePath, System.getenv())) {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteCopiedPython(pythonExePath)));
    }

    return ImmutableMap.of("CLOUDSDK_PYTHON", pythonExePath);
  }

  @VisibleForTesting
  static boolean isUnderTempDirectory(String pythonExePath, Map<String, String> environment) {
    Path pythonExe = Paths.get(pythonExePath);
    String temp = environment.get("TEMP");
    String tmp = environment.get("TMP");

    return (temp != null && pythonExe.startsWith(temp))
        || (tmp != null && pythonExe.startsWith(tmp));
  }

  @VisibleForTesting
  static void deleteCopiedPython(String pythonExePath) {
    // The path returned from gcloud points to the "python.exe" binary. Delete it from the path.
    String pythonHome = pythonExePath.replaceAll("[pP][yY][tT][hH][oO][nN]\\.[eE][xX][eE]$", "");
    boolean endsWithPythonExe = !pythonHome.equals(pythonExePath);

    if (endsWithPythonExe) { // just to be safe
      try {
        MoreFiles.deleteRecursively(Paths.get(pythonHome), RecursiveDeleteOption.ALLOW_INSECURE);
      } catch (IOException e) {
        // not critical to remove a temp directory
      }
    }
  }
}
