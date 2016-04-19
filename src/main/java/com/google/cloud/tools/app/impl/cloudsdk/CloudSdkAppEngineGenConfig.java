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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.genconfig.GenConfigParams;
import com.google.cloud.tools.app.api.genconfig.GenConfigUtility;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meltsufin on 4/18/16.
 */
public class CloudSdkAppEngineGenConfig implements GenConfigUtility {

  private CloudSdk sdk;

  /**
   * Generates missing configuration files.
   */
  @Override
  public void genConfig(GenConfigParams configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(sdk);

    if (!configuration.getSourceDirectory().exists()) {
      throw new AppEngineException("Source directory does not exist. Location: "
          + configuration.getSourceDirectory().toPath().toString());
    }
    if (!configuration.getSourceDirectory().isDirectory()) {
      throw new AppEngineException("Source location is not a directory. Location: "
          + configuration.getSourceDirectory().toPath().toString());
    }

    List<String> arguments = new ArrayList<>();
    arguments.add("gen-config");

    if (configuration.getSourceDirectory() != null) {
      arguments.add(configuration.getSourceDirectory().toPath().toString());
    }
    if (!isNullOrEmpty(configuration.getConfig())) {
      arguments.add("--config");
      arguments.add(configuration.getConfig());
    }
    if (configuration.isCustom()) {
      arguments.add("--custom");
    }
    if (!isNullOrEmpty(configuration.getRuntime())) {
      arguments.add("--runtime");
      arguments.add(configuration.getRuntime());
    }

    try {
      int result = sdk.runAppCommand(arguments);
      if (result != 0) {
        throw new AppEngineException("Generating configuration failed with error code: " + result);
      }
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }

  }
}
