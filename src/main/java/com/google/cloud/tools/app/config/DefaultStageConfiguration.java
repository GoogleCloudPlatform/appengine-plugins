/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.app.config;

import com.google.common.base.Preconditions;

import java.io.File;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link StageConfiguration}.
 */
public class DefaultStageConfiguration implements StageConfiguration {

  private final File sourceDirectory;
  private final File stagingDirectory;
  private final File dockerfile;
  private final boolean enableQuickstart;
  private final boolean disableUpdateCheck;
  private final String version;
  private final String applicationId;
  private final boolean enableJarSplitting;
  private final String jarSplittingExcludes;
  private final String compileEncoding;
  private final boolean deleteJsps;
  private final boolean enableJarClasses;

  private DefaultStageConfiguration(File sourceDirectory, File stagingDirectory,
      @Nullable File dockerfile, boolean enableQuickstart, boolean disableUpdateCheck,
      @Nullable String version, @Nullable String applicationId, boolean enableJarSplitting,
      @Nullable String jarSplittingExcludes, @Nullable String compileEncoding, boolean deleteJsps,
      boolean enableJarClasses) {
    this.sourceDirectory = sourceDirectory;
    this.stagingDirectory = stagingDirectory;
    this.dockerfile = dockerfile;
    this.enableQuickstart = enableQuickstart;
    this.disableUpdateCheck = disableUpdateCheck;
    this.version = version;
    this.applicationId = applicationId;
    this.enableJarSplitting = enableJarSplitting;
    this.jarSplittingExcludes = jarSplittingExcludes;
    this.compileEncoding = compileEncoding;
    this.deleteJsps = deleteJsps;
    this.enableJarClasses = enableJarClasses;
  }

  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  @Override
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  @Override
  public File getDockerfile() {
    return dockerfile;
  }

  @Override
  public boolean isEnableQuickstart() {
    return enableQuickstart;
  }

  @Override
  public boolean isDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public String getApplicationId() {
    return applicationId;
  }

  @Override
  public boolean isEnableJarSplitting() {
    return enableJarSplitting;
  }

  @Override
  public String getJarSplittingExcludes() {
    return jarSplittingExcludes;
  }

  @Override
  public String getCompileEncoding() {
    return compileEncoding;
  }

  @Override
  public boolean isDeleteJsps() {
    return deleteJsps;
  }

  @Override
  public boolean isEnableJarClasses() {
    return enableJarClasses;
  }

  public static Builder newBuilder(File sourceDirectory, File stagingDirectory) {
    Preconditions.checkNotNull(sourceDirectory);
    Preconditions.checkNotNull(stagingDirectory);

    return new Builder(sourceDirectory, stagingDirectory);
  }

  public static class Builder {

    private File sourceDirectory;
    private File stagingDirectory;
    private File dockerfile;
    private boolean enableQuickstart;
    private boolean disableUpdateCheck;
    private String version;
    private String applicationId;
    private boolean enableJarSplitting;
    private String jarSplittingExcludes;
    private String compileEncoding;
    private boolean deleteJsps;
    private boolean enableJarClasses;

    private Builder(File sourceDirectory, File stagingDirectory) {
      this.sourceDirectory = sourceDirectory;
      this.stagingDirectory = stagingDirectory;
    }

    public Builder dockerfile(File dockerfile) {
      this.dockerfile = dockerfile;
      return this;
    }

    public Builder enableQuickstart(boolean enableQuickstart) {
      this.enableQuickstart = enableQuickstart;
      return this;
    }

    public Builder disableUpdateCheck(boolean disableUpdateCheck) {
      this.disableUpdateCheck = disableUpdateCheck;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder applicationId(String applicationId) {
      this.applicationId = applicationId;
      return this;
    }

    public Builder enableJarSplitting(boolean enableJarSplitting) {
      this.enableJarSplitting = enableJarSplitting;
      return this;
    }

    public Builder jarSplittingExcludes(String jarSplittingExcludes) {
      this.jarSplittingExcludes = jarSplittingExcludes;
      return this;
    }

    public Builder compileEncoding(String compileEncoding) {
      this.compileEncoding = compileEncoding;
      return this;
    }

    public Builder deleteJsps(boolean deleteJsps) {
      this.deleteJsps = deleteJsps;
      return this;
    }

    public Builder enableJarClasses(boolean enableJarClasses) {
      this.enableJarClasses = enableJarClasses;
      return this;
    }

    public StageConfiguration build() {
      return new DefaultStageConfiguration(sourceDirectory, stagingDirectory, dockerfile,
          enableQuickstart, disableUpdateCheck, version, applicationId, enableJarSplitting,
          jarSplittingExcludes, compileEncoding, deleteJsps, enableJarClasses);
    }
  }
}
