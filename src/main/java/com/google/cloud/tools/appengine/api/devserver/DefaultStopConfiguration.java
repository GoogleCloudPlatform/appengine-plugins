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

/** Plain Java bean implementation of {@link StopConfiguration}. */
public class DefaultStopConfiguration implements StopConfiguration {

  @Nullable private String adminHost;
  @Nullable private Integer adminPort;

  @Override
  @Nullable
  public String getAdminHost() {
    return adminHost;
  }

  public void setAdminHost(String adminHost) {
    this.adminHost = adminHost;
  }

  @Override
  @Nullable
  public Integer getAdminPort() {
    return adminPort;
  }

  public void setAdminPort(Integer adminPort) {
    this.adminPort = adminPort;
  }
}
