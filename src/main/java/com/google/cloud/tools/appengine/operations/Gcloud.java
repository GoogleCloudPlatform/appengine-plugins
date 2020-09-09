/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

package com.google.cloud.tools.appengine.operations;

import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.operations.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.operations.cloudsdk.internal.process.ExitCodeRecorderProcessExitListener;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.LegacyProcessHandler;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.StringBuilderProcessOutputLineListener;
import com.google.cloud.tools.appengine.operations.cloudsdk.serialization.CloudSdkComponent;
import com.google.cloud.tools.appengine.operations.cloudsdk.serialization.CloudSdkConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Operations that use gcloud. */
public class Gcloud {
  private final CloudSdk sdk;
  private final GcloudRunner.Factory gcloudRunnerFactory;
  @Nullable private final String metricsEnvironment;
  @Nullable private final String metricsEnvironmentVersion;
  @Nullable private final Path credentialFile;
  @Nullable private final List<Path> flagsFiles;
  @Nullable private final String outputFormat;
  @Nullable private final String showStructuredLogs;
  @Nullable private final String verbosity;

  private Gcloud(
      CloudSdk sdk,
      GcloudRunner.Factory gcloudRunnerFactory,
      @Nullable String metricsEnvironment,
      @Nullable String metricsEnvironmentVersion,
      @Nullable Path credentialFile,
      @Nullable List<Path> flagsFiles,
      @Nullable String outputFormat,
      @Nullable String showStructuredLogs,
      @Nullable String verbosity) {
    this.gcloudRunnerFactory = gcloudRunnerFactory;
    this.sdk = sdk;
    this.metricsEnvironment = metricsEnvironment;
    this.metricsEnvironmentVersion = metricsEnvironmentVersion;
    this.credentialFile = credentialFile;
    this.flagsFiles = flagsFiles;
    this.outputFormat = outputFormat;
    this.showStructuredLogs = showStructuredLogs;
    this.verbosity = verbosity;
  }

  public Deployment newDeployment(ProcessHandler processHandler) {
    return new Deployment(getRunner(processHandler));
  }

  public Versions newVersions(ProcessHandler processHandler) {
    return new Versions(getRunner(processHandler));
  }

  public Auth newAuth(ProcessHandler processHandler) {
    return new Auth(getRunner(processHandler));
  }

  public GenRepoInfoFile newGenRepoInfo(ProcessHandler processHandler) {
    return new GenRepoInfoFile(getRunner(processHandler));
  }

  /**
   * Returns the list of Cloud SDK Components and their settings, reported by the current gcloud
   * installation. Unlike other methods in this class that call gcloud, this method always uses a
   * synchronous ProcessRunner and will block until the gcloud process returns.
   *
   * @throws ProcessHandlerException when process runner encounters an error
   * @throws JsonSyntaxException when the cloud SDK output cannot be parsed
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  public List<CloudSdkComponent> getComponents()
      throws ProcessHandlerException, JsonSyntaxException, CloudSdkNotFoundException,
          CloudSdkOutOfDateException, CloudSdkVersionFileException, IOException {
    sdk.validateCloudSdk();

    // gcloud components list --show-versions --format=json
    List<String> command =
        new ImmutableList.Builder<String>()
            .add("components", "list")
            .addAll(GcloudArgs.get("show-versions", true))
            .addAll(GcloudArgs.get("format", "json"))
            .build();

    String componentsJson = runCommand(command);
    return CloudSdkComponent.fromJsonList(componentsJson);
  }

  /**
   * Returns a representation of gcloud config, it makes a synchronous call to gcloud config list to
   * do so.
   */
  public CloudSdkConfig getConfig()
      throws CloudSdkNotFoundException, CloudSdkOutOfDateException, CloudSdkVersionFileException,
          IOException, ProcessHandlerException {
    sdk.validateCloudSdk();

    List<String> command =
        new ImmutableList.Builder<String>()
            .add("config", "list")
            .addAll(GcloudArgs.get("format", "json"))
            .build();

    String configJson = runCommand(command);
    return CloudSdkConfig.fromJson(configJson);
  }

