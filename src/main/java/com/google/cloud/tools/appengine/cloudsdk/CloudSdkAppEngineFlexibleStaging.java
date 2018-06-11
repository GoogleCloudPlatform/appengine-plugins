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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.StageFlexibleConfiguration;
import com.google.cloud.tools.io.FileUtil;
import com.google.cloud.tools.project.AppYaml;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Cloud SDK based implementation of {@link AppEngineFlexibleStaging}. */
public class CloudSdkAppEngineFlexibleStaging implements AppEngineFlexibleStaging {

  private static final Logger log =
      Logger.getLogger(CloudSdkAppEngineFlexibleStaging.class.getName());

  private static final String APP_YAML = "app.yaml";

  /**
   * Stages a Java JAR/WAR App Engine Flexible Environment application to be deployed.
   *
   * <p>Copies app.yaml, Dockerfile and the application artifact to the staging area.
   */
  @Override
  public void stageFlexible(StageFlexibleConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    File stagingDirectory = config.getStagingDirectory();

    if (!stagingDirectory.exists()) {
      throw new AppEngineException(
          "Staging directory does not exist. Location: " + stagingDirectory);
    }
    if (!stagingDirectory.isDirectory()) {
      throw new AppEngineException(
          "Staging location is not a directory. Location: " + stagingDirectory);
    }

    try {
      String runtime = findRuntime(config);
      CopyService copyService = new CopyService();
      copyDockerContext(config, copyService, runtime);
      copyAppEngineContext(config, copyService);
      copyArtifact(config, copyService);
    } catch (IOException ex) {
      throw new AppEngineException(ex);
    }
  }

  @VisibleForTesting
  @Nullable
  static String findRuntime(StageFlexibleConfiguration config)
      throws IOException, AppEngineException {
    // verify that app.yaml that contains runtime:java
    File appEngineDirectory = config.getAppEngineDirectory();
    if (appEngineDirectory == null) {
      throw new AppEngineException("Invalid Staging Configuration: missing App Engine directory");
    }
    Path appYaml = appEngineDirectory.toPath().resolve(APP_YAML);
    try (InputStream input = MoreFiles.asByteSource(appYaml).openBufferedStream()) {
      return AppYaml.parse(input).getRuntime();
    }
  }

  @VisibleForTesting
  static void copyDockerContext(
      StageFlexibleConfiguration config, CopyService copyService, @Nullable String runtime)
      throws IOException, AppEngineException {
    File dockerDirectory = config.getDockerDirectory();
    if (dockerDirectory != null) {
      if (dockerDirectory.exists()) {
        if ("java".equals(runtime)) {
          log.warning(
              "WARNING: runtime 'java' detected, any docker configuration in "
                  + dockerDirectory
                  + " will be ignored. If you wish to specify a docker configuration, please use "
                  + "'runtime: custom'.");
        } else {
          // Copy docker context to staging
          if (!Files.isRegularFile(dockerDirectory.toPath().resolve("Dockerfile"))) {
            throw new AppEngineException(
                "Docker directory " + dockerDirectory.toPath() + " does not contain Dockerfile.");
          } else {
            File stagingDirectory = config.getStagingDirectory();
            copyService.copyDirectory(dockerDirectory.toPath(), stagingDirectory.toPath());
          }
        }
      }
    }
  }

  @VisibleForTesting
  static void copyAppEngineContext(StageFlexibleConfiguration config, CopyService copyService)
      throws IOException, AppEngineException {
    File appEngineDirectory = config.getAppEngineDirectory();
    Path appYaml = appEngineDirectory.toPath().resolve(APP_YAML);
    if (!Files.exists(appYaml)) {
      throw new AppEngineException(APP_YAML + " not found in the App Engine directory.");
    }
    Path stagingDirectory = config.getStagingDirectory().toPath();
    copyService.copyFileAndReplace(appYaml, stagingDirectory.resolve(APP_YAML));
  }

  private static void copyArtifact(StageFlexibleConfiguration config, CopyService copyService)
      throws IOException, AppEngineException {
    // Copy the JAR/WAR file to staging.
    File artifact = config.getArtifact();
    if (artifact.exists()) {
      File stagingDirectory = config.getStagingDirectory();
      Path destination = stagingDirectory.toPath().resolve(artifact.toPath().getFileName());
      copyService.copyFileAndReplace(artifact.toPath(), destination);
    } else {
      throw new AppEngineException("Artifact doesn't exist at '" + artifact.getPath() + "'.");
    }
  }

  @VisibleForTesting
  static class CopyService {

    void copyDirectory(Path src, Path dest) throws IOException {
      FileUtil.copyDirectory(src, dest);
    }

    void copyFileAndReplace(Path src, Path dest) throws IOException {
      Files.copy(src, dest, REPLACE_EXISTING);
    }
  }
}
