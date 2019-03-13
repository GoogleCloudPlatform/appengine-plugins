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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class RunConfiguration {

  private final List<Path> services;
  @Nullable private final String host;
  @Nullable private final Integer port;
  private final List<String> jvmFlags;
  @Nullable private final Boolean automaticRestart;
  @Nullable private final String defaultGcsBucketName;
  @Nullable private final Map<String, String> environment;
  @Nullable private final List<String> additionalArguments;
  @Nullable private final String projectId;

  private RunConfiguration(
      List<Path> services,
      @Nullable String host,
      @Nullable Integer port,
      List<String> jvmFlags,
      @Nullable Boolean automaticRestart,
      @Nullable String defaultGcsBucketName,
      @Nullable Map<String, String> environment,
      @Nullable List<String> additionalArguments,
      @Nullable String projectId) {
    this.services = services;
    this.host = host;
    this.port = port;
    this.jvmFlags = jvmFlags;
    this.automaticRestart = automaticRestart;
    this.defaultGcsBucketName = defaultGcsBucketName;
    this.environment = environment;
    this.additionalArguments = additionalArguments;
    this.projectId = projectId;
  }

  /**
   * List of all the services (1 or more) that need to be run with the local devappserver. If a
   * directory, it must include WEB-INF/appengine-web.xml. For dev appserver 2, this may instead
   * include references to app.yamls.
   */
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

  /**
   * Returns command line flags that will be passed to the Java virtual machine that runs the local
   * development server.
   */
  public List<String> getJvmFlags() {
    ArrayList<String> copy = new ArrayList<>(jvmFlags);
    return copy;
  }

  @Nullable
  public Boolean getAutomaticRestart() {
    return automaticRestart;
  }

  @Nullable
  public String getDefaultGcsBucketName() {
    return defaultGcsBucketName;
  }

  @Nullable
  public Map<String, String> getEnvironment() {
    return environment;
  }

  /**
   * Any additional arguments to be passed to the local development server. These arguments are
   * neither parsed nor validated.
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
    private List<String> jvmFlags = new ArrayList<>();
    @Nullable private Boolean automaticRestart;
    @Nullable private String defaultGcsBucketName;
    @Nullable private Map<String, String> environment;
    @Nullable private List<String> additionalArguments;
    @Nullable private String projectId;

    private Builder(List<Path> services) {
      Preconditions.checkNotNull(services);
      this.services = services;
    }

    public Builder host(@Nullable String host) {
      this.host = host;
      return this;
    }

    public Builder port(@Nullable Integer port) {
      this.port = port;
      return this;
    }

    /** Sets extra flags to be passed to the Java virtual machine. */
    public Builder jvmFlags(@Nullable List<String> jvmFlags) {
      this.jvmFlags.clear();
      if (jvmFlags != null) {
        this.jvmFlags.addAll(jvmFlags);
      }
      return this;
    }

    public Builder automaticRestart(@Nullable Boolean automaticRestart) {
      this.automaticRestart = automaticRestart;
      return this;
    }

    public Builder defaultGcsBucketName(@Nullable String defaultGcsBucketName) {
      this.defaultGcsBucketName = defaultGcsBucketName;
      return this;
    }

    public Builder environment(@Nullable Map<String, String> environment) {
      this.environment = environment;
      return this;
    }

    public Builder additionalArguments(@Nullable List<String> additionalArguments) {
      this.additionalArguments = additionalArguments;
      return this;
    }

    public Builder projectId(@Nullable String projectId) {
      this.projectId = projectId;
      return this;
    }

    /** Build a {@link RunConfiguration}. */
    public RunConfiguration build() {
      return new RunConfiguration(
          services,
          host,
          port,
          jvmFlags,
          automaticRestart,
          defaultGcsBucketName,
          environment,
          additionalArguments,
          projectId);
    }
  }

  /** Returns a mutable builder initialized with the values of this runtime configuration. */
  public Builder toBuilder() {
    Builder builder =
        builder(getServices())
            .additionalArguments(getAdditionalArguments())
            .automaticRestart(automaticRestart)
            .defaultGcsBucketName(defaultGcsBucketName)
            .environment(getEnvironment())
            .host(host)
            .jvmFlags(getJvmFlags())
            .port(port)
            .projectId(projectId);
    return builder;
  }
}
