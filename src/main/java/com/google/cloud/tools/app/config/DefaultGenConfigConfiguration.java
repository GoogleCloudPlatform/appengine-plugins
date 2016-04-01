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


import java.io.File;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link GenConfigConfiguration}.
 */
public class DefaultGenConfigConfiguration implements GenConfigConfiguration {

  private final File sourceDirectory;
  private final String config;
  private final boolean custom;
  private final String runtime;

  private DefaultGenConfigConfiguration(File sourceDirectory, @Nullable String config,
      boolean custom, @Nullable String runtime) {
    this.sourceDirectory = sourceDirectory;
    this.config = config;
    this.custom = custom;
    this.runtime = runtime;
  }

  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  @Override
  public String getConfig() {
    return config;
  }

  @Override
  public boolean isCustom() {
    return custom;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  public static Builder newBuilder(File sourceDirectory) {
    return new Builder(sourceDirectory);
  }

  public static class Builder {
    private File sourceDirectory;
    private String config;
    private boolean custom;
    private String runtime;

    public Builder (File sourceDirectory) {
      this.sourceDirectory = sourceDirectory;
    }

    public Builder config(String config) {
      this.config = config;
      return this;
    }

    public Builder custom(boolean custom) {
      this.custom = custom;
      return this;
    }

    public Builder runtime(String runtime) {
      this.runtime = runtime;
      return this;
    }

    public GenConfigConfiguration build() {
      return new DefaultGenConfigConfiguration(sourceDirectory, config, custom, runtime);
    }
  }
}
