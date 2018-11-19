/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.appengine.api.deploy;

import com.google.common.base.Preconditions;
import java.nio.file.Path;
import javax.annotation.Nullable;

/** Configuration for {@link AppEngineArchiveStaging#stageArchive(StageArchiveConfiguration)}. */
public class StageArchiveConfiguration {

  private final Path appEngineDirectory;
  @Nullable private final Path dockerDirectory;
  @Nullable private final Path extraFilesDirectory;
  private final Path artifact;
  private final Path stagingDirectory;

  private StageArchiveConfiguration(
      Path appEngineDirectory,
      @Nullable Path dockerDirectory,
      @Nullable Path extraFilesDirectory,
      Path artifact,
      Path stagingDirectory) {
    this.appEngineDirectory = appEngineDirectory;
    this.dockerDirectory = dockerDirectory;
    this.extraFilesDirectory = extraFilesDirectory;
    this.artifact = artifact;
    this.stagingDirectory = stagingDirectory;
  }

  /** Directory containing {@code app.yaml}. */
  public Path getAppEngineDirectory() {
    return appEngineDirectory;
  }

  /** Directory containing {@code Dockerfile} and other resources used by it. */
  @Nullable
  public Path getDockerDirectory() {
    return dockerDirectory;
  }

  /** Directory containing other files to be deployed with the application. */
  @Nullable
  public Path getExtraFilesDirectory() {
    return extraFilesDirectory;
  }

  /** Artifact to deploy such as WAR or JAR. */
  public Path getArtifact() {
    return artifact;
  }

  /**
   * Directory where {@code app.yaml}, files in docker directory, and the artifact to deploy will be
   * copied for deploying.
   */
  public Path getStagingDirectory() {
    return stagingDirectory;
  }

  public static Builder builder(Path appEngineDirectory, Path artifact, Path stagingDirectory) {
    return new Builder(appEngineDirectory, artifact, stagingDirectory);
  }

  public static final class Builder {
    private Path appEngineDirectory;
    @Nullable private Path dockerDirectory;
    @Nullable private Path extraFilesDirectory;
    private Path artifact;
    private Path stagingDirectory;

    Builder(Path appEngineDirectory, Path artifact, Path stagingDirectory) {
      Preconditions.checkNotNull(appEngineDirectory);
      Preconditions.checkNotNull(artifact);
      Preconditions.checkNotNull(stagingDirectory);

      this.appEngineDirectory = appEngineDirectory;
      this.artifact = artifact;
      this.stagingDirectory = stagingDirectory;
    }

    public StageArchiveConfiguration.Builder dockerDirectory(@Nullable Path dockerDirectory) {
      this.dockerDirectory = dockerDirectory;
      return this;
    }

    public StageArchiveConfiguration.Builder extraFilesDirectory(
        @Nullable Path extraFilesDirectory) {
      this.extraFilesDirectory = extraFilesDirectory;
      return this;
    }

    /** Build a {@link StageArchiveConfiguration}. */
    public StageArchiveConfiguration build() {
      return new StageArchiveConfiguration(
          this.appEngineDirectory,
          this.dockerDirectory,
          this.extraFilesDirectory,
          this.artifact,
          this.stagingDirectory);
    }
  }
}
