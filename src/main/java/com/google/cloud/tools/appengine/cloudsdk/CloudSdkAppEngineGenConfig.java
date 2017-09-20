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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.genconfig.GenConfigParams;
import com.google.cloud.tools.appengine.api.genconfig.GenConfigUtility;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

/** Cloud SDK based implementation of {@link GenConfigUtility}. */
public class CloudSdkAppEngineGenConfig implements GenConfigUtility {

  private CloudSdk sdk;

  public CloudSdkAppEngineGenConfig(CloudSdk sdk) {
    this.sdk = Preconditions.checkNotNull(sdk);
  }

  /**
   * Generates missing configuration files.
   *
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  @Override
  public void genConfig(GenConfigParams config) throws AppEngineException {
    Preconditions.checkNotNull(config);

    if (!config.getSourceDirectory().exists()) {
      throw new AppEngineException(
          "Source directory does not exist. Location: " + config.getSourceDirectory().toPath());
    }
    if (!config.getSourceDirectory().isDirectory()) {
      throw new AppEngineException(
          "Source location is not a directory. Location: " + config.getSourceDirectory().toPath());
    }

    List<String> arguments = new ArrayList<>();
    arguments.add("gen-config");

    if (config.getSourceDirectory() != null) {
      arguments.add(config.getSourceDirectory().toPath().toString());
    }

    arguments.addAll(GcloudArgs.get("config", config.getConfig()));
    arguments.addAll(GcloudArgs.get("custom", config.getCustom()));
    arguments.addAll(GcloudArgs.get("runtime", config.getRuntime()));

    try {
      sdk.runAppCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }
}
