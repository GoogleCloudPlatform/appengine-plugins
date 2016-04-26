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

package com.google.cloud.tools.app.impl.appcfg;

import com.google.appengine.tools.admin.AppCfg;

import java.io.File;
import java.util.List;

/**
 * App Engine SDK CLI wrapper.
 */
public class AppEngineSdk {

  private final File appengineSdkPath;

  public AppEngineSdk(File appengineSdkPath) {
    this.appengineSdkPath = appengineSdkPath;
  }

  /**
   * Executes an App Engine SDK CLI command synchronously.
   */
  public void runCommand(List<String> args) {
    // AppEngineSdk requires this system property to be set.
    System.setProperty("appengine.sdk.root", appengineSdkPath.toString());
    AppCfg.main(args.toArray(new String[args.size()]));
  }

}
