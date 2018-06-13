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

import com.google.common.base.Preconditions;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** {@link InstallScriptProvider} for Mac and Linux. */
final class UnixInstallScriptProvider implements InstallScriptProvider {

  /** Instantiated by {@link InstallerFactory}. */
  UnixInstallScriptProvider() {}

  @Override
  public List<String> getScriptCommandLine(Path installedSdkRoot) {
    Preconditions.checkArgument(installedSdkRoot.isAbsolute(), "non-absolute SDK path");

    List<String> script = new ArrayList<>(1);
    script.add(installedSdkRoot.resolve("install.sh").toString());
    return script;
  }

  // todo should probably return an empty map
  @Override
  @Nullable
  public Map<String, String> getScriptEnvironment() {
    return null;
  }
}
