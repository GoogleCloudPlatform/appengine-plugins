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

/** Plain Java bean implementation of {@link StageFlexibleConfiguration}. */
public class DefaultStageFlexibleConfiguration implements StageFlexibleConfiguration {

  @Nullable private File appEngineDirectory;
  @Nullable private File dockerDirectory;
  @Nullable private File artifact;
  @Nullable private File stagingDirectory;

  @Override
  @Nullable
  public File getAppEngineDirectory() {
    return appEngineDirectory;
  }

  public void setAppEngineDirectory(File appEngineDirectory) {
    this.appEngineDirectory = appEngineDirectory;
  }

  @Override
  @Nullable
  public File getDockerDirectory() {
    return dockerDirectory;
  }

  public void setDockerDirectory(File dockerDirectory) {
    this.dockerDirectory = dockerDirectory;
  }

  @Override
  @Nullable
  public File getArtifact() {
    return artifact;
  }

  public void setArtifact(File artifact) {
    this.artifact = artifact;
  }

  @Override
  @Nullable
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  public void setStagingDirectory(File stagingDirectory) {
    this.stagingDirectory = stagingDirectory;
  }
}
