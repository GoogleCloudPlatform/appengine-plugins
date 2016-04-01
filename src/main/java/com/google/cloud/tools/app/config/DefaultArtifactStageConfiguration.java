/**
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
package com.google.cloud.tools.app.config;

import com.google.common.base.Preconditions;

import java.io.File;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link ArtifactStageConfiguration}.
 */
public class DefaultArtifactStageConfiguration implements ArtifactStageConfiguration {

  private final File appYaml;
  private final File dockerfile;
  private final File artifact;
  private final File stagingDirectory;

  private DefaultArtifactStageConfiguration(@Nullable File appYaml, @Nullable File dockerfile,
      File artifact, File stagingDirectory) {
    this.appYaml = appYaml;
    this.dockerfile = dockerfile;
    this.artifact = artifact;
    this.stagingDirectory = stagingDirectory;
  }

  @Override
  public File getAppYaml() {
    return appYaml;
  }

  @Override
  public File getDockerfile() {
    return dockerfile;
  }

  @Override
  public File getArtifact() {
    return artifact;
  }

  @Override
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  public static Builder newBuilder(File artifact, File stagingDirectory) {
    Preconditions.checkNotNull(artifact);
    Preconditions.checkNotNull(stagingDirectory);

    return new Builder(artifact, stagingDirectory);
  }

  public static class Builder {
    private File appYaml;
    private File dockerfile;
    private File artifact;
    private File stagingDirectory;

    private Builder(File artifact, File stagingDirectory) {
      this.artifact = artifact;
      this.stagingDirectory = stagingDirectory;
    }

    public Builder appYaml(File appYaml) {
      this.appYaml = appYaml;
      return this;
    }

    public Builder dockerfile(File dockerfile) {
      this.dockerfile = dockerfile;
      return this;
    }

    public ArtifactStageConfiguration build() {
      return new DefaultArtifactStageConfiguration(appYaml, dockerfile, artifact,
          stagingDirectory);
    }
  }
}
