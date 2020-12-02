/*
 * Copyright 2017 Google LLC.
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** {@link InstallScriptProvider} for windows. */
final class WindowsInstallScriptProvider implements InstallScriptProvider {

  private final Map<String, String> additionalEnvironmentVariables;

  /** Instantiated by {@link InstallerFactory}. */
  WindowsInstallScriptProvider(Map<String, String> additionalEnvironmentVariables) {
    this.additionalEnvironmentVariables = additionalEnvironmentVariables;
  }

  @Override
  public List<String> getScriptCommandLine(Path installedSdkRoot) {
    Preconditions.checkArgument(installedSdkRoot.isAbsolute(), "non-absolute SDK path");

    List<String> script = new ArrayList<>(3);
    script.add("cmd.exe");
    script.add("/c");
    script.add(installedSdkRoot.resolve("install.bat").toString());
    return script;
  }

  @Override
  public Map<String, String> getScriptEnvironment() {
    Map<String, String> environment = new HashMap<>();
    if (additionalEnvironmentVariables != null) {
      environment.putAll(additionalEnvironmentVariables);
    }
    environment.put("CLOUDSDK_CORE_DISABLE_PROMPTS", "1");
    return ImmutableMap.copyOf(environment);
  }
}
