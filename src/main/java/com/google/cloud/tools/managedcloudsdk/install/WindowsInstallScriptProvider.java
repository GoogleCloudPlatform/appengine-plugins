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

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** {@link InstallScriptProvider} for windows. */
final class WindowsInstallScriptProvider implements InstallScriptProvider {

  /** Instantiated by {@link InstallerFactory}. */
  WindowsInstallScriptProvider() {}

  @Override
  public List<String> getScriptCommandLine() {
    List<String> script = new ArrayList<>(3);
    script.add("cmd.exe");
    script.add("/c");
    script.add("install.bat");
    return script;
  }

  @Override
  public Map<String, String> getScriptEnvironment() {
    return ImmutableMap.of("CLOUDSDK_CORE_DISABLE_PROMPTS", "1");
  }
}
