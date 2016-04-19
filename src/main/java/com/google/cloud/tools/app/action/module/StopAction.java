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
package com.google.cloud.tools.app.action.module;

import com.google.cloud.tools.app.action.Action;
import com.google.cloud.tools.app.config.module.StopConfiguration;
import com.google.cloud.tools.app.executor.AppExecutor;
import com.google.cloud.tools.app.executor.ExecutorException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Stops serving a specific version of a module.
 */
public class StopAction implements Action {

  private StopConfiguration configuration;
  private AppExecutor sdkExec;

  public StopAction(StopConfiguration configuration, AppExecutor sdkExec) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getModules());
    Preconditions.checkArgument(configuration.getModules().size() > 0);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(configuration.getVersion()));
    Preconditions.checkNotNull(sdkExec);

    this.configuration = configuration;
    this.sdkExec = sdkExec;
  }

  @Override
  public int execute() throws ExecutorException {
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

    return sdkExec.runApp(arguments);
  }

}
