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

import java.io.File;
import javax.annotation.Nullable;

/** Configuration for {@link AppEngineFlexibleStaging#stageFlexible(StageFlexibleConfiguration)}. */
public class StageFlexibleConfiguration {

  private final File appEngineDirectory;
  @Nullable private final File dockerDirectory;
  private final File artifact;
  private final File stagingDirectory;

  private StageFlexibleConfiguration(
      File appEngineDirectory,
      @Nullable File dockerDirectory,
      File artifact,
      File stagingDirectory) {
    this.appEngineDirectory = appEngineDirectory;
    this.dockerDirectory = dockerDirectory;
    this.artifact = artifact;
    this.stagingDirectory = stagingDirectory;
  }

  /** Directory containing {@code app.yaml}. */
  public File getAppEngineDirectory() {
    return appEngineDirectory;
  }

  /** Directory containing {@code Dockerfile} and other resources used by it. */
  @Nullable
  public File getDockerDirectory() {
    return dockerDirectory;
  }

  /** Artifact to deploy such as WAR or JAR. */
  public File getArtifact() {
    return artifact;
  }

  /**
   * Directory where {@code app.yaml}, files in docker directory, and the artifact to deploy will be
   * copied for deploying.
   */
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  public static Builder builder(File appEngineDirectory, File artifact, File stagingDirectory) {
    return new Builder(appEngineDirectory, artifact, stagingDirectory);
  }

  public static final class Builder {
    private File appEngineDirectory;
    @Nullable private File dockerDirectory;
    private File artifact;
    private File stagingDirectory;

    Builder(File appEngineDirectory, File artifact, File stagingDirectory) {
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

    public StageFlexibleConfiguration.Builder setDockerDirectory(@Nullable File dockerDirectory) {
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
