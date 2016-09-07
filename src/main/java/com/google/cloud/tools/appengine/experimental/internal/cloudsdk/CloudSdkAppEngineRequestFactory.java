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

import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.experimental.AppEngineRequestFactory;
import com.google.cloud.tools.appengine.experimental.deploy.DeployResult;
import com.google.cloud.tools.appengine.experimental.internal.cloudsdk.deploy.DeployResultConverter;
import com.google.cloud.tools.appengine.experimental.internal.cloudsdk.deploy.DeployTranslator;
import com.google.cloud.tools.appengine.experimental.internal.process.CliProcessManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Cloud Sdk implementation of the {@link AppEngineRequestFactory}.
 */
public class CloudSdkAppEngineRequestFactory implements AppEngineRequestFactory {
  private final CloudSdkV2 sdk;
  private final String metricsEnvironment;
  private final String metricsEnvironmentVersion;
  private final Path credentialFile;

  /**
   * Configure a new Cloud Sdk based request factory.
   * @param cloudSdkHome Path to the Google Cloud Sdk
   * @param credentialFile Path to a credential override file
   * @param metricsEnvironment The tool using this library to call gcloud
   * @param metricsEnvironmentVersion The tool version
   */
  public CloudSdkAppEngineRequestFactory(Path cloudSdkHome, Path credentialFile,
      String metricsEnvironment, String metricsEnvironmentVersion) {
    this(new CloudSdkV2(cloudSdkHome), credentialFile, metricsEnvironment,
        metricsEnvironmentVersion);
  }

  CloudSdkAppEngineRequestFactory(CloudSdkV2 sdk, Path credentialFile,
      String metricsEnvironment, String metricsEnvironmentVersion) {
    this.sdk = sdk;
    this.metricsEnvironment = metricsEnvironment;
    this.metricsEnvironmentVersion = metricsEnvironmentVersion;
    this.credentialFile = credentialFile;
  }

  @VisibleForTesting
  protected Map<String, String> getEnvironment() {
    Map<String, String> environment = Maps.newHashMap();
    // see crendential-file-override in gcloud arguments
    if (credentialFile != null) {
      environment.put("CLOUDSDK_APP_USE_GSUTIL", "0");
    }
    if (metricsEnvironment != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT", metricsEnvironment);
    }
    if (metricsEnvironmentVersion != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT_VERSION", metricsEnvironmentVersion);
    }
    return environment;
  }

  @VisibleForTesting
  protected List<String> getAppCommand(List<String> params) {
    List<String> command = Lists.newArrayList();
    command.add(sdk.getGCloudPath().toString());
    command.add("app");
    command.addAll(params);
    command.add("--format=yaml");
    command.add("--quiet");
    if (credentialFile != null) {
      command.addAll(GcloudArgs.get("credential-file-override", credentialFile.toFile()));
    }
    return command;
  }

  @Override
  public CloudSdkRequest<DeployResult> newDeploymentRequest(
      DeployConfiguration deployConfiguration) {

    return new CloudSdkRequest<>(
        new CloudSdkProcessFactory(
            getAppCommand(new DeployTranslator().translate(deployConfiguration)), getEnvironment()),
        new CliProcessManager.Provider<DeployResult>(),
        new DeployResultConverter());
  }
}
