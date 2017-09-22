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
import com.google.cloud.tools.appengine.api.logs.AppEngineLogs;
import com.google.cloud.tools.appengine.api.logs.LogsConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

/** Cloud SDK based implementation of {@link AppEngineLogs}. */
public class CloudSdkAppEngineLogs implements AppEngineLogs {

  private CloudSdk sdk;

  public CloudSdkAppEngineLogs(CloudSdk sdk) {
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
   * Read log entries.
   *
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  @Override
  public void read(LogsConfiguration configuration) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("logs");
    arguments.add("read");
    arguments.addAll(GcloudArgs.get("level", configuration.getLevel()));
    arguments.addAll(GcloudArgs.get("version", configuration.getVersion()));
    arguments.addAll(GcloudArgs.get("service", configuration.getService()));
    arguments.addAll(GcloudArgs.get("limit", configuration.getLimit()));
    arguments.addAll(GcloudArgs.get(configuration));

    execute(arguments);
  }
}
