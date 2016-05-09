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
import com.google.cloud.tools.app.api.service.AppEngineService;
import com.google.cloud.tools.app.api.service.TrafficSplitConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineService}.
 */
public class CloudSdkAppEngineService implements AppEngineService {

  private CloudSdk sdk;

  public CloudSdkAppEngineService(
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
   * Set the traffic splitting.
   */
  @Override
  public void setTraffic(TrafficSplitConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getServices());
    Preconditions.checkArgument(configuration.getServices().size() > 0);
    Preconditions.checkNotNull(configuration.getVersionToTrafficSplit());
    Preconditions.checkArgument(configuration.getVersionToTrafficSplit().size() > 0);
    Preconditions.checkNotNull(sdk);

    List<String> arguments = new ArrayList<>();
    arguments.add("services");
    arguments.add("set-traffic");
    arguments.addAll(configuration.getServices());
    arguments.add("--splits");
    arguments.add(Args.keyValues(configuration.getVersionToTrafficSplit()));

    execute(arguments);
  }
}
