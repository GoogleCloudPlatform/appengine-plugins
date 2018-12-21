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
import com.google.cloud.tools.appengine.api.deploy.AppEngineAppYamlStaging;
import com.google.cloud.tools.appengine.api.deploy.StageAppYamlConfiguration;
import com.google.cloud.tools.io.FileUtil;
import com.google.cloud.tools.project.AppYaml;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Cloud SDK based implementation of {@link AppEngineAppYamlStaging}. */
public class CloudSdkAppEngineAppYamlStaging implements AppEngineAppYamlStaging {

  private static final Logger log =
      Logger.getLogger(CloudSdkAppEngineAppYamlStaging.class.getName());

  private static final String APP_YAML = "app.yaml";

  @VisibleForTesting
  static final ImmutableList<String> OTHER_YAMLS =
      ImmutableList.of("cron.yaml", "dos.yaml", "dispatch.yaml", "index.yaml", "queue.yaml");

  /**
   * Stages a Java JAR/WAR App Engine application to be deployed.
   *
   * <p>Copies app.yaml, Dockerfile and the application artifact to the staging area.
   */
  @Override
  public void stageArchive(StageAppYamlConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Path stagingDirectory = config.getStagingDirectory();

    if (!Files.exists(stagingDirectory)) {
      throw new AppEngineException(
          "Staging directory does not exist. Location: " + stagingDirectory);
    }
    if (!Files.isDirectory(stagingDirectory)) {
      throw new AppEngineException(
          "Staging location is not a directory. Location: " + stagingDirectory);
    }

    try {
      String env = findEnv(config);
      String runtime = findRuntime(config);
      CopyService copyService = new CopyService();
      if ("flex".equals(env)) {
        stageFlexibleArchive(config, runtime);
      } else if ("java11".equals(runtime)) {
        stageStandardArchive(config);
      } else {
        // I don't know how to deploy this
        throw new AppEngineException(
            "Cannot process application with runtime: "
                + runtime
                + (Strings.isNullOrEmpty(env) ? "" : " and env: " + env));
      }
    } catch (IOException ex) {
      throw new AppEngineException(ex);
    }
  }

  @VisibleForTesting
  void stageFlexibleArchive(StageAppYamlConfiguration config, @Nullable String runtime)
      throws IOException, AppEngineException {
    CopyService copyService = new CopyService();
    copyDockerContext(config, copyService, runtime);
    copyExtraFiles(config, copyService);
    copyAppEngineContext(config, copyService);
    copyArtifact(config, copyService);
  }

  @VisibleForTesting
  void stageStandardArchive(StageAppYamlConfiguration config)
      throws IOException, AppEngineException {
    CopyService copyService = new CopyService();
    copyExtraFiles(config, copyService);
    copyAppEngineContext(config, copyService);
    copyArtifact(config, copyService);
  }

  @VisibleForTesting
  @Nullable
  static String findEnv(StageAppYamlConfiguration config) throws AppEngineException, IOException {
    Path appEngineDirectory = config.getAppEngineDirectory();
    if (appEngineDirectory == null) {
      throw new AppEngineException("Invalid Staging Configuration: missing App Engine directory");
    }
    Path appYaml = appEngineDirectory.resolve(APP_YAML);
    try (InputStream input = MoreFiles.asByteSource(appYaml).openBufferedStream()) {
      return AppYaml.parse(input).getEnvironmentType();
    }
  }

  @VisibleForTesting
  @Nullable
  static String findRuntime(StageAppYamlConfiguration config)
      throws IOException, AppEngineException {
    // verify that app.yaml that contains runtime:java
    Path appEngineDirectory = config.getAppEngineDirectory();
    if (appEngineDirectory == null) {
      throw new AppEngineException("Invalid Staging Configuration: missing App Engine directory");
    }
    Path appYaml = appEngineDirectory.resolve(APP_YAML);
    try (InputStream input = MoreFiles.asByteSource(appYaml).openBufferedStream()) {
      return AppYaml.parse(input).getRuntime();
    }
  }

  @VisibleForTesting
  static void copyDockerContext(
      StageAppYamlConfiguration config, CopyService copyService, @Nullable String runtime)
      throws IOException, AppEngineException {
    Path dockerDirectory = config.getDockerDirectory();
    if (dockerDirectory != null) {
      if (Files.exists(dockerDirectory)) {
        if ("java".equals(runtime)) {
          log.warning(
              "WARNING: runtime 'java' detected, any docker configuration in "
                  + dockerDirectory
                  + " will be ignored. If you wish to specify a docker configuration, please use "
                  + "'runtime: custom'.");
        } else {
          // Copy docker context to staging
          if (!Files.isRegularFile(dockerDirectory.resolve("Dockerfile"))) {
            throw new AppEngineException(
                "Docker directory " + dockerDirectory + " does not contain Dockerfile.");
          } else {
            Path stagingDirectory = config.getStagingDirectory();
            copyService.copyDirectory(dockerDirectory, stagingDirectory);
          }
        }
      }
    }
  }

  @VisibleForTesting
  static void copyAppEngineContext(StageAppYamlConfiguration config, CopyService copyService)
      throws IOException, AppEngineException {
    Path appYaml = config.getAppEngineDirectory().resolve(APP_YAML);
    if (!Files.exists(appYaml)) {
      throw new AppEngineException(APP_YAML + " not found in the App Engine directory.");
    }
    Path stagingDirectory = config.getStagingDirectory();
    copyService.copyFileAndReplace(appYaml, stagingDirectory.resolve(APP_YAML));
  }

  @VisibleForTesting
  static void copyExtraFiles(StageAppYamlConfiguration config, CopyService copyService)
      throws IOException, AppEngineException {
    List<Path> extraFilesDirectories = config.getExtraFilesDirectory();
    if (extraFilesDirectories == null) {
      return;
    }
    for (Path extraFilesDirectory : extraFilesDirectories) {
      if (!Files.exists(extraFilesDirectory)) {
        throw new AppEngineException(
            "Extra files directory does not exist. Location: " + extraFilesDirectory);
      }
      if (!Files.isDirectory(extraFilesDirectory)) {
        throw new AppEngineException(
            "Extra files location is not a directory. Location: " + extraFilesDirectory);
      }
      Path stagingDirectory = config.getStagingDirectory();
      copyService.copyDirectory(extraFilesDirectory, stagingDirectory);
    }
  }

  private static void copyArtifact(StageAppYamlConfiguration config, CopyService copyService)
      throws IOException, AppEngineException {
    // Copy the JAR/WAR file to staging.
    Path artifact = config.getArtifact();
    if (Files.exists(artifact)) {
      Path stagingDirectory = config.getStagingDirectory();
      Path destination = stagingDirectory.resolve(artifact.getFileName());
      copyService.copyFileAndReplace(artifact, destination);
    } else {
      throw new AppEngineException("Artifact doesn't exist at '" + artifact + "'.");
    }
  }

  @VisibleForTesting
  static class CopyService {
    void copyDirectory(Path src, Path dest, List<Path> excludes) throws IOException {
      FileUtil.copyDirectory(src, dest, excludes);
    }

    void copyDirectory(Path src, Path dest) throws IOException {
      FileUtil.copyDirectory(src, dest);
    }

    void copyFileAndReplace(Path src, Path dest) throws IOException {
      Files.copy(src, dest, REPLACE_EXISTING);
    }
  }
}
