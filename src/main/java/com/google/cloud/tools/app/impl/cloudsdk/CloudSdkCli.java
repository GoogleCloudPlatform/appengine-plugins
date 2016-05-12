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

package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessExitListener;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessOutputLineListener;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdkConfigurationException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.PathResolver;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CloudSdkCli {

  private static final Logger log = Logger.getLogger(CloudSdkCli.class.toString());

  private final CloudSdk sdk;
  private final ProcessRunner processRunner;
  private final File gcloudCredentialOverride;
  private final String gcloudOutputFormat;
  private final Map<String, String> gcloudEnvironment;
  private boolean isUsed;

  private CloudSdkCli(CloudSdk sdk, ProcessRunner processRunner, File gcloudCredentialOverride,
      String gcloudOutputFormat, Map<String, String> gcloudEnvironment) {
    this.sdk = sdk;
    this.processRunner = processRunner;
    this.gcloudCredentialOverride = gcloudCredentialOverride;
    this.gcloudOutputFormat = gcloudOutputFormat;
    this.gcloudEnvironment = gcloudEnvironment;
    this.isUsed = false;
  }

  public enum Environment {
    CLOUDSDK_METRICS_ENVIROMENT,
    CLOUDSDK_METRICS_ENVIRONMENT_VERSION,
    CLOUDSDK_APP_USE_GSUTIL
  }

  public static class Builder {

    private CloudSdk sdk;
    private ProcessOutputLineListener outputListener;
    private ProcessOutputLineListener errorListener;
    private boolean redirectError;
    private ProcessExitListener exitListener;
    private boolean async;
    private Map<String, String> gcloudEnvironment = Maps.newHashMap();
    private File gcloudCredentialOverride;
    private String gcloudOutputFormat;

    /**
     * @param sdkPath Path to cloud sdk installation.
     */
    public Builder(File sdkPath) {
      if (sdkPath == null) {
        Path discoveredSdkPath = PathResolver.INSTANCE.getCloudSdkPath();
        if (discoveredSdkPath == null) {
          throw new CloudSdkConfigurationException("Google Cloud SDK path was not provided and"
              + " could not be found in any known install locations.");
        }
        sdkPath = discoveredSdkPath.toFile();
      }
      sdk = new CloudSdk(sdkPath.toPath());
    }

    /**
     * Run the process asynchronously.
     */
    public Builder async() {
      this.async = true;
      return this;
    }

    /**
     * Client listener on the process output stream.
     */
    public Builder stdOutListener(ProcessOutputLineListener listener) {
      this.outputListener = listener;
      return this;
    }

    /**
     * Client listener on the process error stream.
     */
    public Builder stdErrListener(ProcessOutputLineListener listener) {
      this.errorListener = listener;
      return this;
    }

    /**
     * Client listener of the process exit with code.
     */
    public Builder exitListener(ProcessExitListener listener) {
      this.exitListener = listener;
      return this;
    }

    /**
     * Redirect error stream to standard output stream.
     */
    public Builder redirectError(boolean redirect) {
      this.redirectError = redirect;
      return this;
    }

    /**
     * Override file for gcloud credential configuration.
     */
    public Builder gcloudCredentialOverride(File override) {
      this.gcloudCredentialOverride = override;
      return this;
    }

    /**
     * Format for gcloud output.
     */
    public Builder gcloudOutputFormat(String format) {
      this.gcloudOutputFormat = format;
      return this;
    }

    /**
     * An environment variable for gcloud invocations.
     */
    public Builder environmentVariable(Environment key, String value) {
      gcloudEnvironment.put(key.toString(), value);
      return this;
    }

    /**
     * @return a CloudSdkCli instance.
     */
    public CloudSdkCli build() {
      ProcessBuilder processBuilder = new ProcessBuilder();
      // this is the stdin, should we just close the stdin stream?
      processBuilder.redirectOutput(Redirect.INHERIT);

      if (outputListener == null) {
        processBuilder.redirectInput(Redirect.INHERIT);
      }
      if (errorListener == null) {
        processBuilder.redirectError(Redirect.INHERIT);
      }
      if (redirectError) {
        processBuilder.redirectErrorStream(true);
      }

      ProcessRunner processRunner = new ProcessRunner(processBuilder);
      processRunner.setStdOutLineListener(outputListener);
      processRunner.setStdOutLineListener(errorListener);
      processRunner.setExitListener(exitListener);
      processRunner.setAsync(async);

      return new CloudSdkCli(sdk, processRunner, gcloudCredentialOverride, gcloudOutputFormat,
          gcloudEnvironment);
    }

  }

  /**
   * Uses the process runner to execute the gcloud app command with the provided arguments.
   *
   * @param args The arguments to pass to "gcloud app" command.
   */
  public void runGcloudAppCommand(List<String> args) throws ProcessRunnerException {
    checkIfUsed();
    List<String> command = new ArrayList<>();
    command.add(sdk.getGCloudPath().toString());
    command.add("preview");
    command.add("app");
    command.addAll(args);
    command.addAll(Args.filePath("credential-file-override", gcloudCredentialOverride));
    command.addAll(Args.string("format", gcloudOutputFormat));

    logCommand(command);

    processRunner.setEnvironment(gcloudEnvironment);
    processRunner.run(command.toArray(new String[command.size()]));
  }

  /**
   * Uses the process runner to execute a dev_appserver.py command.
   *
   * @param args The arguments to pass to dev_appserver.py.
   * @throws ProcessRunnerException When process runner encounters an error.
   */
  public void runDevAppServerCommand(List<String> args) throws ProcessRunnerException {
    checkIfUsed();
    List<String> command = new ArrayList<>();
    command.add(sdk.getDevAppServerPath().toString());
    command.addAll(args);

    logCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
  }

  /**
   * Uses the process runner to execute a appcfg.java command.
   *
   * @param args The arguments to pass to dev_appserver.py.
   * @throws ProcessRunnerException When process runner encounters an error.
   */
  public void runAppCfgCommand(List<String> args) throws ProcessRunnerException {
    checkIfUsed();
    // AppEngineSdk requires this system property to be set.
    System.setProperty("appengine.sdk.root", sdk.getJavaAppEngineSdkPath().toString());

    List<String> command = new ArrayList<>();
    command.add(
        Paths.get(System.getProperty("java.home")).resolve("bin/java").toAbsolutePath().toString());
    command.add("-cp");
    command.add(sdk.getJavaToolsJar().toAbsolutePath().toString());
    command.add("com.google.appengine.tools.admin.AppCfg");
    command.addAll(args);

    logCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
  }

  private void logCommand(List<String> command) {
    Joiner joiner = Joiner.on(" ");
    log.info("Executing command: " + joiner.join(command));
  }

  private synchronized void checkIfUsed() {
    if (isUsed) {
      throw new IllegalStateException(
          "Cloud Sdk Cli already consumed, create a new one to run another CLI command");
    }
    isUsed = true;
  }
}
