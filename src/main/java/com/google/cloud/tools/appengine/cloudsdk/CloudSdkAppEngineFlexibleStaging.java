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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Cloud SDK based implementation of {@link AppEngineFlexibleStaging}.
 */
public class CloudSdkAppEngineFlexibleStaging implements AppEngineFlexibleStaging {

  private static final Logger log = Logger
      .getLogger(CloudSdkAppEngineFlexibleStaging.class.getName());

  private static final String APP_YAML = "app.yaml";
  private static final String CRON_YAML = "cron.yaml";
  private static final String DOS_YAML = "dos.yaml";
  private static final String DISPATCH_YAML = "dispatch.yaml";
  private static final String INDEX_YAML = "index.yaml";
  private static final String QUEUE_YAML = "queue.yaml";

  /**
   * Stages a Java JAR/WAR App Engine Flexible Environment application to be deployed.
   *
   * <p>Copies app.yaml, Dockerfile and the application artifact to the staging area.
   *
   * <p>If app.yaml or Dockerfile do not exist, gcloud cloud will create them during deployment.
   */
  @Override
  public void stageFlexible(StageFlexibleConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getStagingDirectory());
    Preconditions.checkNotNull(config.getArtifact());

    if (!config.getStagingDirectory().exists()) {
      throw new AppEngineException("Staging directory does not exist. Location: "
          + config.getStagingDirectory().toPath());
    }
    if (!config.getStagingDirectory().isDirectory()) {
      throw new AppEngineException("Staging location is not a directory. Location: "
          + config.getStagingDirectory().toPath());
    }

    String runtime = findRuntime(config);
    try {
      CopyService copyService = new CopyService();
      copyDockerContext(config, copyService, runtime);
      copyAppEngineContext(config, copyService);
      copyArtifact(config, copyService);
    } catch (IOException e) {
      throw new AppEngineException(e);
    }
  }

  private static String findRuntime(StageFlexibleConfiguration config) {
    // verification for app.yaml that contains runtime:java
    Path appYaml = config.getAppEngineDirectory().toPath().resolve(APP_YAML);
    String runtime = null;
    try {
      if (Files.isRegularFile(appYaml)) {
        runtime = new AppYaml(appYaml).getRuntime();
      }
    } catch (IOException e) {
      log.warning("Unable to determine runtime: error parsing app.yaml : " + e.getMessage());
    }
    return runtime;
  }

  @VisibleForTesting
  static void copyDockerContext(StageFlexibleConfiguration config, CopyService copyService,
      String runtime) throws IOException {
    if (config.getDockerDirectory() != null && config.getDockerDirectory().exists()) {
      if (runtime != null && runtime.equals("java")) {
        log.warning("WARNING: 'runtime 'java' detected, any docker configuration in "
            + config.getDockerDirectory() + " will be ignored. If you wish to specify"
            + "docker configuration, please use 'runtime: custom'");
      } else {
        // Copy docker context to staging
        if (!Files.isRegularFile(config.getDockerDirectory().toPath().resolve("Dockerfile"))) {
          throw new AppEngineException("Docker directory " + config.getDockerDirectory().toPath()
              + " does not contain Dockerfile");
        } else {
          copyService.copyDirectory(config.getDockerDirectory().toPath(),
              config.getStagingDirectory().toPath());
        }
      }
    }
  }

  @VisibleForTesting
  static void copyAppEngineContext(StageFlexibleConfiguration config, CopyService copyService)
      throws IOException {
    Path fromDirectory = config.getAppEngineDirectory().toPath();
    Path appYaml = fromDirectory.resolve(APP_YAML);
    if (!appYaml.toFile().exists()) {
      throw new AppEngineException(APP_YAML + " not found in the App Engine directory.");
    }
    Path toDirectory = config.getStagingDirectory().toPath();
    copyService.copyFileAndReplace(appYaml, toDirectory.resolve(APP_YAML));

    if (config.getIncludeOptionalConfigurationFiles()) {
      copyFileAndReplaceIfFileExists(fromDirectory, toDirectory, CRON_YAML, copyService);
      copyFileAndReplaceIfFileExists(fromDirectory, toDirectory, DOS_YAML, copyService);
      copyFileAndReplaceIfFileExists(fromDirectory, toDirectory, DISPATCH_YAML, copyService);
      copyFileAndReplaceIfFileExists(fromDirectory, toDirectory, INDEX_YAML, copyService);
      copyFileAndReplaceIfFileExists(fromDirectory, toDirectory, QUEUE_YAML, copyService);
    }
  }

  @VisibleForTesting
  static void copyFileAndReplaceIfFileExists(Path fromDirectory, Path toDirectory,
        String filename, CopyService copyService) throws IOException {
    Path file = fromDirectory.resolve(filename);
    if (file.toFile().exists()) {
      copyService.copyFileAndReplace(file, toDirectory.resolve(filename));
    }
  }

  private static void copyArtifact(StageFlexibleConfiguration config, CopyService copyService)
      throws IOException {
    // Copy the JAR/WAR file to staging.
    if (config.getArtifact() != null && config.getArtifact().exists()) {
      Path destination = config.getStagingDirectory().toPath()
          .resolve(config.getArtifact().toPath().getFileName());
      copyService.copyFileAndReplace(config.getArtifact().toPath(), destination);
    } else {
      throw new AppEngineException("Artifact doesn't exist at '" + config.getArtifact().getPath()
          + "'");
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
