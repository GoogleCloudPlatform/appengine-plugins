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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.appengine.api.deploy.StageStandardConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.AppCfgArgs;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link AppEngineStandardStaging} that uses App Engine SDK bundled with the
 * Cloud SDK.
 */
public class CloudSdkAppEngineStandardStaging implements AppEngineStandardStaging {

  private AppCfgRunner runner;

  public CloudSdkAppEngineStandardStaging(AppCfgRunner runner) {
    this.runner = runner;
  }

  @Override
  public void stageStandard(StageStandardConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getSourceDirectory());
    Preconditions.checkNotNull(config.getStagingDirectory());

    List<String> arguments = new ArrayList<>();

    arguments.addAll(AppCfgArgs.get("enable_quickstart", config.getEnableQuickstart()));
    arguments.addAll(AppCfgArgs.get("disable_update_check", config.getDisableUpdateCheck()));
    arguments.addAll(AppCfgArgs.get("enable_jar_splitting", config.getEnableJarSplitting()));
    arguments.addAll(AppCfgArgs.get("jar_splitting_excludes", config.getJarSplittingExcludes()));
    arguments.addAll(AppCfgArgs.get("compile_encoding", config.getCompileEncoding()));
    arguments.addAll(AppCfgArgs.get("delete_jsps", config.getDeleteJsps()));
    arguments.addAll(AppCfgArgs.get("enable_jar_classes", config.getEnableJarClasses()));
    arguments.addAll(AppCfgArgs.get("disable_jar_jsps", config.getDisableJarJsps()));
    if (config.getRuntime() != null) {
      // currently only java7 is allowed without --allow_any_runtime
      arguments.addAll(AppCfgArgs.get("allow_any_runtime", true));
      arguments.addAll(AppCfgArgs.get("runtime", config.getRuntime()));
    }
    arguments.add("stage");
    arguments.add(config.getSourceDirectory().toString());
    arguments.add(config.getStagingDirectory().toString());

    Path dockerfile = config.getDockerfile();

    try {

      if (dockerfile != null && Files.exists(dockerfile)) {
        Files.copy(
            dockerfile,
            config.getSourceDirectory().resolve(dockerfile.getFileName()),
            StandardCopyOption.REPLACE_EXISTING);
      }

      runner.run(arguments);

      // TODO : Move this fix up the chain (appcfg)
      if (config.getRuntime() != null && config.getRuntime().equals("java")) {
        Path appYaml = config.getStagingDirectory().resolve("app.yaml");
        Files.write(
            appYaml,
            "\nruntime_config:\n  jdk: openjdk8\n".getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }

    } catch (IOException | ProcessHandlerException e) {
      throw new AppEngineException(e);
    }
  }
}
