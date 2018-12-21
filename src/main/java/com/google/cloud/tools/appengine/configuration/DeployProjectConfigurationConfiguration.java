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

package com.google.cloud.tools.appengine.configuration;

import com.google.cloud.tools.appengine.operations.Deployment;
import com.google.common.base.Preconditions;
import java.nio.file.Path;
import javax.annotation.Nullable;

/** Configuration for {@link Deployment} project-level yaml deployments. */
public class DeployProjectConfigurationConfiguration {

  private final Path appEngineDirectory;
  @Nullable private final String projectId;
  @Nullable private final String server;

  private DeployProjectConfigurationConfiguration(
      Path appEngineDirectory, @Nullable String projectId, @Nullable String server) {
    this.appEngineDirectory = appEngineDirectory;
    this.projectId = projectId;
    this.server = server;
  }

  /** Directory with yaml configuration files. */
  public Path getAppEngineDirectory() {
    return appEngineDirectory;
  }

  /** Google Cloud Project ID to deploy to. */
  @Nullable
  public String getProjectId() {
    return projectId;
  }

  /** The App Engine server to use. Users typically will never set this value. */
  @Nullable
  public String getServer() {
    return server;
  }

  public static Builder builder(Path appEngineDirectory) {
    return new Builder(appEngineDirectory);
  }

  public static final class Builder {
    private Path appEngineDirectory;
    @Nullable private String projectId;
    @Nullable private String server;

    private Builder(Path appEngineDirectory) {
      Preconditions.checkNotNull(appEngineDirectory);

      this.appEngineDirectory = appEngineDirectory;
    }

    public DeployProjectConfigurationConfiguration.Builder projectId(@Nullable String projectId) {
      this.projectId = projectId;
      return this;
    }

    public DeployProjectConfigurationConfiguration.Builder server(@Nullable String server) {
      this.server = server;
      return this;
    }

    /** Build a {@link DeployProjectConfigurationConfiguration}. */
    public DeployProjectConfigurationConfiguration build() {
      return new DeployProjectConfigurationConfiguration(
          this.appEngineDirectory, this.projectId, this.server);
    }
  }
}
