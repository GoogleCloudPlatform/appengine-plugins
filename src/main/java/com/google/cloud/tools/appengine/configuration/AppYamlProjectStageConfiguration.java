/*
 * Copyright 2017 Google LLC.
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

package com.google.cloud.tools.appengine.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Arguments needed to stage an App Engine app.yaml based application. Null return values indicate
 * that the configuration was not set, and thus assumes the tool default value.
 */
public class AppYamlProjectStageConfiguration {

  private final Path appEngineDirectory;
  @Nullable private final Path dockerDirectory;
  @Nullable private final List<Path> extraFilesDirectories;
  private final Path artifact;
  private final Path stagingDirectory;

  private AppYamlProjectStageConfiguration(
      Path appEngineDirectory,
      @Nullable Path dockerDirectory,
      @Nullable List<Path> extraFilesDirectories,
      Path artifact,
      Path stagingDirectory) {
    this.appEngineDirectory = appEngineDirectory;
    this.dockerDirectory = dockerDirectory;
    this.artifact = artifact;
    this.stagingDirectory = stagingDirectory;
    this.extraFilesDirectories =
        (extraFilesDirectories == null) ? null : ImmutableList.copyOf(extraFilesDirectories);
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

  /** Directories containing other files to be deployed with the application. */
  @Nullable
  public List<Path> getExtraFilesDirectory() {
    return extraFilesDirectories;
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

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Create a new builder for AppYamlProjectStageConfiguration.
   * 
   * @deprecated Use
   *     newBuilder().appEngineDirectory(appEngineDirectory).artifact(artifact).stagingDirectory(stagingDirectory)
   *     instead.
   */
  @Deprecated
  public static Builder builder(Path appEngineDirectory, Path artifact, Path stagingDirectory) {
    return new Builder()
        .appEngineDirectory(appEngineDirectory)
        .artifact(artifact)
        .stagingDirectory(stagingDirectory);
  }

  public static final class Builder {
    private Path appEngineDirectory;
    @Nullable private Path dockerDirectory;
    @Nullable private List<Path> extraFilesDirectories;
    private Path artifact;
    private Path stagingDirectory;

    private Builder() {}

    public Builder appEngineDirectory(Path appEngineDirectory) {
      this.appEngineDirectory = Preconditions.checkNotNull(appEngineDirectory);
      return this;
    }

    public Builder artifact(Path artifact) {
      this.artifact = Preconditions.checkNotNull(artifact);
      return this;
    }

    public Builder stagingDirectory(Path stagingDirectory) {
      this.stagingDirectory = Preconditions.checkNotNull(stagingDirectory);
      return this;
    }

    public AppYamlProjectStageConfiguration.Builder dockerDirectory(
        @Nullable Path dockerDirectory) {
      this.dockerDirectory = dockerDirectory;
      return this;
    }

    public AppYamlProjectStageConfiguration.Builder extraFilesDirectories(
        @Nullable List<Path> extraFilesDirectories) {
      this.extraFilesDirectories = extraFilesDirectories;
      return this;
    }

    /** Build a {@link AppYamlProjectStageConfiguration}. */
    public AppYamlProjectStageConfiguration build() {
      Preconditions.checkState(appEngineDirectory != null, "No AppEngine directory supplied");
      Preconditions.checkState(stagingDirectory != null, "No staging directory supplied");
      Preconditions.checkState(artifact != null, "No artifact supplied");

      return new AppYamlProjectStageConfiguration(
          this.appEngineDirectory,
          this.dockerDirectory,
          this.extraFilesDirectories,
          this.artifact,
          this.stagingDirectory);
    }
  }
}
