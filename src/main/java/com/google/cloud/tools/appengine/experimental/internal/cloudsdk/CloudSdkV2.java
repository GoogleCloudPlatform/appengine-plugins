/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.appengine.experimental.internal.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.common.annotations.VisibleForTesting;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cloud SDK CLI wrapper.
 * TODO : Need to revisit usage of path resolver.
 */
public class CloudSdkV2 {
  private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
  private static final String GCLOUD = "bin/gcloud";
  private static final String DEV_APPSERVER_PY = "bin/dev_appserver.py";
  private static final String JAVA_APPENGINE_SDK_PATH =
      "platform/google_appengine/google/appengine/tools/java/lib";
  private static final String JAVA_TOOLS_JAR = "appengine-tools-api.jar";
  private static final String WINDOWS_BUNDLED_PYTHON = "platform/bundledpython/python.exe";

  private final Path sdkPath;

  public CloudSdkV2(Path sdkPath) {
    this.sdkPath = sdkPath;
  }

  public Path getSdkPath() {
    return sdkPath;
  }

  /**
   * return gcloud path.
   */
  public Path getGCloudPath() {
    String gcloud = GCLOUD;
    if (IS_WINDOWS) {
      gcloud += ".cmd";
    }
    return getSdkPath().resolve(gcloud);
  }

  private Path getDevAppServerPath() {
    return getSdkPath().resolve(DEV_APPSERVER_PY);
  }

  public Path getJavaAppEngineSdkPath() {
    return getSdkPath().resolve(JAVA_APPENGINE_SDK_PATH);
  }

  @VisibleForTesting
  Path getWindowsPythonPath() {
    String cloudSdkPython = System.getenv("CLOUDSDK_PYTHON");
    if (cloudSdkPython != null) {
      Path cloudSdkPythonPath = Paths.get(cloudSdkPython);
      if (Files.exists(cloudSdkPythonPath)) {
        return cloudSdkPythonPath;
      } else {
        throw new InvalidPathException(cloudSdkPython, "python binary not in specified location");
      }
    }

    Path pythonPath = getSdkPath().resolve(WINDOWS_BUNDLED_PYTHON);
    if (Files.exists(pythonPath)) {
      return pythonPath;
    } else {
      return Paths.get("python");
    }

  }

  /**
   * Checks whether the configured Cloud SDK Path is valid.
   *
   * @throws AppEngineException when there is a validation error.
   */
  public void validate() throws AppEngineException {
    if (sdkPath == null) {
      throw new AppEngineException("Validation Error: SDK path is null");
    }
    if (!Files.isDirectory(sdkPath)) {
      throw new AppEngineException(
          "Validation Error: SDK location '" + sdkPath + "' is not a directory.");
    }
    if (!Files.isRegularFile(getGCloudPath())) {
      throw new AppEngineException(
          "Validation Error: gcloud location '" + getGCloudPath() + "' is not a file.");
    }
    if (!Files.isRegularFile(getDevAppServerPath())) {
      throw new AppEngineException(
          "Validation Error: dev_appserver.py location '"
              + getDevAppServerPath() + "' is not a file.");
    }
    if (!Files.isDirectory(getJavaAppEngineSdkPath())) {
      throw new AppEngineException(
          "Validation Error: Java App Engine SDK location '"
              + getJavaAppEngineSdkPath() + "' is not a directory.");
    }
  }
}
