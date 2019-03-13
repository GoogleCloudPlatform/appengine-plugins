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

import javax.annotation.Nullable;

public class StopConfiguration {

  @Nullable private final String host;
  @Nullable private final Integer port;

  public StopConfiguration(@Nullable String host, @Nullable Integer port) {
    this.host = host;
    this.port = port;
  }

  @Nullable
  public String getHost() {
    return host;
  }

  @Nullable
  public Integer getPort() {
    return port;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    @Nullable private String host;
    @Nullable private Integer port;

    public Builder host(@Nullable String host) {
      this.host = host;
      return this;
    }

    public Builder port(@Nullable Integer port) {
      this.port = port;
      return this;
    }

    public StopConfiguration build() {
      return new StopConfiguration(host, port);
    }
  }
}
