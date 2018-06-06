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

/** Plain Java bean implementation of {@link StageStandardConfiguration}. */
public class DefaultStageStandardConfiguration implements StageStandardConfiguration {

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

  public DefaultStageStandardConfiguration(File sourceDirectory, File stagingDirectory) {
    this.sourceDirectory = Preconditions.checkNotNull(sourceDirectory);
    this.stagingDirectory = Preconditions.checkNotNull(stagingDirectory);
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
  @Nullable
  public File getDockerfile() {
    return dockerfile;
  }

  public void setDockerfile(@Nullable File dockerfile) {
    this.dockerfile = dockerfile;
  }

  @Override
  @Nullable
  public Boolean getEnableQuickstart() {
    return enableQuickstart;
  }

  public void setEnableQuickstart(@Nullable Boolean enableQuickstart) {
    this.enableQuickstart = enableQuickstart;
  }

  @Override
  @Nullable
  public Boolean getDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  public void setDisableUpdateCheck(@Nullable Boolean disableUpdateCheck) {
    this.disableUpdateCheck = disableUpdateCheck;
  }

  @Override
  @Nullable
  public Boolean getEnableJarSplitting() {
    return enableJarSplitting;
  }

  public void setEnableJarSplitting(@Nullable Boolean enableJarSplitting) {
    this.enableJarSplitting = enableJarSplitting;
  }

  @Override
  @Nullable
  public String getJarSplittingExcludes() {
    return jarSplittingExcludes;
  }

  public void setJarSplittingExcludes(@Nullable String jarSplittingExcludes) {
    this.jarSplittingExcludes = jarSplittingExcludes;
  }

  @Override
  @Nullable
  public String getCompileEncoding() {
    return compileEncoding;
  }

  public void setCompileEncoding(@Nullable String compileEncoding) {
    this.compileEncoding = compileEncoding;
  }

  @Override
  @Nullable
  public Boolean getDeleteJsps() {
    return deleteJsps;
  }

  public void setDeleteJsps(@Nullable Boolean deleteJsps) {
    this.deleteJsps = deleteJsps;
  }

  @Override
  @Nullable
  public Boolean getEnableJarClasses() {
    return enableJarClasses;
  }

  public void setEnableJarClasses(@Nullable Boolean enableJarClasses) {
    this.enableJarClasses = enableJarClasses;
  }

  @Override
  @Nullable
  public Boolean getDisableJarJsps() {
    return disableJarJsps;
  }

  public void setDisableJarJsps(@Nullable Boolean disableJarJsps) {
    this.disableJarJsps = disableJarJsps;
  }

  @Override
  @Nullable
  public String getRuntime() {
    return runtime;
  }

  public void setRuntime(@Nullable String runtime) {
    this.runtime = runtime;
  }
}
