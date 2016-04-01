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
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link DeployConfiguration}.
 */
public class DefaultDeployConfiguration implements DeployConfiguration {

  private final List<File> deployables;
  private final String bucket;
  private final String dockerBuild;
  private final boolean force;
  private final String imageUrl;
  private final boolean promote;
  private final String server;
  private final boolean stopPreviousVersion;
  private final String version;

  private DefaultDeployConfiguration(List<File> deployables, @Nullable String bucket,
      @Nullable String dockerBuild, boolean force, @Nullable String imageUrl,
      boolean promote, @Nullable String server, boolean stopPreviousVersion,
      @Nullable String version) {
    this.deployables = deployables;
    this.bucket = bucket;
    this.dockerBuild = dockerBuild;
    this.force = force;
    this.imageUrl = imageUrl;
    this.promote = promote;
    this.server = server;
    this.stopPreviousVersion = stopPreviousVersion;
    this.version = version;
  }

  @Override
  public List<File> getDeployables() {
    return deployables;
  }

  @Override
  public String getBucket() {
    return bucket;
  }

  @Override
  public String getDockerBuild() {
    return dockerBuild;
  }

  @Override
  public boolean isForce() {
    return force;
  }

  @Override
  public String getImageUrl() {
    return imageUrl;
  }

  @Override
  public boolean isPromote() {
    return promote;
  }

  @Override
  public String getServer() {
    return server;
  }

  @Override
  public boolean isStopPreviousVersion() {
    return stopPreviousVersion;
  }

  @Override
  public String getVersion() {
    return version;
  }

  public static Builder newBuilder(File... deployables) {
    Preconditions.checkArgument(deployables.length > 0);

    return new Builder(ImmutableList.copyOf(deployables));
  }

  public static class Builder {
    private List<File> deployables;
    private String bucket;
    private String dockerBuild;
    private boolean force;
    private String imageUrl;
    private boolean promote;
    private String server;
    private boolean stopPreviousVersion;
    private String version;

    private Builder(List<File> deployables) {
      this.deployables = deployables;
    }

    public Builder bucket(String bucket) {
      this.bucket = bucket;
      return this;
    }

    public Builder dockerBuild(String dockerBuild) {
      this.dockerBuild = dockerBuild;
      return this;
    }

    public Builder force(boolean force) {
      this.force = force;
      return this;
    }

    public Builder imageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    public Builder promote(boolean promote) {
      this.promote = promote;
      return this;
    }

    public Builder server(String server) {
      this.server = server;
      return this;
    }

    public Builder stopPreviousVersion(boolean stopPreviousVersion) {
      this.stopPreviousVersion = stopPreviousVersion;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public DeployConfiguration build() {
      return new DefaultDeployConfiguration(deployables, bucket, dockerBuild, force, imageUrl,
          promote, server, stopPreviousVersion, version);
    }
  }
}
