/*
 * Copyright 2016 Google LLC.
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

import com.google.common.base.Preconditions;
import java.util.Collection;
import javax.annotation.Nullable;

public class VersionsSelectionConfiguration {

  @Nullable private final String service;
  @Nullable private final String projectId;
  private final Collection<String> versions;

  private VersionsSelectionConfiguration(
      @Nullable String service, @Nullable String projectId, Collection<String> versions) {
    this.service = service;
    this.projectId = projectId;
    this.versions = versions;
  }

  public Collection<String> getVersions() {
    return versions;
  }

  @Nullable
  public String getService() {
    return service;
  }

  @Nullable
  public String getProjectId() {
    return projectId;
  }

  public static Builder builder(Collection<String> versions) {
    return new Builder(versions);
  }

  public static class Builder {
    @Nullable private String service;
    @Nullable private String projectId;
    private final Collection<String> versions;

    private Builder(Collection<String> versions) {
      Preconditions.checkNotNull(versions);
      Preconditions.checkArgument(versions.size() != 0);

      this.versions = versions;
    }

    public Builder service(@Nullable String service) {
      this.service = service;
      return this;
    }

    public Builder projectId(@Nullable String projectId) {
      this.projectId = projectId;
      return this;
    }

    public VersionsSelectionConfiguration build() {
      return new VersionsSelectionConfiguration(service, projectId, versions);
    }
  }
}
