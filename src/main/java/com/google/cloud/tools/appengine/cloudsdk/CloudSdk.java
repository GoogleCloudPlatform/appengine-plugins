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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.DefaultProcessRunner;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunner;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.WaitingProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessStartListener;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Cloud SDK CLI wrapper.
 */
public class CloudSdk {

  private static final Logger logger = Logger.getLogger(CloudSdk.class.toString());
  private static final Joiner WHITESPACE_JOINER = Joiner.on(" ");

  // TODO : does this continue to work on windows?
  private static final String GCLOUD = "bin/gcloud";
  private static final String DEV_APPSERVER_PY = "bin/dev_appserver.py";
  private static final String JAVA_APPENGINE_SDK_PATH =
      "platform/google_appengine/google/appengine/tools/java/lib";
  private static final String JAVA_TOOLS_JAR = "appengine-tools-api.jar";

  private final Path sdkPath;
  private final ProcessRunner processRunner;
  private final String appCommandMetricsEnvironment;
  private final String appCommandMetricsEnvironmentVersion;
  @Nullable private final File appCommandCredentialFile;
  private final String appCommandOutputFormat;
  private final int runDevAppServerWaitSeconds;
  private final WaitingProcessOutputLineListener runDevAppServerWaitListener;


  private CloudSdk(Path sdkPath, String appCommandMetricsEnvironment,
                   String appCommandMetricsEnvironmentVersion,
                   @Nullable File appCommandCredentialFile, String appCommandOutputFormat,
                   boolean async,
                   List<ProcessOutputLineListener> stdOutLineListeners,
                   List<ProcessOutputLineListener> stdErrLineListeners,
                   ProcessExitListener exitListener, ProcessStartListener startListener,
                   int runDevAppServerWaitSeconds) {
    this.sdkPath = sdkPath;
    this.appCommandMetricsEnvironment = appCommandMetricsEnvironment;
    this.appCommandMetricsEnvironmentVersion = appCommandMetricsEnvironmentVersion;
    this.appCommandCredentialFile = appCommandCredentialFile;
    this.appCommandOutputFormat = appCommandOutputFormat;

    // configure listeners for async dev app server start with waiting
    if (async && runDevAppServerWaitSeconds > 0) {
      this.runDevAppServerWaitSeconds = runDevAppServerWaitSeconds;
      this.runDevAppServerWaitListener = new WaitingProcessOutputLineListener(
          "Dev App Server is now running", runDevAppServerWaitSeconds);

      stdOutLineListeners.add(runDevAppServerWaitListener);
      stdErrLineListeners.add(runDevAppServerWaitListener);
    } else {
      this.runDevAppServerWaitSeconds = 0;
      this.runDevAppServerWaitListener = null;
    }

    // create process runner
    this.processRunner = new DefaultProcessRunner(async, stdOutLineListeners, stdErrLineListeners,
        exitListener, startListener);

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

    Map<String, String> environment = Maps.newHashMap();
    if (appCommandCredentialFile != null) {
      command.addAll(GcloudArgs.get("credential-file-override", appCommandCredentialFile));
      environment.put("CLOUDSDK_APP_USE_GSUTIL", "0");
    }
    if (appCommandMetricsEnvironment != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT", appCommandMetricsEnvironment);
    }
    if (appCommandMetricsEnvironmentVersion != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT_VERSION", appCommandMetricsEnvironmentVersion);
    }
    logCommand(command);
    processRunner.setEnvironment(environment);
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

    logCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));

    // wait for start if configured
    if (runDevAppServerWaitListener != null) {
      runDevAppServerWaitListener.await();
    }
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

    logCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
  }

  private void logCommand(List<String> command) {
    logger.info("submitting command: " + WHITESPACE_JOINER.join(command));
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
   * Checks whether the configured Cloud SDK Path is valid.
   *
   * @throws AppEngineException when there is a validation error.
   */
  public void validate() throws AppEngineException {
    if (sdkPath == null) {
      throw new AppEngineException("Validation Error: SDK path is null");
    }
    if (!sdkPath.toFile().isDirectory()) {
      throw new AppEngineException(
          "Validation Error: SDK directory '" + sdkPath + "' is not valid");
    }
    if (!getGCloudPath().toFile().isFile()) {
      throw new AppEngineException(
          "Validation Error: gcloud path '" + getGCloudPath() + "' is not valid");
    }
    if (!getDevAppServerPath().toFile().isFile()) {
      throw new AppEngineException(
          "Validation Error: dev_appserver.py path '" + getDevAppServerPath() + "' is not valid");
    }
    if (!getJavaAppEngineSdkPath().toFile().isDirectory()) {
      throw new AppEngineException(
          "Validation Error: Java App Engine SDK path '" + getJavaAppEngineSdkPath()
              + "' is not valid");
    }
    if (!getJavaToolsJar().toFile().isFile()) {
      throw new AppEngineException(
          "Validation Error: Java Tools jar path '" + getJavaToolsJar() + "' is not valid");
    }
  }

  public static class Builder {
    private Path sdkPath;
    private String appCommandMetricsEnvironment;
    private String appCommandMetricsEnvironmentVersion;
    @Nullable private File appCommandCredentialFile;
    private String appCommandOutputFormat;
    private boolean async = false;
    private List<ProcessOutputLineListener> stdOutLineListeners = new ArrayList<>();
    private List<ProcessOutputLineListener> stdErrLineListeners = new ArrayList<>();
    private ProcessExitListener exitListener;
    private ProcessStartListener startListener;
    private int runDevAppServerWaitSeconds;

    /**
     * The home directory of Google Cloud SDK. If not set, will attempt to look for the SDK in known
     * install locations.
     */
    public Builder sdkPath(File sdkPathFile) {
      if (sdkPathFile != null) {
        this.sdkPath = sdkPathFile.toPath();
      }
      return this;
    }

    /**
     * The metrics environment.
     */
    public Builder appCommandMetricsEnvironment(String appCommandMetricsEnvironment) {
      this.appCommandMetricsEnvironment = appCommandMetricsEnvironment;
      return this;
    }

    /**
     * The metrics environment version.
     */
    public Builder appCommandMetricsEnvironmentVersion(
        String appCommandMetricsEnvironmentVersion) {
      this.appCommandMetricsEnvironmentVersion = appCommandMetricsEnvironmentVersion;
      return this;
    }

    /**
     * Sets the path the credential override file.
     */
    public Builder appCommandCredentialFile(File appCommandCredentialFile) {
      this.appCommandCredentialFile = appCommandCredentialFile;
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
     * Whether to run commands asynchronously.
     */
    public Builder async(boolean async) {
      this.async = async;
      return this;
    }

    /**
     * Adds a client consumer of process standard output. If none, output will be inherited by
     * parent process.
     */
    public Builder addStdOutLineListener(ProcessOutputLineListener stdOutLineListener) {
      this.stdOutLineListeners.add(stdOutLineListener);
      return this;
    }

    /**
     * Adds a client consumer of process error output. If none, output will be inherited by parent
     * process.
     */
    public Builder addStdErrLineListener(ProcessOutputLineListener stdErrLineListener) {
      this.stdErrLineListeners.add(stdErrLineListener);
      return this;
    }

    /**
     * The client listener of the process exit with code.
     */
    public Builder exitListener(ProcessExitListener exitListener) {
      this.exitListener = exitListener;
      return this;
    }

    /**
     * The client listener of the process start. Allows access to the underlying process.
     */
    public Builder startListener(ProcessStartListener startListener) {
      this.startListener = startListener;
      return this;
    }

    /**
     * When run asynchronously, configure the Dev App Server command to wait for successful start of
     * the server. Setting this will force process output not to be inherited by the caller.
     *
     * @param runDevAppServerWaitSeconds Number of seconds to wait > 0.
     */
    public Builder runDevAppServerWait(int runDevAppServerWaitSeconds) {
      this.runDevAppServerWaitSeconds = runDevAppServerWaitSeconds;
      return this;
    }

    /**
     * Create a new instance of {@link CloudSdk}.
     */
    public CloudSdk build() {

      // Default SDK path
      if (sdkPath == null) {
        Path discoveredSdkPath = PathResolver.INSTANCE.getCloudSdkPath();
        if (discoveredSdkPath == null) {
          throw new AppEngineException("Google Cloud SDK path was not provided and could not be"
              + " found in any known install locations.");
        }
        sdkPath = discoveredSdkPath;
      }

      return new CloudSdk(sdkPath, appCommandMetricsEnvironment,
          appCommandMetricsEnvironmentVersion, appCommandCredentialFile,
          appCommandOutputFormat, async, stdOutLineListeners, stdErrLineListeners, exitListener,
          startListener, runDevAppServerWaitSeconds);
    }

  }
}
