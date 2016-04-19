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

import javax.annotation.Nullable;

/**
 * Default implementation of {@link StopConfiguration}.
 */
public class DefaultStopConfiguration implements StopConfiguration {

  private final String adminHost;
  private final Integer adminPort;

  private DefaultStopConfiguration(@Nullable String adminHost, @Nullable Integer adminPort) {
    this.adminHost = adminHost;
    this.adminPort = adminPort;
  }

  @Override
  public String getAdminHost() {
    return adminHost;
  }

  @Override
  public Integer getAdminPort() {
    return adminPort;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String adminHost;
    private Integer adminPort;

    public Builder adminHost(String adminHost) {
      this.adminHost = adminHost;
      return this;
    }

    public Builder adminPort(Integer adminPort) {
      this.adminPort = adminPort;
      return this;
    }

    public StopConfiguration build() {
      return new DefaultStopConfiguration(adminHost, adminPort);
    }
  }
}
