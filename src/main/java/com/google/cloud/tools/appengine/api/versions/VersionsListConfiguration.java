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

package com.google.cloud.tools.appengine.api.versions;

import javax.annotation.Nullable;

public class VersionsListConfiguration {

  @Nullable private final Boolean hideNoTraffic;
  @Nullable private final String projectId;
  @Nullable private final String service;

  private VersionsListConfiguration(
      @Nullable Boolean hideNoTraffic, @Nullable String projectId, @Nullable String service) {
    this.hideNoTraffic = hideNoTraffic;
    this.projectId = projectId;
    this.service = service;
  }

  @Nullable
  public String getService() {
    return service;
  }

  @Nullable
  public Boolean getHideNoTraffic() {
    return hideNoTraffic;
  }

  @Nullable
  public String getProjectId() {
    return projectId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    @Nullable private Boolean hideNoTraffic;
    @Nullable private String projectId;
    @Nullable private String service;

    private Builder() {}

    public Builder setHideNoTraffic(@Nullable Boolean hideNoTraffic) {
      this.hideNoTraffic = hideNoTraffic;
      return this;
    }

    public Builder setProjectId(@Nullable String projectId) {
      this.projectId = projectId;
      return this;
    }

    public Builder setService(@Nullable String service) {
      this.service = service;
      return this;
    }

    public VersionsListConfiguration build() {
      return new VersionsListConfiguration(hideNoTraffic, projectId, service);
    }
  }
}
