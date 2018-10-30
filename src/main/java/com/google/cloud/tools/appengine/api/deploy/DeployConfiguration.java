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

package com.google.cloud.tools.appengine.api.deploy;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;

/** Configuration for {@link AppEngineDeployment#deploy(DeployConfiguration)}. */
public class DeployConfiguration {

  @Nullable private final String bucket;
  private final List<File> deployables;
  @Nullable private final String imageUrl;
  @Nullable private final String projectId;
  @Nullable private final Boolean promote;
  @Nullable private final String server;
  @Nullable private final Boolean stopPreviousVersion;
  @Nullable private final String version;

  private DeployConfiguration(
      @Nullable String bucket,
      List<File> deployables,
      @Nullable String imageUrl,
      @Nullable String projectId,
      @Nullable Boolean promote,
      @Nullable String server,
      @Nullable Boolean stopPreviousVersion,
      @Nullable String version) {
    this.bucket = bucket;
    this.deployables = deployables;
    this.imageUrl = imageUrl;
    this.projectId = projectId;
    this.promote = promote;
    this.server = server;
    this.stopPreviousVersion = stopPreviousVersion;
    this.version = version;
  }

  /** GCS storage bucket used for staging files associated with deployment. */
  @Nullable
  public String getBucket() {
    return bucket;
  }

  /** List of deployable target directories or yaml files. */
  public List<File> getDeployables() {
    return deployables;
  }

  /** Docker image to use during deployment (only for app.yaml deployments). */
  @Nullable
  public String getImageUrl() {
    return imageUrl;
  }

  /** Google Cloud Project ID to deploy to. */
  @Nullable
  public String getProjectId() {
    return projectId;
  }

  /** Promote the deployed version to receive all traffic. */
  @Nullable
  public Boolean getPromote() {
    return promote;
  }

  /** The App Engine server to use. Users typically will never set this value. */
  @Nullable
  public String getServer() {
    return server;
  }

  /** Stop the previous running version when deploying and promote this new version. */
  @Nullable
  public Boolean getStopPreviousVersion() {
    return stopPreviousVersion;
  }

  /** Version to deploy. */
  @Nullable
  public String getVersion() {
    return version;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    @Nullable private String bucket;
    @Nullable private List<File> deployables;
    @Nullable private String imageUrl;
    @Nullable private String projectId;
    @Nullable private Boolean promote;
    @Nullable private String server;
    @Nullable private Boolean stopPreviousVersion;
    @Nullable private String version;

    Builder() {}

    public DeployConfiguration.Builder setBucket(@Nullable String bucket) {
      this.bucket = bucket;
      return this;
    }

    /** Set a non-null non-empty list of deployables. */
    public DeployConfiguration.Builder setDeployables(List<File> deployables) {
      if (deployables == null || deployables.size() == 0) {
        throw new IllegalArgumentException("Null/empty deployables");
      }
      this.deployables = deployables;
      return this;
    }

    public DeployConfiguration.Builder setImageUrl(@Nullable String imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    public DeployConfiguration.Builder setProjectId(@Nullable String projectId) {
      this.projectId = projectId;
      return this;
    }

    public DeployConfiguration.Builder setPromote(@Nullable Boolean promote) {
      this.promote = promote;
      return this;
    }

    public DeployConfiguration.Builder setServer(@Nullable String server) {
      this.server = server;
      return this;
    }

    public DeployConfiguration.Builder setStopPreviousVersion(
        @Nullable Boolean stopPreviousVersion) {
      this.stopPreviousVersion = stopPreviousVersion;
      return this;
    }

    public DeployConfiguration.Builder setVersion(@Nullable String version) {
      this.version = version;
      return this;
    }

    /** Build a {@link DeployConfiguration}. */
    public DeployConfiguration build() {
      if (deployables == null) {
        throw new IllegalStateException("Missing required properties: deployables");
      }
      return new DeployConfiguration(
          this.bucket,
          this.deployables,
          this.imageUrl,
          this.projectId,
          this.promote,
          this.server,
          this.stopPreviousVersion,
          this.version);
    }
  }
}
