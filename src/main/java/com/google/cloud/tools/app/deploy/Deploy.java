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

package com.google.cloud.tools.app.deploy;

import com.google.cloud.tools.app.api.deploy.DeployConfiguration;
import com.google.cloud.tools.app.deploy.gcloud.GcloudAppDeploy;
import com.google.cloud.tools.app.deploy.process.OutputHandler;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import java.util.concurrent.Future;

/**
 * Created by appu on 5/24/16.
 */
public class Deploy {

  // If there's a good way to configure the cloud SDK here instead of
  // providing an object, we can explore that.
  public static DeployRequestFactory newRequestFactory(CloudSdk sdk) {
    return new GcloudAppDeploy.Builder(sdk);
  }

  private Deploy() {
  }

  public interface DeployRequestFactory {
    DeployRequest newDeploymentRequest(DeployConfiguration config);
  }

  public interface DeployRequest {
    DeployRequest setStatusUpdater(OutputHandler handler);
    Future<DeployResult> deploy();
  }
}
