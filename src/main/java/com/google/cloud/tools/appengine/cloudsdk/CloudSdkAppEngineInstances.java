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
import com.google.cloud.tools.appengine.api.instances.AppEngineInstances;
import com.google.cloud.tools.appengine.api.instances.InstancesSelectionConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

/** Cloud SDK based implementation of {@link AppEngineInstances}. */
public class CloudSdkAppEngineInstances implements AppEngineInstances {

  private CloudSdk sdk;

  public CloudSdkAppEngineInstances(CloudSdk sdk) {
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
   * Enable debug on the instance.
   *
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  @Override
  public void enableDebug(InstancesSelectionConfiguration configuration) throws AppEngineException {
    setDebug(configuration, true);
  }

  /**
   * Disable debug on the instance.
   *
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  @Override
  public void disableDebug(InstancesSelectionConfiguration configuration)
      throws AppEngineException {
    setDebug(configuration, false);
  }

  private void setDebug(InstancesSelectionConfiguration configuration, boolean enable) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getVersion());
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("instances");
    if (enable) {
      arguments.add("enable-debug");
    } else {
      arguments.add("disable-debug");
    }
    arguments.addAll(GcloudArgs.get("version", configuration.getVersion()));
    arguments.addAll(GcloudArgs.get("service", configuration.getService()));
    arguments.addAll(GcloudArgs.get(configuration));

    execute(arguments);
  }
}
