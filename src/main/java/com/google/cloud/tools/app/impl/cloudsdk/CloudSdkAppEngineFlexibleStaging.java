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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.app.api.deploy.StageFlexibleConfiguration;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Cloud SDK based implementation of {@link AppEngineFlexibleStaging}.
 */
public class CloudSdkAppEngineFlexibleStaging implements AppEngineFlexibleStaging {

  /**
   * Stages a Java JAR/WAR Managed VMs application to be deployed.
   *
   * Copies app.yaml, Dockerfile and the application artifact to the staging area.
   *
   * <p>If app.yaml or Dockerfile do not exist, gcloud cloud will create them.
   */
  @Override
  public void stageFlexible(StageFlexibleConfiguration configuration) throws AppEngineException {
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
}
