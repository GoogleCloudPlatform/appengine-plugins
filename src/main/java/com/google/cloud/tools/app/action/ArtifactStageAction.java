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
package com.google.cloud.tools.app.action;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.google.cloud.tools.app.config.ArtifactStageConfiguration;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * Stages a Java JAR/WAR Managed VMs application to be deployed.
 */
public class ArtifactStageAction implements Action {

  private static Logger logger = Logger.getLogger(ArtifactStageAction.class.getName());
  private ArtifactStageConfiguration configuration;

  public ArtifactStageAction(ArtifactStageConfiguration configuration) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getStagingDirectory());
    Preconditions.checkNotNull(configuration.getArtifact());

    this.configuration = configuration;
  }

  /**
   * Copies app.yaml, Dockerfile and the application artifact to the staging area.
   *
   * <p>If app.yaml or Dockerfile do not exist, gcloud deploy will create them.
   */
  @Override
  public int execute() throws IOException {
    if (!configuration.getStagingDirectory().exists()) {
      logger.severe("Staging directory does not exist. Location: "
          + configuration.getStagingDirectory().toPath().toString());
      return 1;
    }
    if (!configuration.getStagingDirectory().isDirectory()) {
      logger.severe("Staging location is not a directory. Location: "
          + configuration.getStagingDirectory().toPath().toString());
      return 1;
    }

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

    return 0;
  }
}
