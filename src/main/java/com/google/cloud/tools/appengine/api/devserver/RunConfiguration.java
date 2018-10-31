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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class RunConfiguration {

  private final List<Path> services;
  @Nullable private final String host;
  @Nullable private final Integer port;
  @Nullable private final String adminHost;
  @Nullable private final Integer adminPort;
  @Nullable private final String authDomain;
  @Nullable private final Path storagePath;
  @Nullable private final String logLevel;
  @Nullable private final Integer maxModuleInstances;
  @Nullable private final Boolean useMtimeFileWatcher;
  @Nullable private final String threadsafeOverride;
  @Nullable private final String pythonStartupScript;
  @Nullable private final String pythonStartupArgs;
  @Nullable private final List<String> jvmFlags;
  @Nullable private final String customEntrypoint;
  @Nullable private final String runtime;
  @Nullable private final Boolean allowSkippedFiles;
  @Nullable private final Integer apiPort;
  @Nullable private final Boolean automaticRestart;
  @Nullable private final String devAppserverLogLevel;
  @Nullable private final Boolean skipSdkUpdateCheck;
  @Nullable private final String defaultGcsBucketName;
  @Nullable private final Boolean clearDatastore;
  @Nullable private final Path datastorePath;
  @Nullable private final Map<String, String> environment;
  @Nullable private final List<String> additionalArguments;
  @Nullable private final String projectId;

  private RunConfiguration(
      List<Path> services,
      @Nullable String host,
      @Nullable Integer port,
      @Nullable String adminHost,
      @Nullable Integer adminPort,
      @Nullable String authDomain,
      @Nullable Path storagePath,
      @Nullable String logLevel,
      @Nullable Integer maxModuleInstances,
      @Nullable Boolean useMtimeFileWatcher,
      @Nullable String threadsafeOverride,
      @Nullable String pythonStartupScript,
      @Nullable String pythonStartupArgs,
      @Nullable List<String> jvmFlags,
      @Nullable String customEntrypoint,
      @Nullable String runtime,
      @Nullable Boolean allowSkippedFiles,
      @Nullable Integer apiPort,
      @Nullable Boolean automaticRestart,
      @Nullable String devAppserverLogLevel,
      @Nullable Boolean skipSdkUpdateCheck,
      @Nullable String defaultGcsBucketName,
      @Nullable Boolean clearDatastore,
      @Nullable Path datastorePath,
      @Nullable Map<String, String> environment,
      @Nullable List<String> additionalArguments,
      @Nullable String projectId) {
    this.services = services;
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
    this.clearDatastore = clearDatastore;
    this.datastorePath = datastorePath;
    this.environment = environment;
    this.additionalArguments = additionalArguments;
    this.projectId = projectId;
  }

  /**
   * List of all the services (1 or more) that need to be run with the local devappserver. If a
   * directory, it must include WEB-INF/appengine-web.xml. For dev appserver 2, this may instead
   * include references to app.yamls.
   */
  @Nullable
  public List<Path> getServices() {
    return services;
  }

  @Nullable
  public String getHost() {
    return host;
  }

  @Nullable
  public Integer getPort() {
    return port;
  }

  @Nullable
  public String getAdminHost() {
    return adminHost;
  }

  @Nullable
  public Integer getAdminPort() {
    return adminPort;
  }

  @Nullable
  public String getAuthDomain() {
    return authDomain;
  }

  @Nullable
  public Path getStoragePath() {
    return storagePath;
  }

  @Nullable
  public String getLogLevel() {
    return logLevel;
  }

  @Nullable
  public Integer getMaxModuleInstances() {
    return maxModuleInstances;
  }

  @Nullable
  public Boolean getUseMtimeFileWatcher() {
    return useMtimeFileWatcher;
  }

  @Nullable
  public String getThreadsafeOverride() {
    return threadsafeOverride;
  }

  @Nullable
  public String getPythonStartupScript() {
    return pythonStartupScript;
  }

  @Nullable
  public String getPythonStartupArgs() {
    return pythonStartupArgs;
  }

  @Nullable
  public List<String> getJvmFlags() {
    return jvmFlags;
  }

  @Nullable
  public String getCustomEntrypoint() {
    return customEntrypoint;
  }

  @Nullable
  public String getRuntime() {
    return runtime;
  }

  @Nullable
  public Boolean getAllowSkippedFiles() {
    return allowSkippedFiles;
  }

  @Nullable
  public Integer getApiPort() {
    return apiPort;
  }

  @Nullable
  public Boolean getAutomaticRestart() {
    return automaticRestart;
  }

  @Nullable
  public String getDevAppserverLogLevel() {
    return devAppserverLogLevel;
  }

  @Nullable
  public Boolean getSkipSdkUpdateCheck() {
    return skipSdkUpdateCheck;
  }

  @Nullable
  public String getDefaultGcsBucketName() {
    return defaultGcsBucketName;
  }

  @Nullable
  public Boolean getClearDatastore() {
    return clearDatastore;
  }

  @Nullable
  public Path getDatastorePath() {
    return datastorePath;
  }

  @Nullable
  public Map<String, String> getEnvironment() {
    return environment;
  }

  /**
   * Any additional arguments to be passed to the appserver. These arguments are neither parsed nor
   * validated.
   */
  @Nullable
  public List<String> getAdditionalArguments() {
    return additionalArguments;
  }

  /** Gets the GCP project ID. */
  @Nullable
  public String getProjectId() {
    return projectId;
  }

  public static Builder builder(List<Path> services) {
    return new Builder(services);
  }

  public static final class Builder {
    private List<Path> services;
    @Nullable private String host;
    @Nullable private Integer port;
    @Nullable private String adminHost;
    @Nullable private Integer adminPort;
    @Nullable private String authDomain;
    @Nullable private Path storagePath;
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
    @Nullable private Path datastorePath;
    @Nullable private Map<String, String> environment;
    @Nullable private List<String> additionalArguments;
    @Nullable private String projectId;

    private Builder(List<Path> services) {
      if (services == null || services.size() == 0) {
        throw new IllegalArgumentException("Null/empty services");
      }
      this.services = services;
    }

    public Builder setHost(@Nullable String host) {
      this.host = host;
      return this;
    }

    public Builder setPort(@Nullable Integer port) {
      this.port = port;
      return this;
    }

    public Builder setAdminHost(@Nullable String adminHost) {
      this.adminHost = adminHost;
      return this;
    }

    public Builder setAdminPort(@Nullable Integer adminPort) {
      this.adminPort = adminPort;
      return this;
    }

    public Builder setAuthDomain(@Nullable String authDomain) {
      this.authDomain = authDomain;
      return this;
    }

    public Builder setStoragePath(@Nullable Path storagePath) {
      this.storagePath = storagePath;
      return this;
    }

    public Builder setLogLevel(@Nullable String logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    public Builder setMaxModuleInstances(@Nullable Integer maxModuleInstances) {
      this.maxModuleInstances = maxModuleInstances;
      return this;
    }

    public Builder setUseMtimeFileWatcher(@Nullable Boolean useMtimeFileWatcher) {
      this.useMtimeFileWatcher = useMtimeFileWatcher;
      return this;
    }

    public Builder setThreadsafeOverride(@Nullable String threadsafeOverride) {
      this.threadsafeOverride = threadsafeOverride;
      return this;
    }

    public Builder setPythonStartupScript(@Nullable String pythonStartupScript) {
      this.pythonStartupScript = pythonStartupScript;
      return this;
    }

    public Builder setPythonStartupArgs(@Nullable String pythonStartupArgs) {
      this.pythonStartupArgs = pythonStartupArgs;
      return this;
    }

    public Builder setJvmFlags(@Nullable List<String> jvmFlags) {
      this.jvmFlags = jvmFlags;
      return this;
    }

    public Builder setCustomEntrypoint(@Nullable String customEntrypoint) {
      this.customEntrypoint = customEntrypoint;
      return this;
    }

    public Builder setRuntime(@Nullable String runtime) {
      this.runtime = runtime;
      return this;
    }

    public Builder setAllowSkippedFiles(@Nullable Boolean allowSkippedFiles) {
      this.allowSkippedFiles = allowSkippedFiles;
      return this;
    }

    public Builder setApiPort(@Nullable Integer apiPort) {
      this.apiPort = apiPort;
      return this;
    }

    public Builder setAutomaticRestart(@Nullable Boolean automaticRestart) {
      this.automaticRestart = automaticRestart;
      return this;
    }

    public Builder setDevAppserverLogLevel(@Nullable String devAppserverLogLevel) {
      this.devAppserverLogLevel = devAppserverLogLevel;
      return this;
    }

    public Builder setSkipSdkUpdateCheck(@Nullable Boolean skipSdkUpdateCheck) {
      this.skipSdkUpdateCheck = skipSdkUpdateCheck;
      return this;
    }

    public Builder setDefaultGcsBucketName(@Nullable String defaultGcsBucketName) {
      this.defaultGcsBucketName = defaultGcsBucketName;
      return this;
    }

    public Builder setClearDatastore(@Nullable Boolean clearDatastore) {
      this.clearDatastore = clearDatastore;
      return this;
    }

    public Builder setDatastorePath(@Nullable Path datastorePath) {
      this.datastorePath = datastorePath;
      return this;
    }

    public Builder setEnvironment(@Nullable Map<String, String> environment) {
      this.environment = environment;
      return this;
    }

    public Builder setAdditionalArguments(@Nullable List<String> additionalArguments) {
      this.additionalArguments = additionalArguments;
      return this;
    }

    public Builder setProjectId(@Nullable String projectId) {
      this.projectId = projectId;
      return this;
    }

    /** Build a {@link RunConfiguration}. */
    public RunConfiguration build() {
      return new RunConfiguration(
          services,
          host,
          port,
          adminHost,
          adminPort,
          authDomain,
          storagePath,
          logLevel,
          maxModuleInstances,
          useMtimeFileWatcher,
          threadsafeOverride,
          pythonStartupScript,
          pythonStartupArgs,
          jvmFlags,
          customEntrypoint,
          runtime,
          allowSkippedFiles,
          apiPort,
          automaticRestart,
          devAppserverLogLevel,
          skipSdkUpdateCheck,
          defaultGcsBucketName,
          clearDatastore,
          datastorePath,
          environment,
          additionalArguments,
          projectId);
    }
  }
}
