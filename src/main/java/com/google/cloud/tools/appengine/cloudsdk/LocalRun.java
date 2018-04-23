/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.common.annotations.VisibleForTesting;

/** Create Dev App Servers. */
public class LocalRun {
  private final CloudSdk sdk;
  private final DevAppServerRunner.Factory devAppServerRunnerFactory;

  private LocalRun(CloudSdk sdk, DevAppServerRunner.Factory devAppServerRunnerFactory) {
    this.devAppServerRunnerFactory = devAppServerRunnerFactory;
    this.sdk = sdk;
  }

  public AppEngineDevServer newDevAppServer1(ProcessHandler processHandler) {
    return new CloudSdkAppEngineDevServer1(sdk, getRunner(processHandler));
  }

  public AppEngineDevServer newDevAppServer2(ProcessHandler processHandler) {
    return new CloudSdkAppEngineDevServer2(getRunner(processHandler));
  }

  @VisibleForTesting
  DevAppServerRunner getRunner(ProcessHandler processHandler) {
    return devAppServerRunnerFactory.newRunner(sdk, processHandler);
  }

  public static Builder builder(CloudSdk sdk) {
    return new Builder(sdk, new DevAppServerRunner.Factory());
  }

  public static class Builder {
    private final CloudSdk sdk;
    private final DevAppServerRunner.Factory runnerFactory;

    @VisibleForTesting
    Builder(CloudSdk sdk, DevAppServerRunner.Factory runnerFactory) {
      this.sdk = sdk;
      this.runnerFactory = runnerFactory;
    }

    /** Build an immutable LocalRun instance. */
    public LocalRun build() {
      return new LocalRun(sdk, runnerFactory);
    }
  }
}
