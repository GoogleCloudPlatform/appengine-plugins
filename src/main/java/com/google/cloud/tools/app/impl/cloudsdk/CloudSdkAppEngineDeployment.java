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

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.app.api.deploy.DeployConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineDeployment}.
 */
public class CloudSdkAppEngineDeployment implements AppEngineDeployment {

  private CloudSdkCli sdkCli;

  public CloudSdkAppEngineDeployment(CloudSdkCli sdkCli) {
    this.sdkCli = sdkCli;
  }

  @Override
  public void deploy(DeployConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDeployables());
    Preconditions.checkArgument(config.getDeployables().size() > 0);
    Preconditions.checkNotNull(sdkCli);

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    for (File deployable : config.getDeployables()) {
      if (!deployable.exists()) {
        throw new IllegalArgumentException(
            "Deployable " + deployable.toPath().toString() + " does not exist.");
      }
      arguments.add(deployable.toPath().toString());
    }

    arguments.addAll(Args.string("bucket", config.getBucket()));
    arguments.addAll(Args.string("docker-build", config.getDockerBuild()));
    arguments.addAll(Args.boolWithNo("force", config.getForce()));
    arguments.addAll(Args.string("image-url", config.getImageUrl()));
    arguments.addAll(Args.string("project", config.getProject()));
    arguments.addAll(Args.boolWithNo("promote", config.getPromote()));
    arguments.addAll(Args.string("server", config.getServer()));
    arguments
        .addAll(Args.boolWithNo("stop-previous-version", config.getStopPreviousVersion()));
    arguments.addAll(Args.string("version", config.getVersion()));

    try {
      sdkCli.runGcloudAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }

  }

}
