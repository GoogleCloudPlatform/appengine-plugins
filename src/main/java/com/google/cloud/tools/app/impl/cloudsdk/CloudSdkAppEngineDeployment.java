/**
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.appengine.repackaged.com.google.api.client.util.Strings;
import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.app.api.deploy.DeployConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineDeployment}.
 */
public class CloudSdkAppEngineDeployment implements AppEngineDeployment {

  private CloudSdk sdk;

  public CloudSdkAppEngineDeployment(
      CloudSdk sdk) {
    this.sdk = sdk;
  }

  @Override
  public void deploy(DeployConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getDeployables());
    Preconditions.checkArgument(configuration.getDeployables().size() > 0);
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    for (File deployable : configuration.getDeployables()) {
      if (!deployable.exists()) {
        throw new IllegalArgumentException(
            "Deployable " + deployable.toPath().toString() + " does not exist.");
      }
      arguments.add(deployable.toPath().toString());
    }

    if (!Strings.isNullOrEmpty(configuration.getBucket())) {
      arguments.add("--bucket");
      arguments.add(configuration.getBucket());
    }

    if (!Strings.isNullOrEmpty(configuration.getDockerBuild())) {
      arguments.add("--docker-build");
      arguments.add(configuration.getDockerBuild());
    }

    if (configuration.isForce()) {
      arguments.add("--force");
    }

    if (!Strings.isNullOrEmpty(configuration.getImageUrl())) {
      arguments.add("--image-url");
      arguments.add(configuration.getImageUrl());
    }

    if (!Strings.isNullOrEmpty(configuration.getServer())) {
      arguments.add("--server");
      arguments.add(configuration.getServer());
    }

    if (configuration.isStopPreviousVersion()) {
      arguments.add("--stop-previous-version");
    }

    if (!Strings.isNullOrEmpty(configuration.getVersion())) {
      arguments.add("--version");
      arguments.add(configuration.getVersion());
    }

    arguments.add("--quiet");

    try {
      int result = sdk.runAppCommand(arguments);
      if (result != 0) {
        throw new AppEngineException("Deployment failed with error code: " + result);
      }
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }

  }

}
