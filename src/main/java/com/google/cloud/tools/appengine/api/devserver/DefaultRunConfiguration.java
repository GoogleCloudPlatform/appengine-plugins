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

package com.google.cloud.tools.appengine.api.devserver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Plain Java bean implementation of {@link RunConfiguration}. */
public class DefaultRunConfiguration implements RunConfiguration {

  @Nullable private List<File> services;
  @Nullable private String host;
  @Nullable private Integer port;
  @Nullable private String adminHost;
  @Nullable private Integer adminPort;
  @Nullable private String authDomain;
  @Nullable private File storagePath;
  @Nullable private String logLevel;
  @Nullable private Integer maxModuleInstances;
  @Nullable private Boolean useMtimeFileWatcher;
  @Nullable private String threadsafeOverride;
  @Nullable private String pythonStartupScript;
  @Nullable private String pythonStartupArgs;
  @Nullable private List<String> jvmFlags;
  @Nullable private String customEntrypoint;
  @Nullable private String runtime;
  @Nullable private Boolean allowSkippedFiles;
  @Nullable private Integer apiPort;
  @Nullable private Boolean automaticRestart;
  @Nullable private String devAppserverLogLevel;
  @Nullable private Boolean skipSdkUpdateCheck;
  @Nullable private String defaultGcsBucketName;
  @Nullable private Boolean clearDatastore;
  @Nullable private File datastorePath;
  @Nullable private Map<String, String> environment;
  @Nullable private List<String> additionalArguments;
  @Nullable private String projectId;

  @Override
  @Nullable
  public List<File> getServices() {
    return services;
  }

  public void setServices(List<File> services) {
    this.services = services;
  }

  @Override
  @Nullable
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
  @Nullable
  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  @Override
  @Nullable
  public String getAdminHost() {
    return adminHost;
  }

  public void setAdminHost(String adminHost) {
    this.adminHost = adminHost;
  }

  @Override
  @Nullable
  public Integer getAdminPort() {
    return adminPort;
  }

  public void setAdminPort(Integer adminPort) {
    this.adminPort = adminPort;
  }

  @Override
  @Nullable
  public String getAuthDomain() {
    return authDomain;
  }

  public void setAuthDomain(String authDomain) {
    this.authDomain = authDomain;
  }

  @Override
  @Nullable
  public File getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(File storagePath) {
    this.storagePath = storagePath;
  }

  @Override
  @Nullable
  public String getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }

  @Override
  @Nullable
  public Integer getMaxModuleInstances() {
    return maxModuleInstances;
  }

  public void setMaxModuleInstances(Integer maxModuleInstances) {
    this.maxModuleInstances = maxModuleInstances;
  }

  @Override
  @Nullable
  public Boolean getUseMtimeFileWatcher() {
    return useMtimeFileWatcher;
  }

  public void setUseMtimeFileWatcher(Boolean useMtimeFileWatcher) {
    this.useMtimeFileWatcher = useMtimeFileWatcher;
  }

  @Override
  @Nullable
  public String getThreadsafeOverride() {
    return threadsafeOverride;
  }

  public void setThreadsafeOverride(String threadsafeOverride) {
    this.threadsafeOverride = threadsafeOverride;
  }

  @Override
  @Nullable
  public String getPythonStartupScript() {
    return pythonStartupScript;
  }

  public void setPythonStartupScript(String pythonStartupScript) {
    this.pythonStartupScript = pythonStartupScript;
  }

  @Override
  @Nullable
  public String getPythonStartupArgs() {
    return pythonStartupArgs;
  }

  public void setPythonStartupArgs(String pythonStartupArgs) {
    this.pythonStartupArgs = pythonStartupArgs;
  }

  @Override
  @Nullable
  public List<String> getJvmFlags() {
    return jvmFlags;
  }

  public void setJvmFlags(List<String> jvmFlags) {
    this.jvmFlags = jvmFlags != null ? ImmutableList.copyOf(jvmFlags) : null;
  }

  @Override
  @Nullable
  public String getCustomEntrypoint() {
    return customEntrypoint;
  }

  public void setCustomEntrypoint(String customEntrypoint) {
    this.customEntrypoint = customEntrypoint;
  }

  @Override
  @Nullable
  public String getRuntime() {
    return runtime;
  }

  public void setRuntime(String runtime) {
    this.runtime = runtime;
  }

  @Override
  @Nullable
  public Boolean getAllowSkippedFiles() {
    return allowSkippedFiles;
  }

  public void setAllowSkippedFiles(Boolean allowSkippedFiles) {
    this.allowSkippedFiles = allowSkippedFiles;
  }

  @Override
  @Nullable
  public Integer getApiPort() {
    return apiPort;
  }

  public void setApiPort(Integer apiPort) {
    this.apiPort = apiPort;
  }

  @Override
  @Nullable
  public Boolean getAutomaticRestart() {
    return automaticRestart;
  }

  public void setAutomaticRestart(Boolean automaticRestart) {
    this.automaticRestart = automaticRestart;
  }

  @Override
  @Nullable
  public String getDevAppserverLogLevel() {
    return devAppserverLogLevel;
  }

  public void setDevAppserverLogLevel(String devAppserverLogLevel) {
    this.devAppserverLogLevel = devAppserverLogLevel;
  }

  @Override
  @Nullable
  public Boolean getSkipSdkUpdateCheck() {
    return skipSdkUpdateCheck;
  }

  public void setSkipSdkUpdateCheck(Boolean skipSdkUpdateCheck) {
    this.skipSdkUpdateCheck = skipSdkUpdateCheck;
  }

  @Override
  @Nullable
  public String getDefaultGcsBucketName() {
    return defaultGcsBucketName;
  }

  public void setDefaultGcsBucketName(String defaultGcsBucketName) {
    this.defaultGcsBucketName = defaultGcsBucketName;
  }

  @Override
  @Nullable
  public Boolean getClearDatastore() {
    return clearDatastore;
  }

  public void setClearDatastore(Boolean clearDatastore) {
    this.clearDatastore = clearDatastore;
  }

  @Override
  @Nullable
  public File getDatastorePath() {
    return datastorePath;
  }

  public void setDatastorePath(File datastorePath) {
    this.datastorePath = datastorePath;
  }

  @Override
  @Nullable
  public Map<String, String> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Map<String, String> environment) {
    this.environment = environment != null ? ImmutableMap.copyOf(environment) : null;
  }

  @Override
  @Nullable
  public List<String> getAdditionalArguments() {
    return additionalArguments;
  }

  public void setAdditionalArguments(List<String> additionalArguments) {
    this.additionalArguments =
        additionalArguments != null ? ImmutableList.copyOf(additionalArguments) : null;
  }

  /** Gets the GCP project ID. */
  @Override
  @Nullable
  public String getProjectId() {
    return projectId;
  }

  /** Sets the GCP project ID. */
  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }
}
