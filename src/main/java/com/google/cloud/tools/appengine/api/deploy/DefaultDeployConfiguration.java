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

import com.google.cloud.tools.appengine.api.DefaultConfiguration;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;

/** Plain Java bean implementation of {@link DeployConfiguration}. */
public class DefaultDeployConfiguration extends DefaultConfiguration
    implements DeployConfiguration {

  @Nullable private List<File> deployables;
  @Nullable private String bucket;
  @Nullable private String imageUrl;
  @Nullable private Boolean promote;
  @Nullable private String server;
  @Nullable private Boolean stopPreviousVersion;
  @Nullable private String version;

  @Override
  @Nullable
  public List<File> getDeployables() {
    return deployables;
  }

  public void setDeployables(List<File> deployables) {
    this.deployables = deployables;
  }

  @Override
  @Nullable
  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  @Override
  @Nullable
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Override
  @Nullable
  public Boolean getPromote() {
    return promote;
  }

  public void setPromote(Boolean promote) {
    this.promote = promote;
  }

  @Override
  @Nullable
  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  @Override
  @Nullable
  public Boolean getStopPreviousVersion() {
    return stopPreviousVersion;
  }

  public void setStopPreviousVersion(Boolean stopPreviousVersion) {
    this.stopPreviousVersion = stopPreviousVersion;
  }

  @Override
  @Nullable
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
