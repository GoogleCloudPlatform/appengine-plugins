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

package com.google.cloud.tools.app.api.deploy;

import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessExitListener;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessOutputLineListener;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessStartListener;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.config.DefaultDeployConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Future;

public class DeploymentBuilder {
  private DefaultDeployConfiguration config = new DefaultDeployConfiguration();
  private CloudSdk.Builder cloudSdkBuilder = new CloudSdk.Builder();

  public DeploymentBuilder deployables(File... deployables) {
    config.setDeployables(Arrays.asList(deployables));
    return this;
  }

  public DeploymentBuilder bucket(String bucket) {
    config.setBucket(bucket);
    return this;
  }

  public DeploymentBuilder dockerBuild(String dockerBuild) {
    config.setDockerBuild(dockerBuild);
    return this;
  }

  public DeploymentBuilder force(Boolean force) {
    config.setForce(force);
    return this;
  }

  public DeploymentBuilder imageUrl(String imageUrl) {
    config.setImageUrl(imageUrl);
    return this;
  }

  public DeploymentBuilder project(String project) {
    config.setProject(project);
    return this;
  }

  public DeploymentBuilder promote(Boolean promote) {
    config.setPromote(promote);
    return this;
  }

  public DeploymentBuilder server(String server) {
    config.setServer(server);
    return this;
  }

  public DeploymentBuilder stopPreviousVersion(Boolean stopPreviousVersion) {
    config.setStopPreviousVersion(stopPreviousVersion);
    return this;
  }

  public DeploymentBuilder version(String version) {
    config.setVersion(version);
    return this;
  }

  /**
   * The home directory of Google Cloud SDK. If not set, will attempt to look for the SDK in known
   * install locations.
   */
  public DeploymentBuilder sdkPath(File sdkPathFile) {
    cloudSdkBuilder.sdkPath(sdkPathFile);
    return this;
  }

  /**
   * The metrics environment.
   */
  public DeploymentBuilder appCommandMetricsEnvironment(String appCommandMetricsEnvironment) {
    cloudSdkBuilder.appCommandMetricsEnvironment(appCommandMetricsEnvironment);
    return this;
  }

  /**
   * The metrics environment version.
   */
  public DeploymentBuilder appCommandMetricsEnvironmentVersion(String appCommandMetricsEnvironmentVersion) {
    cloudSdkBuilder.appCommandMetricsEnvironmentVersion(appCommandMetricsEnvironmentVersion);
    return this;
  }

  /**
   * Configures usage of gsutil.
   */
  public DeploymentBuilder appCommandGsUtil(Integer appCommandGsUtil) {
    cloudSdkBuilder.appCommandGsUtil(appCommandGsUtil);
    return this;
  }

  /**
   * Sets the path the credential override file.
   */
  public DeploymentBuilder appCommandCredentialFile(File appCommandCredentialFile) {
    cloudSdkBuilder.appCommandCredentialFile(appCommandCredentialFile);
    return this;
  }

  /**
   * Sets the format for printing command output resources. The default is a command-specific
   * human-friendly output format. The supported formats are: csv, default, flattened, json, list,
   * multi, none, table, text, value, yaml. For more details run $ gcloud topic formats.
   */
  public DeploymentBuilder appCommandOutputFormat(String appCommandOutputFormat) {
    cloudSdkBuilder.appCommandOutputFormat(appCommandOutputFormat);
    return this;
  }

  /**
   * Adds a client consumer of process error output. If none, output will be inherited by parent
   * process.
   */
  public DeploymentBuilder statusLineListener(ProcessOutputLineListener... statusLineListener) {
    for (ProcessOutputLineListener listener : statusLineListener) {
      cloudSdkBuilder.addStdErrLineListener(listener);
    }
    return this;
  }

  /**
   * The client listener of the process exit with code.
   */
  public DeploymentBuilder exitListener(ProcessExitListener exitListener) {
    cloudSdkBuilder.exitListener(exitListener);
    return this;
  }

  /**
   * The client listener of the process start. Allows access to the underlying process.
   */
  public DeploymentBuilder startListener(ProcessStartListener startListener) {
    cloudSdkBuilder.startListener(startListener);
    return this;
  }

  public Deployment build() {
    return new Deployment() {
      @Override
      public Future<DeploymentResult> deploy() {
        CloudSdk sdk = cloudSdkBuilder.build();
        AppEngineDeployment deployment = new CloudSdkAppEngineDeployment(sdk);
        deployment.deploy(config);
        return null;
      }
    };
  }
}
