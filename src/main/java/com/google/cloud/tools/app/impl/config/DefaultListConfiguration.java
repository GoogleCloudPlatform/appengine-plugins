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

package com.google.cloud.tools.app.impl.config;

import com.google.cloud.tools.app.api.module.ListConfiguration;

import java.util.Collection;

/**
 * Plain Java bean implementation of {@link ListConfiguration}
 */
public class DefaultListConfiguration implements ListConfiguration {

  private Collection<String> modules;
  private String server;

  @Override
  public Collection<String> getModules() {
    return modules;
  }

  public void setModules(Collection<String> modules) {
    this.modules = modules;
  }

  @Override
  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }
}
