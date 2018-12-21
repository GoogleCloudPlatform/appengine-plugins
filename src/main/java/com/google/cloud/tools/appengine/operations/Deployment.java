/*
 * Copyright 2016 Google LLC.
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

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.DeployConfiguration;
import com.google.cloud.tools.appengine.configuration.DeployProjectConfigurationConfiguration;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Deploy staged application and project configuration. */
public class Deployment {

  private final GcloudRunner runner;

  Deployment(GcloudRunner runner) {
    this.runner = Preconditions.checkNotNull(runner);
  }

  /**
   * Deploys a project to App Engine.
   *
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   * @throws AppEngineException when there is an issue uploading project files to the cloud
   * @throws IllegalArgumentException when a local deployable referenced by the configuration isn't
   *     found
   */
  public void deploy(DeployConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getDeployables());
    Preconditions.checkArgument(config.getDeployables().size() > 0);
    Path workingDirectory = null;

    List<String> arguments = new ArrayList<>();
    arguments.add("app");
    arguments.add("deploy");

    // Unfortunately, 'gcloud app deploy' does not let you pass a staging directory as a deployable.
    // Instead, we have to run 'gcloud app deploy' from the staging directory to achieve this.
    // So, if we find that the only deployable in the list is a directory, we just run the command
    // from that directory without passing in any deployables to gcloud.
    if (config.getDeployables().size() == 1 && Files.isDirectory(config.getDeployables().get(0))) {
      workingDirectory = config.getDeployables().get(0);
    } else {
      for (Path deployable : config.getDeployables()) {
        if (!Files.exists(deployable)) {
          throw new IllegalArgumentException("Deployable " + deployable + " does not exist.");
        }
        arguments.add(deployable.toString());
      }
    }

    arguments.addAll(GcloudArgs.get("bucket", config.getBucket()));
    arguments.addAll(GcloudArgs.get("image-url", config.getImageUrl()));
    arguments.addAll(GcloudArgs.get("promote", config.getPromote()));
    arguments.addAll(GcloudArgs.get("server", config.getServer()));
    arguments.addAll(GcloudArgs.get("stop-previous-version", config.getStopPreviousVersion()));
    arguments.addAll(GcloudArgs.get("version", config.getVersion()));
    arguments.addAll(GcloudArgs.get("project", config.getProjectId()));

    try {
      runner.run(arguments, workingDirectory);
    } catch (ProcessHandlerException | IOException ex) {
      throw new AppEngineException(ex);
    }
  }

  /** Deploy cron.yaml to App Engine. */
  public void deployCron(DeployProjectConfigurationConfiguration config) throws AppEngineException {
    deployConfig("cron.yaml", config);
  }

  /** Deploy dos.yaml to App Engine. */
  public void deployDos(DeployProjectConfigurationConfiguration config) throws AppEngineException {
    deployConfig("dos.yaml", config);
  }

  /** Deploy dispatch.yaml to App Engine. */
  public void deployDispatch(DeployProjectConfigurationConfiguration config)
      throws AppEngineException {
    deployConfig("dispatch.yaml", config);
  }

  /** Deploy index.yaml to App Engine. */
  public void deployIndex(DeployProjectConfigurationConfiguration config)
      throws AppEngineException {
    deployConfig("index.yaml", config);
  }

  /** Deploy queue.yaml to App Engine. */
  public void deployQueue(DeployProjectConfigurationConfiguration config)
      throws AppEngineException {
    deployConfig("queue.yaml", config);
  }

  /**
   * Common configuration deployment function.
   *
   * @param filename Yaml file that we want to deploy (cron.yaml, dos.yaml, etc)
   * @param configuration Deployment configuration
   */
  @VisibleForTesting
  void deployConfig(String filename, DeployProjectConfigurationConfiguration configuration)
      throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getAppEngineDirectory());

    Path deployable = configuration.getAppEngineDirectory().resolve(filename);
    Preconditions.checkArgument(
        Files.isRegularFile(deployable), deployable.toString() + " does not exist.");

    List<String> arguments = new ArrayList<>();
    arguments.add("app");
    arguments.add("deploy");
    arguments.add(deployable.toAbsolutePath().toString());
    arguments.addAll(GcloudArgs.get("server", configuration.getServer()));
    arguments.addAll(GcloudArgs.get("project", configuration.getProjectId()));

    try {
      runner.run(arguments, null);
    } catch (ProcessHandlerException | IOException ex) {
      throw new AppEngineException(ex);
    }
  }
}
