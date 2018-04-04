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

import java.io.File;

/** Plain Java bean implementation of {@link StageStandardConfiguration}. */
public class DefaultStageStandardConfiguration implements StageStandardConfiguration {

  private File sourceDirectory;
  private File stagingDirectory;
  private File dockerfile;
  private Boolean enableQuickstart;
  private Boolean disableUpdateCheck;
  private Boolean enableJarSplitting;
  private String jarSplittingExcludes;
  private String compileEncoding;
  private Boolean deleteJsps;
  private Boolean enableJarClasses;
  private Boolean disableJarJsps;
  private String runtime;

  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  @Override
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  public void setStagingDirectory(File stagingDirectory) {
    this.stagingDirectory = stagingDirectory;
  }

  @Override
  public File getDockerfile() {
    return dockerfile;
  }

  public void setDockerfile(File dockerfile) {
    this.dockerfile = dockerfile;
  }

  @Override
  public Boolean getEnableQuickstart() {
    return enableQuickstart;
  }

  public void setEnableQuickstart(Boolean enableQuickstart) {
    this.enableQuickstart = enableQuickstart;
  }

  @Override
  public Boolean getDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  public void setDisableUpdateCheck(Boolean disableUpdateCheck) {
    this.disableUpdateCheck = disableUpdateCheck;
  }

  @Override
  public Boolean getEnableJarSplitting() {
    return enableJarSplitting;
  }

  public void setEnableJarSplitting(Boolean enableJarSplitting) {
    this.enableJarSplitting = enableJarSplitting;
  }

  @Override
  public String getJarSplittingExcludes() {
    return jarSplittingExcludes;
  }

  public void setJarSplittingExcludes(String jarSplittingExcludes) {
    this.jarSplittingExcludes = jarSplittingExcludes;
  }

  @Override
  public String getCompileEncoding() {
    return compileEncoding;
  }

  public void setCompileEncoding(String compileEncoding) {
    this.compileEncoding = compileEncoding;
  }

  @Override
  public Boolean getDeleteJsps() {
    return deleteJsps;
  }

  public void setDeleteJsps(Boolean deleteJsps) {
    this.deleteJsps = deleteJsps;
  }

  @Override
  public Boolean getEnableJarClasses() {
    return enableJarClasses;
  }

  public void setEnableJarClasses(Boolean enableJarClasses) {
    this.enableJarClasses = enableJarClasses;
  }

  @Override
  public Boolean getDisableJarJsps() {
    return disableJarJsps;
  }

  public void setDisableJarJsps(Boolean disableJarJsps) {
    this.disableJarJsps = disableJarJsps;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  public void setRuntime(String runtime) {
    this.runtime = runtime;
  }
}
