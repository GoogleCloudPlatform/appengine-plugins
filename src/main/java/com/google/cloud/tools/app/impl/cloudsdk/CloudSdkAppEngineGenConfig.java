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
import com.google.cloud.tools.app.api.genconfig.GenConfigParams;
import com.google.cloud.tools.app.api.genconfig.GenConfigUtility;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link GenConfigUtility}.
 */
public class CloudSdkAppEngineGenConfig implements GenConfigUtility {

  private CloudSdk sdk;

  public CloudSdkAppEngineGenConfig(
      CloudSdk sdk) {
    this.sdk = sdk;
  }

  /**
   * Generates missing configuration files.
   */
  @Override
  public void genConfig(GenConfigParams config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(sdk);

    if (!config.getSourceDirectory().exists()) {
      throw new AppEngineException("Source directory does not exist. Location: "
          + config.getSourceDirectory().toPath().toString());
    }
    if (!config.getSourceDirectory().isDirectory()) {
      throw new AppEngineException("Source location is not a directory. Location: "
          + config.getSourceDirectory().toPath().toString());
    }

    List<String> arguments = new ArrayList<>();
    arguments.add("gen-config");

    if (config.getSourceDirectory() != null) {
      arguments.add(config.getSourceDirectory().toPath().toString());
    }

    arguments.addAll(Args.string("config", config.getConfig()));
    arguments.addAll(Args.boolWithNo("custom", config.getCustom()));
    arguments.addAll(Args.string("runtime", config.getRuntime()));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }

  }
}
