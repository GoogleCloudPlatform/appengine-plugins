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

package com.google.cloud.tools.appengine.api.deploy;

import com.google.common.base.Preconditions;
import java.io.File;
import javax.annotation.Nullable;

/**
 * Arguments needed to stage an App Engine standard environment application. Null return values
 * indicate that the configuration was not set, and thus assumes the tool default value.
 */
public class StageStandardConfiguration {

  private File sourceDirectory;
  private File stagingDirectory;
  @Nullable private File dockerfile;
  @Nullable private Boolean enableQuickstart;
  @Nullable private Boolean disableUpdateCheck;
  @Nullable private Boolean enableJarSplitting;
  @Nullable private String jarSplittingExcludes;
  @Nullable private String compileEncoding;
  @Nullable private Boolean deleteJsps;
  @Nullable private Boolean enableJarClasses;
  @Nullable private Boolean disableJarJsps;
  @Nullable private String runtime;

  private StageStandardConfiguration(File sourceDirectory, File stagingDirectory) {
    this.sourceDirectory = Preconditions.checkNotNull(sourceDirectory);
    this.stagingDirectory = Preconditions.checkNotNull(stagingDirectory);
  }

  public File getSourceDirectory() {
    return sourceDirectory;
  }

  public File getStagingDirectory() {
    return stagingDirectory;
  }

  @Nullable
  public File getDockerfile() {
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

  @Nullable
  public String getRuntime() {
    return runtime;
  }

  public static class Builder {

    @Nullable private File sourceDirectory;
    @Nullable private File stagingDirectory;
    @Nullable private File dockerfile;
    @Nullable private Boolean enableQuickstart;
    @Nullable private Boolean disableUpdateCheck;
    @Nullable private Boolean enableJarSplitting;
    @Nullable private String jarSplittingExcludes;
    @Nullable private String compileEncoding;
    @Nullable private Boolean deleteJsps;
    @Nullable private Boolean enableJarClasses;
    @Nullable private Boolean disableJarJsps;
    @Nullable private String runtime;

    public Builder setSourceDirectory(File sourceDirectory) {
      this.sourceDirectory = Preconditions.checkNotNull(sourceDirectory);
      return this;
    }

    public Builder setStagingDirectory(File stagingDirectory) {
      this.stagingDirectory = Preconditions.checkNotNull(stagingDirectory);
      return this;
    }

    public void setDockerfile(@Nullable File dockerfile) {
      this.dockerfile = dockerfile;
    }

    public void setEnableQuickstart(@Nullable Boolean enableQuickstart) {
      this.enableQuickstart = enableQuickstart;
    }

    public void setDisableUpdateCheck(@Nullable Boolean disableUpdateCheck) {
      this.disableUpdateCheck = disableUpdateCheck;
    }

    public void setEnableJarSplitting(@Nullable Boolean enableJarSplitting) {
      this.enableJarSplitting = enableJarSplitting;
    }

    public void setJarSplittingExcludes(@Nullable String jarSplittingExcludes) {
      this.jarSplittingExcludes = jarSplittingExcludes;
    }

    public void setCompileEncoding(@Nullable String compileEncoding) {
      this.compileEncoding = compileEncoding;
    }

    public void setDeleteJsps(@Nullable Boolean deleteJsps) {
      this.deleteJsps = deleteJsps;
    }

    public void setEnableJarClasses(@Nullable Boolean enableJarClasses) {
      this.enableJarClasses = enableJarClasses;
    }

    public void setDisableJarJsps(@Nullable Boolean disableJarJsps) {
      this.disableJarJsps = disableJarJsps;
    }

    public void setRuntime(@Nullable String runtime) {
      this.runtime = runtime;
    }

    /** Builds the configuration. */
    public StageStandardConfiguration build() {
      if (sourceDirectory == null || stagingDirectory == null) {
        throw new NullPointerException("Incomplete configuration");
      }
      StageStandardConfiguration stageStandardConfiguration =
          new StageStandardConfiguration(sourceDirectory, stagingDirectory);

      stageStandardConfiguration.dockerfile = dockerfile;
      stageStandardConfiguration.enableQuickstart = enableQuickstart;
      stageStandardConfiguration.disableUpdateCheck = disableUpdateCheck;
      stageStandardConfiguration.enableJarSplitting = enableJarSplitting;
      stageStandardConfiguration.jarSplittingExcludes = jarSplittingExcludes;
      stageStandardConfiguration.compileEncoding = compileEncoding;
      stageStandardConfiguration.deleteJsps = deleteJsps;
      stageStandardConfiguration.enableJarClasses = enableJarClasses;
      stageStandardConfiguration.disableJarJsps = disableJarJsps;
      stageStandardConfiguration.runtime = runtime;
      return stageStandardConfiguration;
    }
  }
}
