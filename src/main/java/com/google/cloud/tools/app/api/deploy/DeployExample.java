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

package com.google.cloud.tools.app.api.deploy;


import com.google.cloud.tools.app.impl.cloudsdk.internal.process.SysOutStatusLineListener;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DeployExample {
  public void example() {

    Deployment deployment = new DeploymentBuilder()
        .sdkPath(new File("google-cloud-sdk"))                  // optional gcloud-specific config
        .statusLineListener(new SysOutStatusLineListener())     // optional gcloud-specific config
        .deployables(new File("app.yaml"))                      // optional deployment config
        .force(true)                                            // optional deployment config
        .build();

    Future<DeploymentResult> result = deployment.deploy();

    try {
      // wait for completion
      result.get();

      // or cancel
      result.cancel(true);

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
