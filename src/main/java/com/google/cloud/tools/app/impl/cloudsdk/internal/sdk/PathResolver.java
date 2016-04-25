/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.app.impl.cloudsdk.internal.sdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * Resolve paths with CloudSdk and Python defaults.
 */
public enum PathResolver {

  INSTANCE;

  /**
   * Attempts to find the path to Google Cloud SDK home directory.
   *
   * @return Path to Google Cloud SDK or null
   */
  public Path getCloudSdkPath() throws FileNotFoundException {
    String sdkDir = System.getenv("GOOGLE_CLOUD_SDK_HOME");
    if (sdkDir == null) {
      boolean isWindows = System.getProperty("os.name").contains("Windows");
      if (isWindows) {
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles == null) {
          programFiles = System.getenv("ProgramFiles(x86)");
        }
        if (programFiles == null) {
          throw new FileNotFoundException(
              "Could not find ProgramFiles, please set the GOOGLE_CLOUD_SDK_HOME"
                  + " environment variable");
        } else {
          sdkDir = programFiles + "\\Google\\Cloud SDK\\google-cloud-sdk";
        }
      } else {
        sdkDir = System.getProperty("user.home") + "/google-cloud-sdk";
        if (!new File(sdkDir).exists()) {
          // try devshell VM:
          sdkDir = "/google/google-cloud-sdk";
          if (!new File(sdkDir).exists()) {
            // try bitnami Jenkins VM:
            sdkDir = "/usr/local/share/google/google-cloud-sdk";
          }
        }
      }
    }
    File file = new File(sdkDir);
    if (file.exists()) {
      return file.toPath();
    } else {
      return null;
    }
  }
}
