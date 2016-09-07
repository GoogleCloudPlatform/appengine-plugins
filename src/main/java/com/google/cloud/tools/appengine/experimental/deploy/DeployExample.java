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

package com.google.cloud.tools.appengine.experimental.deploy;

import com.google.cloud.tools.appengine.api.deploy.DefaultDeployConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.PathResolver;
import com.google.cloud.tools.appengine.experimental.AppEngineRequestFactory;
import com.google.cloud.tools.appengine.experimental.AppEngineRequests;
import com.google.cloud.tools.appengine.experimental.internal.process.io.PrintStreamOutputHandler;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DeployExample {

  /**
   * Example usage.
   */
  public static void main(String[] args) {

    // perhaps we should be using builders for execute configurations
    DefaultDeployConfiguration config = new DefaultDeployConfiguration();
    config.setDeployables(Collections.singletonList(new File("/tmp/app.yaml")));

    // the current implementation doesn't have a good hook in to the autodetection of the
    // cloud sdk.
    AppEngineRequestFactory requestFactory = AppEngineRequests.newRequestFactoryBuilder()
        //.cloudSdk(Paths.get("/path/to/cloudsk"))
        // or explicitly tell it to look for it
        .cloudSdk(
            new PathResolver().getCloudSdkPath()) //<<-- path resolver would go into the public API
        .build();

    // do the execute,
    // the implementation of Deployment request doesn't allow modifiying the DeploymentRequest
    // after execute() is called by enforcing it with illegal state exceptions? Is that necessary?
    // Does the builder style imply that would be the case? Who knows...
    Future<DeployResult> deployFuture = requestFactory.newDeploymentRequest(config)
        .outputHandler(new PrintStreamOutputHandler(System.out))
        .execute();

    try {
      // get the result -- a blocking call
      //Thread.sleep(1000);
      //deployFuture.cancel(true);
      DeployResult result = deployFuture.get();

      System.out.println("result = " + result.data);
      // or kill it!
      //deployFuture.cancel(true);

      // or whatever, I don't know, anything a future can do

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
