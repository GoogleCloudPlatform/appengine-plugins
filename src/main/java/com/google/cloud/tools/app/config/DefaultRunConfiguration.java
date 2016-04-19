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

import com.google.appengine.repackaged.com.google.api.client.util.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link RunConfiguration}.
 */
public class DefaultRunConfiguration implements RunConfiguration {

  private final List<File> appYamls;
  private final boolean async;
  private final String host;
  private final Integer port;
  private final String adminHost;
  private final Integer adminPort;
  private final String authDomain;
  private final String storagePath;
  private final String logLevel;
  private final Integer maxModuleInstances;
  private final boolean useMtimeFileWatcher;
  private final String threadsafeOverride;
  private final String pythonStartupScript;
  private final String pythonStartupArgs;
  private final List<String> jvmFlags;
  private final String customEntrypoint;
  private final String runtime;
  private final boolean allowSkippedFiles;
  private final Integer apiPort;
  private final boolean automaticRestart;
  private final String devAppserverLogLevel;
  private final boolean skipSdkUpdateCheck;
  private final String defaultGcsBucketName;

  private DefaultRunConfiguration(List<File> appYamls, boolean async,
      @Nullable String host, @Nullable Integer port, @Nullable String adminHost,
      @Nullable Integer adminPort, @Nullable String authDomain, @Nullable String storagePath,
      @Nullable String logLevel, @Nullable Integer maxModuleInstances,
      boolean useMtimeFileWatcher, @Nullable String threadsafeOverride,
      @Nullable String pythonStartupScript, @Nullable String pythonStartupArgs,
      @Nullable List<String> jvmFlags, @Nullable String customEntrypoint,
      @Nullable String runtime, boolean allowSkippedFiles, @Nullable Integer apiPort,
      boolean automaticRestart, @Nullable String devAppserverLogLevel,
      boolean skipSdkUpdateCheck, @Nullable String defaultGcsBucketName) {
    this.appYamls = appYamls;
    this.async = async;
    this.host = host;
    this.port = port;
    this.adminHost = adminHost;
    this.adminPort = adminPort;
    this.authDomain = authDomain;
    this.storagePath = storagePath;
    this.logLevel = logLevel;
    this.maxModuleInstances = maxModuleInstances;
    this.useMtimeFileWatcher = useMtimeFileWatcher;
    this.threadsafeOverride = threadsafeOverride;
    this.pythonStartupScript = pythonStartupScript;
    this.pythonStartupArgs = pythonStartupArgs;
    this.jvmFlags = jvmFlags;
    this.customEntrypoint = customEntrypoint;
    this.runtime = runtime;
    this.allowSkippedFiles = allowSkippedFiles;
    this.apiPort = apiPort;
    this.automaticRestart = automaticRestart;
    this.devAppserverLogLevel = devAppserverLogLevel;
    this.skipSdkUpdateCheck = skipSdkUpdateCheck;
    this.defaultGcsBucketName = defaultGcsBucketName;
  }

  @Override
  public List<File> getAppYamls() {
    return appYamls;
  }