  /**
   * Run short lived gcloud commands.
   *
   * @param args the arguments to gcloud command (not including 'gcloud')
   * @return standard out collected as a single string
   */
  public String runCommand(List<String> args)
      throws CloudSdkNotFoundException, IOException, ProcessHandlerException {
    sdk.validateCloudSdkLocation();

    StringBuilderProcessOutputLineListener stdOutListener =
        StringBuilderProcessOutputLineListener.newListener();
    StringBuilderProcessOutputLineListener stdErrListener =
        StringBuilderProcessOutputLineListener.newListenerWithNewlines();
    ExitCodeRecorderProcessExitListener exitListener = new ExitCodeRecorderProcessExitListener();

    // build and run the command
    List<String> command =
        new ImmutableList.Builder<String>()
            .add(sdk.getGCloudPath().toAbsolutePath().toString())
            .addAll(args)
            .build();

    Process process = new ProcessBuilder(command).start();
    LegacyProcessHandler.builder()
        .addStdOutLineListener(stdOutListener)
        .addStdErrLineListener(stdErrListener)
        .setExitListener(exitListener)
        .build()
        .handleProcess(process);

    if (exitListener.getMostRecentExitCode() != null
        && !exitListener.getMostRecentExitCode().equals(0)) {
      Logger.getLogger(Gcloud.class.getName()).severe(stdErrListener.toString());
      throw new ProcessHandlerException(
          "Process exited unsuccessfully with code " + exitListener.getMostRecentExitCode());
    }

    return stdOutListener.toString();
  }

  @VisibleForTesting
  GcloudRunner getRunner(ProcessHandler processHandler) {
    return gcloudRunnerFactory.newRunner(
        sdk,
        metricsEnvironment,
        metricsEnvironmentVersion,
        credentialFile,
        flagsFiles, // this is the only consumer of flagsFiles
        outputFormat,
        showStructuredLogs,
        verbosity,
        processHandler);
  }

  public static Builder builder(CloudSdk sdk) {
    return new Builder(sdk);
  }

  public static class Builder {

    private final CloudSdk sdk;
    private final GcloudRunner.Factory gcloudRunnerFactory;

    @Nullable private String metricsEnvironment;
    @Nullable private String metricsEnvironmentVersion;
    @Nullable private Path credentialFile;
    @Nullable private List<Path> flagsFiles;
    @Nullable private String outputFormat;
    @Nullable private String showStructuredLogs;
    @Nullable private String verbosity;

    private Builder(CloudSdk sdk) {
      this(sdk, new GcloudRunner.Factory());
    }

    @VisibleForTesting
    Builder(CloudSdk sdk, GcloudRunner.Factory gcloudRunnerFactory) {
      this.sdk = sdk;
      this.gcloudRunnerFactory = gcloudRunnerFactory;
    }

    /** Set metrics environment and version. */
    public Builder setMetricsEnvironment(
        String metricsEnvironment, String metricsEnvironmentVersion) {
      this.metricsEnvironment = metricsEnvironment;
      this.metricsEnvironmentVersion = metricsEnvironmentVersion;
      return this;
    }

    /**
     * Sets the format for printing command output resources. The default is a command-specific
     * human-friendly output format. The supported formats are: csv, default, flattened, JSON, list,
     * multi, none, table, text, value, yaml. For more details run $ gcloud topic formats.
     */
    public Builder setOutputFormat(String outputFormat) {
      this.outputFormat = outputFormat;
      return this;
    }

    /** Set the credential file override. */
    public Builder setCredentialFile(Path credentialFile) {
      this.credentialFile = credentialFile;
      return this;
    }

    /** Allow use of flags file for gcloud commands. */
    public Builder setFlagsFiles(List<Path> flagsFiles) {
      this.flagsFiles = ImmutableList.copyOf(flagsFiles);
      return this;
    }

    /**
     * Sets structured JSON logs for the stderr output. Supported values include 'never' (default),
     * 'always', 'terminal', etc.
     */
    public Builder setShowStructuredLogs(String showStructuredLogs) {
      this.showStructuredLogs = showStructuredLogs;
      return this;
    }

    /**
     * Sets the verbosity of output for gcloud. @see <a
     * href="https://cloud.google.com/sdk/gcloud/reference#--verbosity">gcloud docs</a>
     */
    public Builder setVerbosity(String verbosity) {
      this.verbosity = verbosity;
      return this;
    }

    /** Build an immutable Gcloud instance. */
    public Gcloud build() {
      return new Gcloud(
          sdk,
          gcloudRunnerFactory,
          metricsEnvironment,
          metricsEnvironmentVersion,
          credentialFile,
          flagsFiles,
          outputFormat,
          showStructuredLogs,
          verbosity);
    }
  }
}
