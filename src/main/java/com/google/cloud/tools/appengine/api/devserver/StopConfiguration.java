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

package com.google.cloud.tools.appengine.api.devserver;

import javax.annotation.Nullable;

public class StopConfiguration {

  @Nullable private final String adminHost;
  @Nullable private final Integer adminPort;

  public StopConfiguration(@Nullable String adminHost, @Nullable Integer adminPort) {
    this.adminHost = adminHost;
    this.adminPort = adminPort;
  }

  @Nullable
  public String getAdminHost() {
    return adminHost;
  }

  @Nullable
  public Integer getAdminPort() {
    return adminPort;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    @Nullable private String adminHost;
    @Nullable private Integer adminPort;

    public Builder setAdminHost(@Nullable String adminHost) {
      this.adminHost = adminHost;
      return this;
    }

    public Builder setAdminPort(@Nullable Integer adminPort) {
      this.adminPort = adminPort;
      return this;
    }

    public StopConfiguration build() {
      return new StopConfiguration(adminHost, adminPort);
    }
  }
}
