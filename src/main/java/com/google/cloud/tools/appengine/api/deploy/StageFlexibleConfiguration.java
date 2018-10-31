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

import java.nio.file.Path;
import javax.annotation.Nullable;

/** Configuration for {@link AppEngineFlexibleStaging#stageFlexible(StageFlexibleConfiguration)}. */
public class StageFlexibleConfiguration {

  private final Path appEngineDirectory;
  @Nullable private final Path dockerDirectory;
  private final Path artifact;
  private final Path stagingDirectory;

  private StageFlexibleConfiguration(
      Path appEngineDirectory,
      @Nullable Path dockerDirectory,
      Path artifact,
      Path stagingDirectory) {
    this.appEngineDirectory = appEngineDirectory;
    this.dockerDirectory = dockerDirectory;
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
    private Path artifact;
    private Path stagingDirectory;

    Builder(Path appEngineDirectory, Path artifact, Path stagingDirectory) {
      if (appEngineDirectory == null) {
        throw new NullPointerException("Null appEngineDirectory");
      }
      if (artifact == null) {
        throw new NullPointerException("Null artifact");
      }
      if (stagingDirectory == null) {
        throw new NullPointerException("Null stagingDirectory");
      }
      this.appEngineDirectory = appEngineDirectory;
      this.artifact = artifact;
      this.stagingDirectory = stagingDirectory;
    }

    public StageFlexibleConfiguration.Builder dockerDirectory(@Nullable Path dockerDirectory) {
      this.dockerDirectory = dockerDirectory;
      return this;
    }

    /** Build a {@link StageFlexibleConfiguration}. */
    public StageFlexibleConfiguration build() {
      return new StageFlexibleConfiguration(
          this.appEngineDirectory, this.dockerDirectory, this.artifact, this.stagingDirectory);
    }
  }
}
