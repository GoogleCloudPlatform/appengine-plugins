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
import java.nio.file.Path;
import javax.annotation.Nullable;

/**
 * Arguments needed to stage an App Engine appengine-web.xml based application. Null return values
 * indicate that the configuration was not set, and thus assumes the tool default value.
 */
public class AppEngineWebXmlProjectStageConfiguration {
  private final Path sourceDirectory;
  private final Path stagingDirectory;
  @Nullable private final Path dockerfile;
  @Nullable private final Boolean enableQuickstart;
  @Nullable private final Boolean disableUpdateCheck;
  @Nullable private final Boolean enableJarSplitting;
  @Nullable private final String jarSplittingExcludes;
  @Nullable private final String compileEncoding;
  @Nullable private final Boolean deleteJsps;
  @Nullable private final Boolean enableJarClasses;
  @Nullable private final Boolean disableJarJsps;
  @Nullable private final String runtime;

  private AppEngineWebXmlProjectStageConfiguration(
      Path sourceDirectory,
      Path stagingDirectory,
      @Nullable Path dockerfile,
      @Nullable Boolean enableQuickstart,
      @Nullable Boolean disableUpdateCheck,
      @Nullable Boolean enableJarSplitting,
      @Nullable String jarSplittingExcludes,
      @Nullable String compileEncoding,
      @Nullable Boolean deleteJsps,
      @Nullable Boolean enableJarClasses,
      @Nullable Boolean disableJarJsps,
      @Nullable String runtime) {
    this.sourceDirectory = sourceDirectory;
    this.stagingDirectory = stagingDirectory;
    this.dockerfile = dockerfile;
    this.enableQuickstart = enableQuickstart;
    this.disableUpdateCheck = disableUpdateCheck;
    this.enableJarSplitting = enableJarSplitting;
    this.jarSplittingExcludes = jarSplittingExcludes;
    this.compileEncoding = compileEncoding;
    this.deleteJsps = deleteJsps;
    this.enableJarClasses = enableJarClasses;
    this.disableJarJsps = disableJarJsps;
    this.runtime = runtime;
  }

  /** The exploded war directory to stage from. */
  public Path getSourceDirectory() {
    return sourceDirectory;
  }

  /** The staging output directory. */
  public Path getStagingDirectory() {
    return stagingDirectory;
  }

  /** A dockerfile to copy into the staging directory. */
  @Nullable
  public Path getDockerfile() {
    return dockerfile;
  }

  @Nullable
  public Boolean getEnableQuickstart() {
    return enableQuickstart;
  }

  @Nullable
  public Boolean getDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  @Nullable
  public Boolean getEnableJarSplitting() {
    return enableJarSplitting;
  }

  @Nullable
  public String getJarSplittingExcludes() {
    return jarSplittingExcludes;
  }

  @Nullable
  public String getCompileEncoding() {
    return compileEncoding;
  }

  @Nullable
  public Boolean getDeleteJsps() {
    return deleteJsps;
  }

  @Nullable
  public Boolean getEnableJarClasses() {
    return enableJarClasses;
  }

  @Nullable
  public Boolean getDisableJarJsps() {
    return disableJarJsps;
  }

  /** Override of the runtime in the generated app.yaml */
  @Nullable
  public String getRuntime() {
    return runtime;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * 
   * Create a new builder for AppEngineWebXmlProjectStageConfiguration.
   * 
   * @deprecated Use
   *     newBuilder().sourceDirectory(sourceDirectory).stagingDirectory(stagingDirectory) instead.
   */
  @Deprecated
  public static Builder builder(Path sourceDirectory, Path stagingDirectory) {
    return new Builder().sourceDirectory(sourceDirectory).stagingDirectory(stagingDirectory);
  }

  public static final class Builder {
    private Path sourceDirectory;
    private Path stagingDirectory;
    @Nullable private Path dockerfile;
    @Nullable private Boolean enableQuickstart;
    @Nullable private Boolean disableUpdateCheck;
    @Nullable private Boolean enableJarSplitting;
    @Nullable private String jarSplittingExcludes;
    @Nullable private String compileEncoding;
    @Nullable private Boolean deleteJsps;
    @Nullable private Boolean enableJarClasses;
    @Nullable private Boolean disableJarJsps;
    @Nullable private String runtime;

    public Builder dockerfile(@Nullable Path dockerfile) {
      this.dockerfile = dockerfile;
      return this;
    }

    public Builder enableQuickstart(@Nullable Boolean enableQuickstart) {
      this.enableQuickstart = enableQuickstart;
      return this;
    }

    public Builder disableUpdateCheck(@Nullable Boolean disableUpdateCheck) {
      this.disableUpdateCheck = disableUpdateCheck;
      return this;
    }

    public Builder enableJarSplitting(@Nullable Boolean enableJarSplitting) {
      this.enableJarSplitting = enableJarSplitting;
      return this;
    }

    public Builder jarSplittingExcludes(@Nullable String jarSplittingExcludes) {
      this.jarSplittingExcludes = jarSplittingExcludes;
      return this;
    }

    public Builder compileEncoding(@Nullable String compileEncoding) {
      this.compileEncoding = compileEncoding;
      return this;
    }

    public Builder deleteJsps(@Nullable Boolean deleteJsps) {
      this.deleteJsps = deleteJsps;
      return this;
    }

    public Builder enableJarClasses(@Nullable Boolean enableJarClasses) {
      this.enableJarClasses = enableJarClasses;
      return this;
    }

    public Builder disableJarJsps(@Nullable Boolean disableJarJsps) {
      this.disableJarJsps = disableJarJsps;
      return this;
    }

    public Builder runtime(@Nullable String runtime) {
      this.runtime = runtime;
      return this;
    }

    public Builder sourceDirectory(Path sourceDirectory) {
      this.sourceDirectory = Preconditions.checkNotNull(sourceDirectory);
      return this;
    }

    public Builder stagingDirectory(Path stagingDirectory) {
      this.stagingDirectory = Preconditions.checkNotNull(stagingDirectory);
      return this;
    }

    /** Build a {@link AppEngineWebXmlProjectStageConfiguration}. */
    public AppEngineWebXmlProjectStageConfiguration build() {
      Preconditions.checkState(sourceDirectory != null, "No source directory supplied");
      Preconditions.checkState(stagingDirectory != null, "No staging directory supplied");

      return new AppEngineWebXmlProjectStageConfiguration(
          this.sourceDirectory,
          this.stagingDirectory,
          this.dockerfile,
          this.enableQuickstart,
          this.disableUpdateCheck,
          this.enableJarSplitting,
          this.jarSplittingExcludes,
          this.compileEncoding,
          this.deleteJsps,
          this.enableJarClasses,
          this.disableJarJsps,
          this.runtime);
    }
  }
}
