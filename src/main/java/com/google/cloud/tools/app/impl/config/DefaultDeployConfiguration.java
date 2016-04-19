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

import com.google.cloud.tools.app.api.deploy.DeployConfiguration;

import java.io.File;
import java.util.List;

/**
 * Created by meltsufin on 4/19/16.
 */
public class DefaultDeployConfiguration implements DeployConfiguration {

  private List<File> deployables;
  private String bucket;
  private String dockerBuild;
  private boolean force;
  private String imageUrl;
  private boolean promote;
  private String server;
  private boolean stopPreviousVersion;
  private String version;

  @Override
  public List<File> getDeployables() {
    return deployables;
  }

  public void setDeployables(List<File> deployables) {
    this.deployables = deployables;
  }

  @Override
  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  @Override
  public String getDockerBuild() {
    return dockerBuild;
  }

  public void setDockerBuild(String dockerBuild) {
    this.dockerBuild = dockerBuild;
  }

  @Override
  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  @Override
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Override
  public boolean isPromote() {
    return promote;
  }

  public void setPromote(boolean promote) {
    this.promote = promote;
  }

  @Override
  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  @Override
  public boolean isStopPreviousVersion() {
    return stopPreviousVersion;
  }

  public void setStopPreviousVersion(boolean stopPreviousVersion) {
    this.stopPreviousVersion = stopPreviousVersion;
  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
