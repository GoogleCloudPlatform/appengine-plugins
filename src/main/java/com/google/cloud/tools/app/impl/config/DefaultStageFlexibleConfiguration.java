/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.app.impl.config;

import com.google.cloud.tools.app.api.deploy.StageFlexibleConfiguration;

import java.io.File;

/**
 * Plain Java bean implementation of {@link StageFlexibleConfiguration}.
 */
public class DefaultStageFlexibleConfiguration implements StageFlexibleConfiguration {

  private File appYaml;
  private File dockerfile;
  private File artifact;
  private File stagingDirectory;

  @Override
  public File getAppYaml() {
    return appYaml;
  }

  public void setAppYaml(File appYaml) {
    this.appYaml = appYaml;
  }

  @Override
  public File getDockerfile() {
    return dockerfile;
  }

  public void setDockerfile(File dockerfile) {
    this.dockerfile = dockerfile;
  }

  @Override
  public File getArtifact() {
    return artifact;
  }

  public void setArtifact(File artifact) {
    this.artifact = artifact;
  }

  @Override
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  public void setStagingDirectory(File stagingDirectory) {
    this.stagingDirectory = stagingDirectory;
  }
}
