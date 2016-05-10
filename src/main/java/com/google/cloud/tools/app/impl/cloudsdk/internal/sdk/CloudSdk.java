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

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.DefaultProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Joiner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  private final Path sdkPath;
  private final ProcessRunner processRunner;
  private final String appCommandOutputFormat;

  private CloudSdk(Builder builder) {
    this.sdkPath = builder.sdkPath;
    this.processRunner = builder.processRunner;
    this.appCommandOutputFormat = builder.appCommandOutputFormat;
  }

  /**
   * Uses the process runner to execute the gcloud app command with the provided arguments.
   *
   * @param args The arguments to pass to "gcloud app" command.
   */
  public void runAppCommand(List<String> args) throws ProcessRunnerException {
    List<String> command = new ArrayList<>();
    command.add(getGCloudPath().toString());
    command.add("preview");
    command.add("app");
    command.addAll(args);

    command.add("--quiet");
    command.addAll(Args.string("format", appCommandOutputFormat));

    outputCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
  }

  /**
   * Uses the process runner to execute a dev_appserver.py command.
   *
   * @param args The arguments to pass to dev_appserver.py.
   * @throws ProcessRunnerException When process runner encounters an error.
   */
  public void runDevAppServerCommand(List<String> args) throws ProcessRunnerException {
    List<String> command = new ArrayList<>();
    command.add(getDevAppServerPath().toString());
    command.addAll(args);

    outputCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
  }

  /**
   * Executes an App Engine SDK CLI command.
   */
  public void runAppCfgCommand(List<String> args) throws ProcessRunnerException {
    // AppEngineSdk requires this system property to be set.
    System.setProperty("appengine.sdk.root", getJavaAppEngineSdkPath().toString());

    List<String> command = new ArrayList<>();
    command.add(
        Paths.get(System.getProperty("java.home")).resolve("bin/java").toAbsolutePath().toString());
    command.add("-cp");
    command.add(getJavaToolsJar().toAbsolutePath().toString());
    command.add("com.google.appengine.tools.admin.AppCfg");
    command.addAll(args);

    outputCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
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

  public static class Builder {
    private Path sdkPath;
    private ProcessRunner processRunner;
    private String appCommandOutputFormat;

    /**
     * The home directory of Google Cloud SDK. If not set, will attempt to look for the SDK in known
     * install locations.
     */
    public Builder sdkPath(File sdkPathFile) {
      this.sdkPath = sdkPathFile.toPath();
      return this;
    }

    /**
     * The process runner used to execute CLI commands.
     */
    public Builder processRunner(ProcessRunner processRunner) {
      this.processRunner = processRunner;
      return this;
    }

    /**
     * Sets the format for printing command output resources. The default is a command-specific
     * human-friendly output format. The supported formats are: csv, default, flattened, json, list,
     * multi, none, table, text, value, yaml. For more details run $ gcloud topic formats.
     */
    public Builder appCommandOutputFormat(String appCommandOutputFormat) {
      this.appCommandOutputFormat = appCommandOutputFormat;
      return this;
    }

    /**
     * Create a new instance of {@link CloudSdk}.
     */
    public CloudSdk build() {
      // Default process runner
      if (processRunner == null) {
        processRunner = new DefaultProcessRunner();
      }

      // Default SDK path
      if (sdkPath == null) {
        Path discoveredSdkPath = PathResolver.INSTANCE.getCloudSdkPath();
        if (discoveredSdkPath == null) {
          throw new AppEngineException("Google Cloud SDK path was not provided and could not be"
              + " found in any known install locations.");
        }
        sdkPath = discoveredSdkPath;
      }

      return new CloudSdk(this);
    }

  }
}
