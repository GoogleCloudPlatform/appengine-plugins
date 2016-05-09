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

package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.versions.AppEngineVersions;
import com.google.cloud.tools.app.api.versions.VersionsListConfiguration;
import com.google.cloud.tools.app.api.versions.VersionsSelectionConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineVersions}.
 */
public class CloudSdkAppEngineVersions implements AppEngineVersions {

  private CloudSdk sdk;

  public CloudSdkAppEngineVersions(
      CloudSdk sdk) {
    this.sdk = sdk;
  }

  private void execute(List<String> arguments) throws AppEngineException {
    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  /**
   * Starts serving a specific version or versions.
   */
  @Override
  public void start(VersionsSelectionConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getVersions());
    Preconditions.checkArgument(configuration.getVersions().size() > 0);
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("versions");
    arguments.add("start");
    arguments.addAll(configuration.getVersions());
    arguments.addAll(Args.string("service", configuration.getService()));

    execute(arguments);
  }

  /**
   * Stops serving a specific version or versions.
   */
  @Override
  public void stop(VersionsSelectionConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getVersions());
    Preconditions.checkArgument(configuration.getVersions().size() > 0);
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("versions");
    arguments.add("stop");
    arguments.addAll(configuration.getVersions());
    arguments.addAll(Args.string("service", configuration.getService()));

    execute(arguments);
  }

  /**
   * Deletes a specified version or versions.
   */
  @Override
  public void delete(VersionsSelectionConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getVersions());
    Preconditions.checkArgument(configuration.getVersions().size() > 0);

    List<String> arguments = new ArrayList<>();
    arguments.add("versions");
    arguments.add("delete");
    arguments.addAll(configuration.getVersions());
    arguments.addAll(Args.string("service", configuration.getService()));

    execute(arguments);
  }

  /**
   * Lists the versions for a service, or every version of every service if no service is specified.
   */
  @Override
  public void list(VersionsListConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("versions");
    arguments.add("list");
    arguments.addAll(Args.string("service", configuration.getService()));
    arguments.addAll(Args.bool("hide-no-traffic", configuration.getHideNoTraffic()));

    execute(arguments);
  }
}
