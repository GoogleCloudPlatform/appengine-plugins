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
package com.google.cloud.tools.app.config.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link GetLogsConfiguration}.
 */
public class DefaultGetLogsConfiguration implements GetLogsConfiguration {

  private final Collection<String> modules;
  private final String version;
  private final String logFileLocation;
  private final Boolean append;
  private final Integer days;
  private final Boolean details;
  private final String endDate;
  private final String server;
  private final String severity;
  private final String vhost;

  private DefaultGetLogsConfiguration(Collection<String> modules, String version,
      @Nullable String logFileLocation, @Nullable Boolean append, @Nullable Integer days,
      @Nullable Boolean details, @Nullable String endDate, @Nullable String server,
      @Nullable String severity, @Nullable String vhost) {
    this.modules = modules;
    this.version = version;
    this.logFileLocation = logFileLocation;
    this.append = append;
    this.days = days;
    this.details = details;
    this.endDate = endDate;
    this.server = server;
    this.severity = severity;
    this.vhost = vhost;
  }

  public Collection<String> getModules() {
    return modules;
  }

  public String getVersion() {
    return version;
  }

  public String getLogFileLocation() {
    return logFileLocation;
  }

  public Boolean isAppend() {
    return append;
  }

  public Integer getDays() {
    return days;
  }

  public Boolean isDetails() {
    return details;
  }

  public String getEndDate() {
    return endDate;
  }

  public String getServer() {
    return server;
  }

  public String getSeverity() {
    return severity;
  }

  public String getVhost() {
    return vhost;
  }

  public static Builder newBuilder(String version, String... modules) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(version));
    Preconditions.checkArgument(modules.length > 0);

    return new Builder(ImmutableList.copyOf(modules), version);
  }

  public static class Builder {
    private Collection<String> modules;
    private String version;
    private String logFileLocation;
    private Boolean append;
    private Integer days;
    private Boolean details;
    private String endDate;
    private String server;
    private String severity;
    private String vhost;

    private Builder(Collection<String> modules, String version) {
      this.modules = modules;
      this.version = version;
    }

    public Builder logFileLocation(String logFileLocation) {
      this.logFileLocation = logFileLocation;
      return this;
    }

    public Builder append(Boolean append) {
      this.append = append;
      return this;
    }

    public Builder days(Integer days) {
      this.days = days;
      return this;
    }

    public Builder details(Boolean details) {
      this.details = details;
      return this;
    }

    public Builder endDate(String endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder server(String server) {
      this.server = server;
      return this;
    }

    public Builder severity(String severity) {
      this.severity = severity;
      return this;
    }

    public Builder vhost(String vhost) {
      this.vhost = vhost;
      return this;
    }

    public GetLogsConfiguration build() {
      return new DefaultGetLogsConfiguration(modules, version, logFileLocation, append, days,
          details, endDate, server, severity, vhost);
    }
  }
}