  @Override
  public boolean isRunAsync() {
    return async;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Override
  public String getAdminHost() {
    return adminHost;
  }

  @Override
  public Integer getAdminPort() {
    return adminPort;
  }

  @Override
  public String getAuthDomain() {
    return authDomain;
  }

  @Override
  public String getStoragePath() {
    return storagePath;
  }

  @Override
  public String getLogLevel() {
    return logLevel;
  }

  @Override
  public Integer getMaxModuleInstances() {
    return maxModuleInstances;
  }

  @Override
  public boolean isUseMtimeFileWatcher() {
    return useMtimeFileWatcher;
  }

  @Override
  public String getThreadsafeOverride() {
    return threadsafeOverride;
  }

  @Override
  public String getPythonStartupScript() {
    return pythonStartupScript;
  }

  @Override
  public String getPythonStartupArgs() {
    return pythonStartupArgs;
  }

  @Override
  public List<String> getJvmFlags() {
    return jvmFlags;
  }

  @Override
  public String getCustomEntrypoint() {
    return customEntrypoint;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  @Override
  public boolean isAllowSkippedFiles() {
    return allowSkippedFiles;
  }

  @Override
  public Integer getApiPort() {
    return apiPort;
  }

  @Override
  public boolean isAutomaticRestart() {
    return automaticRestart;
  }

  @Override
  public String getDevAppserverLogLevel() {
    return devAppserverLogLevel;
  }

  @Override
  public boolean isSkipSdkUpdateCheck() {
    return skipSdkUpdateCheck;
  }

  @Override
  public String getDefaultGcsBucketName() {
    return defaultGcsBucketName;
  }

  public static Builder newBuilder(File... appYamls) {
    Preconditions.checkArgument(appYamls.length > 0);

    return new Builder(ImmutableList.copyOf(appYamls));
  }

  public static class Builder {
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

    private Builder(List<File> appYamls) {
      this.appYamls = appYamls;
    }

    public Builder async(boolean async) {
      this.async = async;
      return this;
    }

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder port(Integer port) {
      this.port = port;
      return this;
    }

    public Builder adminHost(String adminHost) {
      this.adminHost = adminHost;
      return this;
    }

    public Builder adminPort(Integer adminPort) {
      this.adminPort = adminPort;
      return this;
    }

    public Builder authDomain(String authDomain) {
      this.authDomain = authDomain;
      return this;
    }

    public Builder storagePath(String storagePath) {
      this.storagePath = storagePath;
      return this;
    }

    public Builder logLevel(String logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    public Builder maxModuleInstances(Integer maxModuleInstances) {
      this.maxModuleInstances = maxModuleInstances;
      return this;
    }

    public Builder useMtimeFileWatcher(boolean useMtimeFileWatcher) {
      this.useMtimeFileWatcher = useMtimeFileWatcher;
      return this;
    }

    public Builder threadsafeOverride(String threadsafeOverride) {
      this.threadsafeOverride = threadsafeOverride;
      return this;
    }

    public Builder pythonStartupScript(String pythonStartupScript) {
      this.pythonStartupScript = pythonStartupScript;
      return this;
    }

    public Builder pythonStartupArgs(String pythonStartupArgs) {
      this.pythonStartupArgs = pythonStartupArgs;
      return this;
    }

    public Builder jvmFlags(List<String> jvmFlags) {
      this.jvmFlags = jvmFlags;
      return this;
    }

    public Builder customEntrypoint(String customEntrypoint) {
      this.customEntrypoint = customEntrypoint;
      return this;
    }

    public Builder runtime(String runtime) {
      this.runtime = runtime;
      return this;
    }

    public Builder allowSkippedFiles(boolean allowSkippedFiles) {
      this.allowSkippedFiles = allowSkippedFiles;
      return this;
    }

    public Builder apiPort(Integer apiPort) {
      this.apiPort = apiPort;
      return this;
    }

    public Builder automaticRestart(boolean automaticRestart) {
      this.automaticRestart = automaticRestart;
      return this;
    }

    public Builder devAppserverLogLevel(String devAppserverLogLevel) {
      this.devAppserverLogLevel = devAppserverLogLevel;
      return this;
    }

    public Builder skipSdkUpdateCheck(boolean skipSdkUpdateCheck) {
      this.skipSdkUpdateCheck = skipSdkUpdateCheck;
      return this;
    }

    public Builder defaultGcsBucketName(String defaultGcsBucketName) {
      this.defaultGcsBucketName = defaultGcsBucketName;
      return this;
    }

    public RunConfiguration build() {
      return new DefaultRunConfiguration(appYamls, async, host, port, adminHost, adminPort,
          authDomain, storagePath, logLevel, maxModuleInstances, useMtimeFileWatcher,
          threadsafeOverride, pythonStartupScript, pythonStartupArgs, jvmFlags, customEntrypoint,
          runtime, allowSkippedFiles, apiPort, automaticRestart, devAppserverLogLevel,
          skipSdkUpdateCheck, defaultGcsBucketName);
    }
  }
}
