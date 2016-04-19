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

package com.google.cloud.tools.app.internal.sdk;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Default implementation of CloudSdk interface for known install locations
 */
public class CloudSdk {

  // TODO : does this continue to work on windows?
  static final String GCLOUD = "bin/gcloud";
  static final String DEV_APPSERVER_PY = "bin/dev_appserver.py";
  static final String JAVA_APPENGINE_SDK_PATH = "platform/google_appengine/google/appengine/tools/java/lib";
  static final String JAVA_TOOLS_JAR = "appengine-tools-api.jar";

  private Path sdkPath = null;

  public CloudSdk(String sdkPath) {
    if (sdkPath == null) {
      throw new NullPointerException("sdkPath cannot be null - use PathResolver for defaults");
    }
    this.sdkPath = Paths.get(sdkPath);
  }

  public CloudSdk(Path sdkPath) {
    if (sdkPath == null) {
      throw new NullPointerException("sdkPath cannot be null - use PathResolver for defaults");
    }
    this.sdkPath = sdkPath;
  }

  public Path getSdkPath() {
    return sdkPath;
  }

  public Path getGCloudPath() {
    return sdkPath.resolve(GCLOUD);
  }

  public Path getDevAppServerPath() {
    return sdkPath.resolve(DEV_APPSERVER_PY);
  }

  public Path getJavaAppEngineSdkPath() {
    return sdkPath.resolve(JAVA_APPENGINE_SDK_PATH);
  }

  public Path getJavaToolsJar() {
    return getJavaAppEngineSdkPath().resolve(JAVA_TOOLS_JAR);
  }

  /**
   * For validation purposes, though should not be in use
   */
  public void validate() throws CloudSdkConfigurationException {
    if (sdkPath == null) {
      throw new CloudSdkConfigurationException("Validation Error : Sdk path is null");
    }
    if (sdkPath.toFile().isDirectory()) {
      throw new CloudSdkConfigurationException(
          "Validation Error : Sdk directory '" + sdkPath + "' is not valid");
    }
    if (getGCloudPath().toFile().isFile()) {
      throw new CloudSdkConfigurationException(
          "Validation Error : gcloud path '" + getGCloudPath() + "' is not valid");
    }
    if (getDevAppServerPath().toFile().isFile()) {
      throw new CloudSdkConfigurationException(
          "Validation Error : dev_appserver.py path '" + getDevAppServerPath() + "' is not valid");
    }
    if (getJavaAppEngineSdkPath().toFile().isFile()) {
      throw new CloudSdkConfigurationException(
          "Validation Error : Java App Engine SDK path '" + getJavaAppEngineSdkPath()
              + "' is not valid");
    }
    if (getJavaToolsJar().toFile().isFile()) {
      throw new CloudSdkConfigurationException(
          "Validation Error : Java Tools jar path '" + getJavaToolsJar() + "' is not valid");
    }
  }

  public CloudSdkCommandFactory getCommandFactory() {
    return new CloudSdkCommandFactory(this);
  }
}
