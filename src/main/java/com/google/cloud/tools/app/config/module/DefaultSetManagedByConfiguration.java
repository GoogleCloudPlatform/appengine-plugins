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
package com.google.cloud.tools.app.config.module;

import com.google.cloud.tools.app.action.module.SetManagedByAction.Manager;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link SetManagedByConfiguration}.
 */
public class DefaultSetManagedByConfiguration implements SetManagedByConfiguration {

  private final Collection<String> modules;
  private final String version;
  private final Manager manager;
  private final String instance;
  private final String server;

  private DefaultSetManagedByConfiguration(Collection<String> modules, String version,
      @Nullable Manager manager, @Nullable String instance, @Nullable String server) {
    this.modules = modules;
    this.version = version;
    this.manager = manager;
    this.instance = instance;
    this.server = server;
  }

  public Collection<String> getModules() {
    return modules;
  }

  public String getVersion() {
    return version;
  }

  public Manager getManager() {
    return manager;
  }

  public String getInstance() {
    return instance;
  }

  public String getServer() {
    return server;
  }

  public static Builder newBuilder(String version, Manager manager, String... modules) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(version));
    Preconditions.checkArgument(modules.length > 0);
    Preconditions.checkNotNull(manager);

    return new Builder(ImmutableList.copyOf(modules), version);
  }

  public static class Builder {
    private Collection<String> modules;
    private String version;
    private Manager manager;
    private String instance;
    private String server;

    private Builder(Collection<String> modules, String version) {
      this.modules = modules;
      this.version = version;
    }

    public Builder manager(Manager manager) {
      this.manager = manager;
      return this;
    }

    public Builder instance(String instance) {
      this.instance = instance;
      return this;
    }

    public Builder server(String server) {
      this.server = server;
      return this;
    }

    public SetManagedByConfiguration build() {
      return new DefaultSetManagedByConfiguration(modules, version, manager, instance, server);
    }
  }
}
