/**
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
package com.google.cloud.tools.app.action;

import com.google.cloud.tools.app.config.RunConfiguration;
import com.google.cloud.tools.app.executor.DevAppServerExecutor;
import com.google.cloud.tools.app.executor.ExecutorException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts the local development server, synchronous or asynchronously.
 */
public class RunAction implements Action {

  private RunConfiguration configuration;
  private DevAppServerExecutor appServerExec;

  public RunAction(RunConfiguration configuration, DevAppServerExecutor appServerExec) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getAppYamls());
    Preconditions.checkArgument(configuration.getAppYamls().size() > 0);
    Preconditions.checkNotNull(appServerExec);

    this.configuration = configuration;
    this.appServerExec = appServerExec;
  }


  @Override
  public int execute() throws ExecutorException {
    List<String> arguments = new ArrayList<>();
    for (File appYaml : configuration.getAppYamls()) {
      arguments.add(appYaml.toPath().toString());
    }
    if (!Strings.isNullOrEmpty(configuration.getHost())) {
      arguments.add("--host");
      arguments.add(configuration.getHost());
    }
    if (configuration.getPort() != null) {
      arguments.add("--port");
      arguments.add(String.valueOf(configuration.getPort()));
    }
    if (!Strings.isNullOrEmpty(configuration.getAdminHost())) {
      arguments.add("--admin_host");
      arguments.add(configuration.getAdminHost());
    }
    if (configuration.getAdminPort() != null) {
      arguments.add("--admin_port");
      arguments.add(String.valueOf(configuration.getAdminPort()));
    }
    if (!Strings.isNullOrEmpty(configuration.getAuthDomain())) {
      arguments.add("--auth_domain");
      arguments.add(configuration.getAuthDomain());
    }
    if (!Strings.isNullOrEmpty(configuration.getStoragePath())) {
      arguments.add("--storage_path");
      arguments.add(configuration.getStoragePath());
    }
    if (!Strings.isNullOrEmpty(configuration.getLogLevel())) {
      arguments.add("--log_level");
      arguments.add(configuration.getLogLevel());
    }
    if (configuration.getMaxModuleInstances() != null) {
      arguments.add("--max_module_instances");
      arguments.add(String.valueOf(configuration.getMaxModuleInstances()));
    }
    if (configuration.isUseMtimeFileWatcher()) {
      arguments.add("--use_mtime_file_watcher");
    }
    if (!Strings.isNullOrEmpty(configuration.getThreadsafeOverride())) {
      arguments.add("--threadsafe_override");
      arguments.add(configuration.getThreadsafeOverride());
    }
    if (!Strings.isNullOrEmpty(configuration.getPythonStartupScript())) {
      arguments.add("--python_startup_script");
      arguments.add(configuration.getPythonStartupScript());
    }
    if (!Strings.isNullOrEmpty(configuration.getPythonStartupArgs())) {
      arguments.add("--python_startup_args");
      arguments.add(configuration.getPythonStartupArgs());
    }
    if (configuration.getJvmFlags() != null) {
      for (String jvmFlag : configuration.getJvmFlags()) {
        arguments.add("--jvm_flag");
        arguments.add(jvmFlag);
      }
    }
    if (!Strings.isNullOrEmpty(configuration.getCustomEntrypoint())) {
      arguments.add("--custom_entrypoint");
      arguments.add(configuration.getCustomEntrypoint());
    }
    if (!Strings.isNullOrEmpty(configuration.getRuntime())) {
      arguments.add("--runtime");
      arguments.add(configuration.getRuntime());
    }
    if (configuration.isAllowSkippedFiles()) {
      arguments.add("--allow_skipped_files");
    }
    if (configuration.getApiPort() != null) {
      arguments.add("--api_port");
      arguments.add(String.valueOf(configuration.getApiPort()));
    }
    if (configuration.isAutomaticRestart()) {
      arguments.add("--automatic_restart");
    }
    if (!Strings.isNullOrEmpty(configuration.getDevAppserverLogLevel())) {
      arguments.add("--dev_appserver_log_level");
      arguments.add(configuration.getDevAppserverLogLevel());
    }
    if (configuration.isSkipSdkUpdateCheck()) {
      arguments.add("--skip_sdk_update_check");
    }
    if (!Strings.isNullOrEmpty(configuration.getDefaultGcsBucketName())) {
      arguments.add("--default_gcs_bucket_name");
      arguments.add(configuration.getDefaultGcsBucketName());
    }

    if (configuration.isRunAsync()) {
      // TODO : handle future here if you want
      appServerExec.runDevAppServerAsync(arguments);
      return 0;
    }
    else {
      return appServerExec.runDevAppServer(arguments);
    }
  }
}
