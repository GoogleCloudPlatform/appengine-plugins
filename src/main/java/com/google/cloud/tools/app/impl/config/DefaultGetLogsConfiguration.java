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

package com.google.cloud.tools.app.impl.config;

import com.google.cloud.tools.app.api.module.GetLogsConfiguration;

import java.util.Collection;

/**
 * Plain Java bean implementation of {@link GetLogsConfiguration}.
 */
public class DefaultGetLogsConfiguration implements GetLogsConfiguration {

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

  @Override
  public Collection<String> getModules() {
    return modules;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public String getLogFileLocation() {
    return logFileLocation;
  }

  @Override
  public Boolean isAppend() {
    return append;
  }

  @Override
  public Integer getDays() {
    return days;
  }

  @Override
  public Boolean isDetails() {
    return details;
  }

  @Override
  public String getEndDate() {
    return endDate;
  }

  @Override
  public String getServer() {
    return server;
  }

  @Override
  public String getSeverity() {
    return severity;
  }

  @Override
  public String getVhost() {
    return vhost;
  }
}
