/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.install;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Operating System specific install script command line provider. */
interface InstallScriptProvider {

  /**
   * Returns a list of strings for running the OS specific installer script. This list only contains
   * the script itself and supporting CLI launch scripts. It does NOT provide configuration
   * parameters for the script.
   */
  List<String> getScriptCommandLine(Path installedSdkRoot);

  @Nullable
  Map<String, String> getScriptEnvironment();
}
