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
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.base.Preconditions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineDeployment}.
 */
public class CloudSdkAppEngineDeployment implements AppEngineDeployment {

  private static final List<String> yamlFilenames = Arrays
      .asList("app.yaml", "cron.yaml", "queue.yaml", "dispatch.yaml", "index.yaml", "dos.yaml");

  private CloudSdk sdk;

  public CloudSdkAppEngineDeployment(
      CloudSdk sdk) {
    this.sdk = sdk;
  }

  @Override
  public void deploy(DeployConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDeployables());
    Preconditions.checkArgument(config.getDeployables().size() > 0);
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");

    // If we get a single directory, then assume it is a deployable directory
    // and look in it for yaml files to deploy
    if (config.getDeployables().size() == 1 && config.getDeployables().get(0).isDirectory()) {
      Path deployableDirectory = config.getDeployables().get(0).toPath();
      for (String filename : yamlFilenames) {
        Path yamlFile = deployableDirectory.resolve(filename);
        if (Files.isRegularFile(yamlFile)) {
          arguments.add(yamlFile.toString());
        }
      }
    } else {
      // otherwise deployables are the yamls the user provides
      for (File deployable : config.getDeployables()) {
        if (!deployable.exists()) {
          throw new IllegalArgumentException(
              "Deployable " + deployable.toPath().toString() + " does not exist.");
        }
        arguments.add(deployable.toPath().toString());
      }
    }

    arguments.addAll(GcloudArgs.get("bucket", config.getBucket()));
    arguments.addAll(GcloudArgs.get("docker-build", config.getDockerBuild()));
    arguments.addAll(GcloudArgs.get("force", config.getForce()));
    arguments.addAll(GcloudArgs.get("image-url", config.getImageUrl()));
    arguments.addAll(GcloudArgs.get("promote", config.getPromote()));
    arguments.addAll(GcloudArgs.get("server", config.getServer()));
    arguments.addAll(GcloudArgs.get("stop-previous-version", config.getStopPreviousVersion()));
    arguments.addAll(GcloudArgs.get("version", config.getVersion()));
    arguments.addAll(GcloudArgs.get(config));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

}
