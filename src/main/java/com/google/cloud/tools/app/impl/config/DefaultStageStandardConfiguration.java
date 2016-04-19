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
package com.google.cloud.tools.app.impl.config;

import com.google.cloud.tools.app.api.deploy.StageStandardConfiguration;

import java.io.File;

/**
 * Plain Java bean implementation of {@link StageStandardConfiguration}
 */
public class DefaultStageStandardConfiguration implements StageStandardConfiguration {

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
  public boolean isEnableQuickstart() {
    return enableQuickstart;
  }

  public void setEnableQuickstart(boolean enableQuickstart) {
    this.enableQuickstart = enableQuickstart;
  }

  @Override
  public boolean isDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  public void setDisableUpdateCheck(boolean disableUpdateCheck) {
    this.disableUpdateCheck = disableUpdateCheck;
  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  @Override
  public boolean isEnableJarSplitting() {
    return enableJarSplitting;
  }

  public void setEnableJarSplitting(boolean enableJarSplitting) {
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
  public boolean isDeleteJsps() {
    return deleteJsps;
  }

  public void setDeleteJsps(boolean deleteJsps) {
    this.deleteJsps = deleteJsps;
  }

  @Override
  public boolean isEnableJarClasses() {
    return enableJarClasses;
  }

  public void setEnableJarClasses(boolean enableJarClasses) {
    this.enableJarClasses = enableJarClasses;
  }
}
