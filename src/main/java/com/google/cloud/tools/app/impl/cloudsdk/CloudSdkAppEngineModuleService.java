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

package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.module.AppEngineModuleService;
import com.google.cloud.tools.app.api.module.GetLogsConfiguration;
import com.google.cloud.tools.app.api.module.ListConfiguration;
import com.google.cloud.tools.app.api.module.ModuleSelectionConfiguration;
import com.google.cloud.tools.app.api.module.SetManagedByConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineModuleService}.
 */
public class CloudSdkAppEngineModuleService implements AppEngineModuleService {

  private CloudSdk sdk;

  public CloudSdkAppEngineModuleService(
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
   * Starts serving a specific version in one or more modules.
   */
  @Override
  public void start(ModuleSelectionConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkArgument(configuration.getModules().size() > 0);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(configuration.getVersion()));
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("modules");
    arguments.add("start");
    arguments.addAll(configuration.getModules());
    arguments.add("--version");
    arguments.add(configuration.getVersion());
    if (!Strings.isNullOrEmpty(configuration.getServer())) {
      arguments.add("--server");
      arguments.add(configuration.getServer());
    }

    execute(arguments);
  }


  /**
   * Stops serving a specific version of a module.
   */
  @Override
  public void stop(ModuleSelectionConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkArgument(configuration.getModules().size() > 0);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(configuration.getVersion()));
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("modules");
    arguments.add("stop");
    arguments.addAll(configuration.getModules());
    arguments.add("--version");
    arguments.add(configuration.getVersion());
    if (!Strings.isNullOrEmpty(configuration.getServer())) {
      arguments.add("--server");
      arguments.add(configuration.getServer());
    }

    execute(arguments);
  }

  /**
   * Deletes a version of one or more modules.
   */
  @Override
  public void delete(ModuleSelectionConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkArgument(configuration.getModules().size() > 0);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(configuration.getVersion()));

    List<String> arguments = new ArrayList<>();
    arguments.add("modules");
    arguments.add("delete");
    arguments.addAll(configuration.getModules());
    arguments.add("--version");
    arguments.add(configuration.getVersion());
    arguments.add("--quiet");
    if (!Strings.isNullOrEmpty(configuration.getServer())) {
      arguments.add("--server");
      arguments.add(configuration.getServer());
    }

    execute(arguments);
  }

  /**
   * Sets the default version of a module.
   */
  @Override
  public void setDefault(ModuleSelectionConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkArgument(configuration.getModules().size() > 0);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(configuration.getVersion()));
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("modules");
    arguments.add("set-default");
    arguments.addAll(configuration.getModules());
    arguments.add("--version");
    arguments.add(configuration.getVersion());
    if (!Strings.isNullOrEmpty(configuration.getServer())) {
      arguments.add("--server");
      arguments.add(configuration.getServer());
    }

    execute(arguments);
  }

  /**
   * Gets the logs for a version of a module.
   */
  @Override
  public void getLogs(GetLogsConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkArgument(configuration.getModules().size() > 0);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(configuration.getVersion()));
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("modules");
    arguments.add("get-logs");
    arguments.addAll(configuration.getModules());
    // TODO(joaomartins): Check if file is valid.
    // TODO(joaomartins): Should we disallow empty files? Printing to stdout will be cluttered
    // by Maven's artifacts, for example.
    if (!Strings.isNullOrEmpty(configuration.getLogFileLocation())) {
      arguments.add(configuration.getLogFileLocation());
    }
    arguments.add("--version");
    arguments.add(configuration.getVersion());
    if (configuration.isAppend()) {
      arguments.add("--append");
    }
    if (configuration.getDays() != null) {
      arguments.add("--days");
      arguments.add(String.valueOf(configuration.getDays()));
    }
    if (configuration.isDetails()) {
      arguments.add("--details");
    }
    if (!Strings.isNullOrEmpty(configuration.getEndDate())) {
      arguments.add("--end-date");
      arguments.add(configuration.getEndDate());
    }
    if (!Strings.isNullOrEmpty(configuration.getServer())) {
      arguments.add("--server");
      arguments.add(configuration.getServer());
    }
    if (!Strings.isNullOrEmpty(configuration.getSeverity())) {
      arguments.add("--severity");
      arguments.add(configuration.getSeverity());
    }
    if (!Strings.isNullOrEmpty(configuration.getVhost())) {
      arguments.add("--vhost");
      arguments.add(configuration.getVhost());
    }

    execute(arguments);
  }

  /**
   * Lists the versions for a module, or every version of every module if no module is specified.
   */
  @Override
  public void list(ListConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("modules");
    arguments.add("list");
    arguments.addAll(configuration.getModules());
    if (!com.google.appengine.repackaged.com.google.api.client.util.Strings
        .isNullOrEmpty(configuration.getServer())) {
      arguments.add("--server");
      arguments.add(configuration.getServer());
    }

    execute(arguments);
  }

  /**
   * Sets a specific instance or every instance of a module to managed by Google or Self.
   */
  @Override
  public void setManagedBy(SetManagedByConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkArgument(configuration.getModules().size() > 0);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(configuration.getVersion()));
    Preconditions.checkNotNull(configuration.getManager());
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();

    arguments.add("modules");
    arguments.add("set-managed-by");
    arguments.addAll(configuration.getModules());
    arguments.add("--version");
    arguments.add(configuration.getVersion());
    arguments.add(configuration.getManager().getFlagForm());
    if (!Strings.isNullOrEmpty(configuration.getInstance())) {
      arguments.add("--instance");
      arguments.add(configuration.getInstance());
    }

    execute(arguments);
  }
}
