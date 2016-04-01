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
package com.google.cloud.tools.app.action;

import com.google.cloud.tools.app.config.GenConfigConfiguration;
import com.google.cloud.tools.app.executor.AppExecutor;
import com.google.cloud.tools.app.executor.ExecutorException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Generates missing configuration files.
 */
public class GenConfigAction implements Action {

  private static Logger logger = Logger.getLogger(GenConfigAction.class.getName());
  private GenConfigConfiguration configuration;
  private AppExecutor sdkExec;

  public GenConfigAction(GenConfigConfiguration configuration, AppExecutor sdkExec) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(sdkExec);

    this.configuration = configuration;
    this.sdkExec = sdkExec;
  }

  @Override
  public int execute() throws ExecutorException {
    if (!configuration.getSourceDirectory().exists()) {
      logger.severe("Source directory does not exist. Location: "
          + configuration.getSourceDirectory().toPath().toString());
    }
    if (!configuration.getSourceDirectory().isDirectory()) {
      logger.severe("Source location is not a directory. Location: "
          + configuration.getSourceDirectory().toPath().toString());
    }

    List<String> arguments = new ArrayList<>();
    arguments.add("gen-config");

    if (configuration.getSourceDirectory() != null) {
      arguments.add(configuration.getSourceDirectory().toPath().toString());
    }
    if (!Strings.isNullOrEmpty(configuration.getConfig())) {
      arguments.add("--config");
      arguments.add(configuration.getConfig());
    }
    if (configuration.isCustom()) {
      arguments.add("--custom");
    }
    if (!Strings.isNullOrEmpty(configuration.getRuntime())) {
      arguments.add("--runtime");
      arguments.add(configuration.getRuntime());
    }

    return sdkExec.runApp(arguments);
  }
}
