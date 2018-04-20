/*
 * Copyright 2018 Google Inc.
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

import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.common.annotations.VisibleForTesting;

/** Operations that use appcfg. */
public class AppCfg {
  private final CloudSdk sdk;
  private final AppCfgRunner.Factory appCfgRunnerFactory;

  public static AppCfg newAppCfg(CloudSdk sdk) {
    return new AppCfg(sdk, new AppCfgRunner.Factory());
  }

  @VisibleForTesting
  AppCfg(CloudSdk sdk, AppCfgRunner.Factory appCfgRunnerFactory) {
    this.appCfgRunnerFactory = appCfgRunnerFactory;
    this.sdk = sdk;
  }

  public AppEngineStandardStaging newStaging(ProcessHandler processHandler) {
    return new CloudSdkAppEngineStandardStaging(getRunner(processHandler));
  }

  @VisibleForTesting
  AppCfgRunner getRunner(ProcessHandler processHandler) {
    return appCfgRunnerFactory.newRunner(sdk, processHandler);
  }
}
