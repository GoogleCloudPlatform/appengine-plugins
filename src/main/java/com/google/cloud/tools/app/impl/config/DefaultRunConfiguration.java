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

import com.google.cloud.tools.app.api.devserver.RunConfiguration;

import java.io.File;
import java.util.List;

/**
 * Plain Java bean implementation of {@link RunConfiguration}.
 */
public class DefaultRunConfiguration implements RunConfiguration {

  private List<File> appYamls;
  private boolean async;
  private String host;
  private Integer port;
  private String adminHost;
  private Integer adminPort;
  private String authDomain;
  private String storagePath;
  private String logLevel;
  private Integer maxModuleInstances;
  private boolean useMtimeFileWatcher;
  private String threadsafeOverride;
  private String pythonStartupScript;
  private String pythonStartupArgs;
  private List<String> jvmFlags;
  private String customEntrypoint;
  private String runtime;
  private boolean allowSkippedFiles;
  private Integer apiPort;
  private boolean automaticRestart;
  private String devAppserverLogLevel;
  private boolean skipSdkUpdateCheck;
  private String defaultGcsBucketName;

  @Override
  public List<File> getAppYamls() {
    return appYamls;
  }

  public void setAppYamls(List<File> appYamls) {
    this.appYamls = appYamls;
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  @Override
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  @Override
  public String getAdminHost() {
    return adminHost;
  }

  public void setAdminHost(String adminHost) {
    this.adminHost = adminHost;
  }

  @Override
  public Integer getAdminPort() {
    return adminPort;
  }

  public void setAdminPort(Integer adminPort) {
    this.adminPort = adminPort;
  }

  @Override
  public String getAuthDomain() {
    return authDomain;
  }

  public void setAuthDomain(String authDomain) {
    this.authDomain = authDomain;
  }

  @Override
  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }

  @Override
  public String getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }

  @Override
  public Integer getMaxModuleInstances() {
    return maxModuleInstances;
  }

  public void setMaxModuleInstances(Integer maxModuleInstances) {
    this.maxModuleInstances = maxModuleInstances;
  }

  @Override
  public boolean isUseMtimeFileWatcher() {
    return useMtimeFileWatcher;
  }

  public void setUseMtimeFileWatcher(boolean useMtimeFileWatcher) {
    this.useMtimeFileWatcher = useMtimeFileWatcher;
  }

  @Override
  public String getThreadsafeOverride() {
    return threadsafeOverride;
  }

  public void setThreadsafeOverride(String threadsafeOverride) {
    this.threadsafeOverride = threadsafeOverride;
  }

  @Override
  public String getPythonStartupScript() {
    return pythonStartupScript;
  }

  public void setPythonStartupScript(String pythonStartupScript) {
    this.pythonStartupScript = pythonStartupScript;
  }

  @Override
  public String getPythonStartupArgs() {
    return pythonStartupArgs;
  }

  public void setPythonStartupArgs(String pythonStartupArgs) {
    this.pythonStartupArgs = pythonStartupArgs;
  }

  @Override
  public List<String> getJvmFlags() {
    return jvmFlags;
  }

  public void setJvmFlags(List<String> jvmFlags) {
    this.jvmFlags = jvmFlags;
  }

  @Override
  public String getCustomEntrypoint() {
    return customEntrypoint;
  }

  public void setCustomEntrypoint(String customEntrypoint) {
    this.customEntrypoint = customEntrypoint;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  public void setRuntime(String runtime) {
    this.runtime = runtime;
  }

  @Override
  public boolean isAllowSkippedFiles() {
    return allowSkippedFiles;
  }

  public void setAllowSkippedFiles(boolean allowSkippedFiles) {
    this.allowSkippedFiles = allowSkippedFiles;
  }

  @Override
  public Integer getApiPort() {
    return apiPort;
  }

  public void setApiPort(Integer apiPort) {
    this.apiPort = apiPort;
  }

  @Override
  public boolean isAutomaticRestart() {
    return automaticRestart;
  }

  public void setAutomaticRestart(boolean automaticRestart) {
    this.automaticRestart = automaticRestart;
  }

  @Override
  public String getDevAppserverLogLevel() {
    return devAppserverLogLevel;
  }

  public void setDevAppserverLogLevel(String devAppserverLogLevel) {
    this.devAppserverLogLevel = devAppserverLogLevel;
  }

  @Override
  public boolean isSkipSdkUpdateCheck() {
    return skipSdkUpdateCheck;
  }

  public void setSkipSdkUpdateCheck(boolean skipSdkUpdateCheck) {
    this.skipSdkUpdateCheck = skipSdkUpdateCheck;
  }

  @Override
  public String getDefaultGcsBucketName() {
    return defaultGcsBucketName;
  }

  public void setDefaultGcsBucketName(String defaultGcsBucketName) {
    this.defaultGcsBucketName = defaultGcsBucketName;
  }
}
