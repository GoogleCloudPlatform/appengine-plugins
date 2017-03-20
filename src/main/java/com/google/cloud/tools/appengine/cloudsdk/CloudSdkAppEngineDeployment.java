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
import com.google.cloud.tools.appengine.api.Configuration;
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployCronConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployDispatchConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployDosConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployIndexConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployQueueConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineDeployment}.
 */
public class CloudSdkAppEngineDeployment implements AppEngineDeployment {

  private final CloudSdk sdk;

  public CloudSdkAppEngineDeployment(CloudSdk sdk) {
    this.sdk = Preconditions.checkNotNull(sdk);
  }

  /**
   * Deploys a project to App Engine.
   *
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old 
   * @throws AppEngineException when there is an issue uploading project files to the cloud
   * @throws IllegalArgumentException when a local deployable referenced 
   *     by the configuration isn't found
   */  
  @Override
  public void deploy(DeployConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDeployables());
    Preconditions.checkArgument(config.getDeployables().size() > 0);
    File workingDirectory = null;

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");

    // Unfortunately, 'gcloud app deploy' does not let you pass a staging directory as a deployable.
    // Instead, we have to run 'gcloud app deploy' from the staging directory to achieve this.
    // So, if we find that the only deployable in the list is a directory, we just run the command
    // from that directory without passing in any deployables to gcloud.
    if (config.getDeployables().size() == 1 && config.getDeployables().get(0).isDirectory()) {
      workingDirectory = config.getDeployables().get(0);
    } else {
      for (File deployable : config.getDeployables()) {
        if (!deployable.exists()) {
          throw new IllegalArgumentException(
              "Deployable " + deployable.toPath() + " does not exist.");
        }
        arguments.add(deployable.toPath().toString());
      }
    }

    arguments.addAll(GcloudArgs.get("bucket", config.getBucket()));
    arguments.addAll(GcloudArgs.get("image-url", config.getImageUrl()));
    arguments.addAll(GcloudArgs.get("promote", config.getPromote()));
    arguments.addAll(GcloudArgs.get("server", config.getServer()));
    arguments.addAll(GcloudArgs.get("stop-previous-version", config.getStopPreviousVersion()));
    arguments.addAll(GcloudArgs.get("version", config.getVersion()));
    arguments.addAll(GcloudArgs.get(config));

    try {
      if (workingDirectory != null) {
        sdk.runAppCommandInWorkingDirectory(arguments, workingDirectory);
      } else {
        sdk.runAppCommand(arguments);
      }
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }

  }

  @Override
  public void deployCron(DeployCronConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getCronYaml());

    deployConfig(config.getCronYaml(), "cron.yaml", config);
  }

  @Override
  public void deployDos(DeployDosConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDosYaml());
    deployConfig(config.getDosYaml(), "dos.yaml", config);
  }

  @Override
  public void deployDispatch(DeployDispatchConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDispatchYaml());
    deployConfig(config.getDispatchYaml(), "dispatch.yaml", config);
  }

  @Override
  public void deployIndex(DeployIndexConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getIndexYaml());

    deployConfig(config.getIndexYaml(), "index.yaml", config);
  }

  @Override
  public void deployQueue(DeployQueueConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getQueueYaml());

    deployConfig(config.getQueueYaml(), "queue.yaml", config);
  }

  /**
   * Common configuration deployment function.
   *
   * @param yamlToDeploy Yaml file that we want to deploy
   * @param expectedName Expected filename for error checking
   * @param baseConfig {@link Configuration} to obtain common gcloud parameters
   */
  private void deployConfig(File yamlToDeploy, String expectedName, Configuration baseConfig) {
    Preconditions.checkArgument(yamlToDeploy.getName().equals(expectedName),
            "Invalid deployable: " + yamlToDeploy.getName() + ", expecting: " + expectedName);

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    arguments.add(yamlToDeploy.getAbsolutePath());
    arguments.addAll(GcloudArgs.get(baseConfig));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }

  }

}
