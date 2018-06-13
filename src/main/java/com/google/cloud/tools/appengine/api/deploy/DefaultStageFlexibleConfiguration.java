/*
 * Copyright 2018 Google Inc.
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
import java.io.File;
import javax.annotation.Nullable;

/** Plain java bean implementation of {@link StageFlexibleConfiguration}. */
public class DefaultStageFlexibleConfiguration implements StageFlexibleConfiguration {

  private File appEngineDirectory;
  @Nullable private File dockerDirectory;
  private File artifact;
  private File stagingDirectory;

  private DefaultStageFlexibleConfiguration(
      File appEngineDirectory,
      @Nullable File dockerDirectory,
      File artifact,
      File stagingDirectory) {
    this.appEngineDirectory = Preconditions.checkNotNull(appEngineDirectory);
    this.dockerDirectory = dockerDirectory;
    this.artifact = Preconditions.checkNotNull(artifact);
    this.stagingDirectory = Preconditions.checkNotNull(stagingDirectory);
  }

  /** Directory containing {@code app.yaml}. */
  @Override
  public File getAppEngineDirectory() {
    return appEngineDirectory;
  }

  /** Directory containing {@code Dockerfile} and other resources used by it. */
  @Nullable
  @Override
  public File getDockerDirectory() {
    return dockerDirectory;
  }

  /** Artifact to deploy such as WAR or JAR. */
  @Override
  public File getArtifact() {
    return artifact;
  }

  /**
   * Directory where {@code app.yaml}, files in docker directory, and the artifact to deploy will be
   * copied for deploying.
   */
  @Override
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  public static class Builder {

    @Nullable private File appEngineDirectory;
    @Nullable private File dockerDirectory;
    @Nullable private File artifact;
    @Nullable private File stagingDirectory;

    public Builder setAppEngineDirectory(File appEngineDirectory) {
      this.appEngineDirectory = Preconditions.checkNotNull(appEngineDirectory);
      return this;
    }

    public Builder setDockerDirectory(File dockerDirectory) {
      this.dockerDirectory = Preconditions.checkNotNull(dockerDirectory);
      return this;
    }

    public Builder setArtifact(File artifact) {
      this.artifact = Preconditions.checkNotNull(artifact);
      return this;
    }

    public Builder setStagingDirectory(File stagingDirectory) {
      this.stagingDirectory = Preconditions.checkNotNull(stagingDirectory);
      return this;
    }

    /**
     * Returns a fully configured StageFlexibleConfiguration object.
     *
     * @throws NullPointerException if any required field has not been set
     */
    public DefaultStageFlexibleConfiguration build() {
      if (appEngineDirectory == null) {
        throw new NullPointerException("Incomplete configuration: Missing App Engine directory");
      }
      if (artifact == null) {
        throw new NullPointerException("Incomplete configuration: Missing artifact");
      }
      if (stagingDirectory == null) {
        throw new NullPointerException("Incomplete configuration: Missing staging directory");
      }
      return new DefaultStageFlexibleConfiguration(
          appEngineDirectory, dockerDirectory, artifact, stagingDirectory);
    }
  }
}
