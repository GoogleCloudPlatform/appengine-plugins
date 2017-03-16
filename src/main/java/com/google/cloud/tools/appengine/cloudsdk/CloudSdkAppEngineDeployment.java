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
import com.google.cloud.tools.appengine.api.deploy.DeployCronConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployDispatchConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployDosConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployIndexesConfiguration;
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
    Preconditions.checkArgument(config.getCronYaml().getName().equals("cron.yaml"),
        "Expecting cron.yaml");

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    arguments.add(config.getCronYaml().toPath().toString());
    arguments.addAll(GcloudArgs.get(config));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  @Override
  public void deployDos(DeployDosConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDosYaml());
    Preconditions.checkArgument(config.getDosYaml().getName().equals("dos.yaml"),
        "Expecting dos.yaml");

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    arguments.add(config.getDosYaml().toPath().toString());
    arguments.addAll(GcloudArgs.get(config));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  @Override
  public void deployDispatch(DeployDispatchConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDispatchYaml());
    Preconditions.checkArgument(config.getDispatchYaml().getName().equals("dispatch.yaml"),
        "Expecting dispatch.yaml");

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    arguments.add(config.getDispatchYaml().toPath().toString());
    arguments.addAll(GcloudArgs.get(config));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  @Override
  public void deployIndexes(DeployIndexesConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getIndexesYaml());
    Preconditions.checkArgument(config.getIndexesYaml().getName().equals("indexes.yaml"),
        "Expecting indexes.yaml");

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    arguments.add(config.getIndexesYaml().toPath().toString());
    arguments.addAll(GcloudArgs.get(config));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  @Override
  public void deployQueue(DeployQueueConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getQueueYaml());
    Preconditions.checkArgument(config.getQueueYaml().getName().equals("queue.yaml"),
        "Expecting queue.yaml");

    List<String> arguments = new ArrayList<>();
    arguments.add("deploy");
    arguments.add(config.getQueueYaml().toPath().toString());
    arguments.addAll(GcloudArgs.get(config));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

}
