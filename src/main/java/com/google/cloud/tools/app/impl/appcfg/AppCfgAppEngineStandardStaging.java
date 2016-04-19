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

import com.google.appengine.repackaged.com.google.api.client.util.Strings;
import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.app.api.deploy.StageStandardConfiguration;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link AppEngineStandardStaging} that uses App Engine SDK.
 */
public class AppCfgAppEngineStandardStaging implements AppEngineStandardStaging {

  private AppEngineSdk appEngineSdk;

  public AppCfgAppEngineStandardStaging(
      AppEngineSdk appEngineSdk) {
    this.appEngineSdk = appEngineSdk;
  }

  @Override
  public void stageStandard(StageStandardConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getSourceDirectory());
    Preconditions.checkNotNull(configuration.getStagingDirectory());
    Preconditions.checkNotNull(appEngineSdk);

    List<String> arguments = new ArrayList<>();
    arguments.add(0, "stage");
    arguments.add(configuration.getSourceDirectory().toPath().toString());
    arguments.add(configuration.getStagingDirectory().toPath().toString());
    if (configuration.isEnableQuickstart()) {
      arguments.add("--enable_quickstart");
    }
    if (configuration.isDisableUpdateCheck()) {
      arguments.add("--disable_update_check");
    }
    if (!Strings.isNullOrEmpty(configuration.getVersion())) {
      arguments.add("--version");
      arguments.add(configuration.getVersion());
    }
    if (!Strings.isNullOrEmpty(configuration.getApplicationId())) {
      arguments.add("-A");
      arguments.add(configuration.getApplicationId());
    }
    if (configuration.isEnableJarSplitting()) {
      arguments.add("--enable_jar_splitting");
    }
    if (!Strings.isNullOrEmpty(configuration.getJarSplittingExcludes())) {
      arguments.add("--jar_splitting_excludes");
      arguments.add(configuration.getJarSplittingExcludes());
    }
    if (!Strings.isNullOrEmpty(configuration.getCompileEncoding())) {
      arguments.add("--compile_encoding");
      arguments.add(configuration.getCompileEncoding());
    }
    if (configuration.isDeleteJsps()) {
      arguments.add("--delete_jsps");
    }
    if (configuration.isEnableJarClasses()) {
      arguments.add("--enable_jar_classes");
    }

    Path dockerfile =
        configuration.getDockerfile() == null ? null : configuration.getDockerfile().toPath();
    Path dockerfileDestination = configuration.getStagingDirectory().toPath();

    try {

      appEngineSdk.runCommand(arguments);

      if (dockerfile != null && dockerfileDestination != null) {
        Files.copy(dockerfile, dockerfileDestination, StandardCopyOption.REPLACE_EXISTING);
      }

    } catch (IOException e) {
      throw new AppEngineException(e);
    }

  }

}
