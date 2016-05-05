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

package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.app.api.deploy.StageStandardConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link AppEngineStandardStaging} that uses App Engine SDK bundled with the
 * Cloud SDK.
 */
public class CloudSdkAppEngineStandardStaging implements AppEngineStandardStaging {

  private CloudSdk cloudSdk;

  public CloudSdkAppEngineStandardStaging(
      CloudSdk cloudSdk) {
    this.cloudSdk = cloudSdk;
  }

  @Override
  public void stageStandard(StageStandardConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getSourceDirectory());
    Preconditions.checkNotNull(config.getStagingDirectory());
    Preconditions.checkNotNull(cloudSdk);

    List<String> arguments = new ArrayList<>();

    arguments.addAll(Args.bool("enable_quickstart", config.getEnableQuickstart()));
    arguments.addAll(Args.bool("disable_update_check", config.getDisableUpdateCheck()));
    arguments.addAll(Args.bool("enable_jar_splitting", config.getEnableJarSplitting()));
    arguments.addAll(Args.stringEq("jar_splitting_excludes", config.getJarSplittingExcludes()));
    arguments.addAll(Args.stringEq("compile_encoding", config.getCompileEncoding()));
    arguments.addAll(Args.bool("delete_jsps", config.getDeleteJsps()));
    arguments.addAll(Args.bool("enable_jar_classes", config.getEnableJarClasses()));

    arguments.add("stage");
    arguments.add(config.getSourceDirectory().toPath().toString());
    arguments.add(config.getStagingDirectory().toPath().toString());

    Path dockerfile =
        config.getDockerfile() == null ? null : config.getDockerfile().toPath();
    Path dockerfileDestination = config.getStagingDirectory().toPath();

    try {

      cloudSdk.runAppCfgCommand(arguments);

      if (dockerfile != null) {
        Files.copy(dockerfile, dockerfileDestination, StandardCopyOption.REPLACE_EXISTING);
      }

    } catch (IOException | ProcessRunnerException e) {
      throw new AppEngineException(e);
    }

  }

}
