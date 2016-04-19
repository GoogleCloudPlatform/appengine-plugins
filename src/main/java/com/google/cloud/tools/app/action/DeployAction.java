/**
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
package com.google.cloud.tools.app.action;

import com.google.appengine.repackaged.com.google.api.client.util.Strings;
import com.google.cloud.tools.app.config.DeployConfiguration;
import com.google.cloud.tools.app.executor.AppExecutor;
import com.google.cloud.tools.app.executor.ExecutorException;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Deploys an app to the server.
 */
public class DeployAction implements Action {

  private DeployConfiguration configuration;
  private AppExecutor sdkExec;

  public DeployAction(DeployConfiguration configuration, AppExecutor sdkExec) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getDeployables());
    Preconditions.checkArgument(configuration.getDeployables().size() > 0);
    Preconditions.checkNotNull(sdkExec);

    this.configuration = configuration;
    this.sdkExec = sdkExec;
  }

  @Override
  public int execute() throws ExecutorException {
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

    return sdkExec.runApp(arguments);
  }
}
