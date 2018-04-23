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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;

/** Operations that use gcloud. */
public class Gcloud {
  private final CloudSdk sdk;
  private final GcloudRunner.Factory gcloudRunnerFactory;
  private final String metricsEnvironment;
  private final String metricsEnvironmentVersion;
  private final File credentialFile;
  private final String outputFormat;
  private final String showStructuredLogs;

  private Gcloud(
      CloudSdk sdk,
      GcloudRunner.Factory gcloudRunnerFactory,
      String metricsEnvironment,
      String metricsEnvironmentVersion,
      File credentialFile,
      String outputFormat,
      String showStructuredLogs) {
    this.gcloudRunnerFactory = gcloudRunnerFactory;
    this.sdk = sdk;
    this.metricsEnvironment = metricsEnvironment;
    this.metricsEnvironmentVersion = metricsEnvironmentVersion;
    this.credentialFile = credentialFile;
    this.outputFormat = outputFormat;
    this.showStructuredLogs = showStructuredLogs;
  }

  public CloudSdkAppEngineDeployment newDeployment(ProcessHandler processHandler) {
    return new CloudSdkAppEngineDeployment(getRunner(processHandler));
  }

  public CloudSdkAppEngineVersions newVersions(ProcessHandler processHandler) {
    return new CloudSdkAppEngineVersions(getRunner(processHandler));
  }

  public CloudSdkAuth newAuth(ProcessHandler processHandler) {
    return new CloudSdkAuth(getRunner(processHandler));
  }

  public CloudSdkGenRepoInfoFile newGenRepoInfo(ProcessHandler processHandler) {
    return new CloudSdkGenRepoInfoFile(getRunner(processHandler));
  }

  @VisibleForTesting
  GcloudRunner getRunner(ProcessHandler processHandler) {
    return gcloudRunnerFactory.newRunner(
        sdk,
        metricsEnvironment,
        metricsEnvironmentVersion,
        credentialFile,
        outputFormat,
        showStructuredLogs,
        processHandler);
  }

  public static Builder builder(CloudSdk sdk) {
    return new Builder(sdk);
  }

  public static class Builder {

    private final CloudSdk sdk;
    private final GcloudRunner.Factory gcloudRunnerFactory;

    private String metricsEnvironment;
    private String metricsEnvironmentVersion;
    private File credentialFile;
    private String outputFormat;
    private String showStructuredLogs;

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
    public Builder setCredentialFile(File credentialFile) {
      this.credentialFile = credentialFile;
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

    /** Build an immutable Gcloud instance. */
    public Gcloud build() {
      return new Gcloud(
          sdk,
          gcloudRunnerFactory,
          metricsEnvironment,
          metricsEnvironmentVersion,
          credentialFile,
          outputFormat,
          showStructuredLogs);
    }
  }
}
