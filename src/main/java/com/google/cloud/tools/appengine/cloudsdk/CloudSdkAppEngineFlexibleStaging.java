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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

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
    Preconditions.checkNotNull(config.getStagingDirectory());
    Preconditions.checkNotNull(config.getArtifact());

    if (!config.getStagingDirectory().exists()) {
      throw new AppEngineException(
          "Staging directory does not exist. Location: " + config.getStagingDirectory().toPath());
    }
    if (!config.getStagingDirectory().isDirectory()) {
      throw new AppEngineException(
          "Staging location is not a directory. Location: "
              + config.getStagingDirectory().toPath());
    }

    try {
      String runtime = findRuntime(config);
      CopyService copyService = new CopyService();
      copyDockerContext(config, copyService, runtime);
      copyAppEngineContext(config, copyService);
      copyArtifact(config, copyService);
    } catch (IOException | YAMLException ex) {
      throw new AppEngineException(ex);
    }
  }

  @VisibleForTesting
  static String findRuntime(StageFlexibleConfiguration config)
      throws IOException, AppEngineException {
    try {
      // verification for app.yaml that contains runtime:java
      Path appYaml = config.getAppEngineDirectory().toPath().resolve(APP_YAML);
      return new AppYaml(appYaml).getRuntime();
    } catch (ScannerException | ParserException ex) {
      throw new AppEngineException("Malformed 'app.yaml'.", ex);
    }
  }

  @VisibleForTesting
  static void copyDockerContext(
      StageFlexibleConfiguration config, CopyService copyService, String runtime)
      throws IOException, AppEngineException {
    if (config.getDockerDirectory() != null && config.getDockerDirectory().exists()) {
      if ("java".equals(runtime)) {
        log.warning(
            "WARNING: runtime 'java' detected, any docker configuration in "
                + config.getDockerDirectory()
                + " will be ignored. If you wish to specify a docker configuration, please use "
                + "'runtime: custom'.");
      } else {
        // Copy docker context to staging
        if (!Files.isRegularFile(config.getDockerDirectory().toPath().resolve("Dockerfile"))) {
          throw new AppEngineException(
              "Docker directory "
                  + config.getDockerDirectory().toPath()
                  + " does not contain Dockerfile.");
        } else {
          copyService.copyDirectory(
              config.getDockerDirectory().toPath(), config.getStagingDirectory().toPath());
        }
      }
    }
  }

  @VisibleForTesting
  static void copyAppEngineContext(StageFlexibleConfiguration config, CopyService copyService)
      throws IOException, AppEngineException {
    Path appYaml = config.getAppEngineDirectory().toPath().resolve(APP_YAML);
    if (!appYaml.toFile().exists()) {
      throw new AppEngineException(APP_YAML + " not found in the App Engine directory.");
    }
    copyService.copyFileAndReplace(
        appYaml, config.getStagingDirectory().toPath().resolve(APP_YAML));
  }

  private static void copyArtifact(StageFlexibleConfiguration config, CopyService copyService)
      throws IOException, AppEngineException {
    // Copy the JAR/WAR file to staging.
    if (config.getArtifact() != null && config.getArtifact().exists()) {
      Path destination =
          config
              .getStagingDirectory()
              .toPath()
              .resolve(config.getArtifact().toPath().getFileName());
      copyService.copyFileAndReplace(config.getArtifact().toPath(), destination);
    } else {
      throw new AppEngineException(
          "Artifact doesn't exist at '" + config.getArtifact().getPath() + "'.");
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
