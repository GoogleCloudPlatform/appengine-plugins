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

import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.SimpleProcessRunner;
import com.google.common.base.Joiner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Cloud SDK CLI wrapper.
 */
public class CloudSdk {

  private static final Logger log = Logger.getLogger(CloudSdk.class.toString());

  // TODO : does this continue to work on windows?
  static final String GCLOUD = "bin/gcloud";
  static final String DEV_APPSERVER_PY = "bin/dev_appserver.py";
  static final String JAVA_APPENGINE_SDK_PATH =
      "platform/google_appengine/google/appengine/tools/java/lib";
  static final String JAVA_TOOLS_JAR = "appengine-tools-api.jar";

  private Path sdkPath = null;
  private ProcessRunner processRunner = null;

  public CloudSdk(Path sdkPath) {
    this(sdkPath, new SimpleProcessRunner());
  }

  /**
   * Initializes an instance using the specified SDK path and ProcessRunner.
   *
   * @param sdkPath The home directory of Google Cloud SDK
   */
  public CloudSdk(Path sdkPath, ProcessRunner processRunner) {
    if (sdkPath == null) {
      throw new NullPointerException("sdkPath cannot be null - use PathResolver for defaults");
    }
    this.sdkPath = sdkPath;
    this.processRunner = processRunner;
  }

  /**
   * Uses the process runner to execute the gcloud app command with the provided arguments.
   *
   * @param args The arguments to pass to "gcloud app" command.
   * @return The result of the process execution. 0 = successful.
   */
  public int runAppCommand(List<String> args) throws ProcessRunnerException {
    List<String> command = new ArrayList<>();
    command.add(getGCloudPath().toString());
    command.add("preview");
    command.add("app");
    command.addAll(args);

    outputCommand(command);

    return processRunner.run(command.toArray(new String[command.size()]));
  }

  /**
   * Uses the process runner to execute a dev_appserver.py command.
   * @param args The arguments to pass to dev_appserver.py.
   * @return The result of the process execution. 0 = successful.
   * @throws ProcessRunnerException When process runner encounters an error.
   */
  public int runDevAppServerCommand(List<String> args) throws ProcessRunnerException {
    List<String> command = new ArrayList<>();
    command.add(getDevAppServerPath().toString());
    command.addAll(args);

    outputCommand(command);

    return processRunner.run(command.toArray(new String[command.size()]));
  }

  private void outputCommand(List<String> command) {
    Joiner joiner = Joiner.on(" ");
    log.info("submitting command: " + joiner.join(command));
  }

  private Path getSdkPath() {
    return sdkPath;
  }

  private Path getGCloudPath() {
    return sdkPath.resolve(GCLOUD);
  }

  private Path getDevAppServerPath() {
    return sdkPath.resolve(DEV_APPSERVER_PY);
  }

  private Path getJavaAppEngineSdkPath() {
    return sdkPath.resolve(JAVA_APPENGINE_SDK_PATH);
  }

  private Path getJavaToolsJar() {
    return getJavaAppEngineSdkPath().resolve(JAVA_TOOLS_JAR);
  }


  /**
   * For validation purposes, though should not be in use.
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

}
