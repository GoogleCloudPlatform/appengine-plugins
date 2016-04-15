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
package com.google.cloud.tools.app.deploy.sdk;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.google.appengine.repackaged.com.google.api.client.util.Strings;
import com.google.cloud.tools.app.deploy.AppEngineDeployment;
import com.google.cloud.tools.app.deploy.ArtifactStageConfiguration;
import com.google.cloud.tools.app.deploy.DeployConfiguration;
import com.google.cloud.tools.app.deploy.GenConfigConfiguration;
import com.google.cloud.tools.app.deploy.StageConfiguration;
import com.google.cloud.tools.app.executor.AppExecutor;
import com.google.cloud.tools.app.executor.ExecutorException;
import com.google.cloud.tools.app.executor.StageExecutor;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by meltsufin on 4/15/16.
 */
public class CloudSdkAppEngineDeployment implements AppEngineDeployment {

  private AppExecutor sdkExec;

  private StageExecutor stageExecutor;

  public CloudSdkAppEngineDeployment(AppExecutor sdkExec,
      StageExecutor stageExecutor) {
    this.sdkExec = sdkExec;
    this.stageExecutor = stageExecutor;
  }

  @Override
  public void deploy(DeployConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getDeployables());
    Preconditions.checkArgument(configuration.getDeployables().size() > 0);
    Preconditions.checkNotNull(sdkExec);

    List<String> arguments = new ArrayList<>();
    arguments.add("cloud");
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
      sdkExec.runApp(arguments);
    } catch (ExecutorException e) {
      throw new AppEngineException(e);
    }

  }


  /**
   * Stages a Java JAR/WAR Managed VMs application to be deployed.
   *
   * Copies app.yaml, Dockerfile and the application artifact to the staging area.
   *
   * <p>If app.yaml or Dockerfile do not exist, gcloud cloud will create them.
   */
  @Override
  public void stageForStandard(ArtifactStageConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getStagingDirectory());
    Preconditions.checkNotNull(configuration.getArtifact());

    if (!configuration.getStagingDirectory().exists()) {
      throw new AppEngineException("Staging directory does not exist. Location: "
          + configuration.getStagingDirectory().toPath().toString());
    }
    if (!configuration.getStagingDirectory().isDirectory()) {
      throw new AppEngineException("Staging location is not a directory. Location: "
          + configuration.getStagingDirectory().toPath().toString());
    }

    try {

      // Copy app.yaml to staging.
      if (configuration.getAppYaml() != null && configuration.getAppYaml().exists()) {
        Files.copy(configuration.getAppYaml().toPath(),
            configuration.getStagingDirectory().toPath()
                .resolve(configuration.getAppYaml().toPath().getFileName()),
            REPLACE_EXISTING);
      }

      // Copy Dockerfile to staging.
      if (configuration.getDockerfile() != null && configuration.getDockerfile().exists()) {
        Files.copy(configuration.getDockerfile().toPath(),
            configuration.getStagingDirectory().toPath()
                .resolve(configuration.getDockerfile().toPath().getFileName()),
            REPLACE_EXISTING);
      }

      // TODO : looks like this section should error on no artifacts found? and maybe the
      // TODO : earlier ones should warn?
      // Copy the JAR/WAR file to staging.
      if (configuration.getArtifact() != null && configuration.getArtifact().exists()) {
        Files.copy(configuration.getArtifact().toPath(),
            configuration.getStagingDirectory().toPath()
                .resolve(configuration.getArtifact().toPath().getFileName()),
            REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new AppEngineException(e);
    }
  }

  /**
   * Stages an application to be deployed.
   */
  @Override
  public void stageForFlexible(StageConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getSourceDirectory());
    Preconditions.checkNotNull(configuration.getStagingDirectory());
    Preconditions.checkNotNull(stageExecutor);

    List<String> arguments = new ArrayList<>();
    arguments.add(configuration.getSourceDirectory().toPath().toString());
    arguments.add(configuration.getStagingDirectory().toPath().toString());
    if (configuration.isEnableQuickstart()) {
      arguments.add("--enable_quickstart");
    }
    if (configuration.isDisableUpdateCheck()) {
      arguments.add("--disable_update_check");
    }
    if (!Strings.isNullOrEmpty(configuration.getVersion())) {
      arguments.add("--version");
      arguments.add(configuration.getVersion());
    }
    if (!Strings.isNullOrEmpty(configuration.getApplicationId())) {
      arguments.add("-A");
      arguments.add(configuration.getApplicationId());
    }
    if (configuration.isEnableJarSplitting()) {
      arguments.add("--enable_jar_splitting");
    }
    if (!Strings.isNullOrEmpty(configuration.getJarSplittingExcludes())) {
      arguments.add("--jar_splitting_excludes");
      arguments.add(configuration.getJarSplittingExcludes());
    }
    if (!Strings.isNullOrEmpty(configuration.getCompileEncoding())) {
      arguments.add("--compile_encoding");
      arguments.add(configuration.getCompileEncoding());
    }
    if (configuration.isDeleteJsps()) {
      arguments.add("--delete_jsps");
    }
    if (configuration.isEnableJarClasses()) {
      arguments.add("--enable_jar_classes");
    }

    Path dockerfilePath =
        configuration.getDockerfile() == null ? null : configuration.getDockerfile().toPath();

    try {
      stageExecutor
          .runStage(arguments, dockerfilePath, configuration.getStagingDirectory().toPath());
    } catch (IOException e) {
      throw new AppEngineException(e);
    }

  }

  /**
   * Generates missing configuration files.
   */
  @Override
  public void genConfig(GenConfigConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(sdkExec);

    if (!configuration.getSourceDirectory().exists()) {
      throw new AppEngineException("Source directory does not exist. Location: "
          + configuration.getSourceDirectory().toPath().toString());
    }
    if (!configuration.getSourceDirectory().isDirectory()) {
      throw new AppEngineException("Source location is not a directory. Location: "
          + configuration.getSourceDirectory().toPath().toString());
    }

    List<String> arguments = new ArrayList<>();
    arguments.add("gen-config");

    if (configuration.getSourceDirectory() != null) {
      arguments.add(configuration.getSourceDirectory().toPath().toString());
    }
    if (!isNullOrEmpty(configuration.getConfig())) {
      arguments.add("--config");
      arguments.add(configuration.getConfig());
    }
    if (configuration.isCustom()) {
      arguments.add("--custom");
    }
    if (!isNullOrEmpty(configuration.getRuntime())) {
      arguments.add("--runtime");
      arguments.add(configuration.getRuntime());
    }

    try {
      sdkExec.runApp(arguments);
    } catch (ExecutorException e) {
      throw new AppEngineException(e);
    }
  }

}
