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

package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.app.api.devserver.RunConfiguration;
import com.google.cloud.tools.app.api.devserver.StopConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.util.Args;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud SDK based implementation of {@link AppEngineDevServer}.
 */
public class CloudSdkAppEngineDevServer implements AppEngineDevServer {

  private CloudSdkCli sdkCli;

  private static final String DEFAULT_ADMIN_HOST = "localhost";
  private static final int DEFAULT_ADMIN_PORT = 8000;

  public CloudSdkAppEngineDevServer(CloudSdkCli sdkCli) {
    this.sdkCli = sdkCli;
  }

  /**
   * Starts the local development server, synchronous or asynchronously.
   */
  @Override
  public void run(RunConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getAppYamls());
    Preconditions.checkArgument(config.getAppYamls().size() > 0);
    Preconditions.checkNotNull(sdkCli);

    List<String> arguments = new ArrayList<>();
    for (File appYaml : config.getAppYamls()) {
      arguments.add(appYaml.toPath().toString());
    }

    arguments.addAll(Args.string("host", config.getHost()));
    arguments.addAll(Args.integer("port", config.getPort()));
    arguments.addAll(Args.string("admin_host", config.getAdminHost()));
    arguments.addAll(Args.integer("admin_port", config.getAdminPort()));
    arguments.addAll(Args.string("auth_domain", config.getAuthDomain()));
    arguments.addAll(Args.string("storage_path", config.getStoragePath()));
    arguments.addAll(Args.string("log_level", config.getLogLevel()));
    arguments.addAll(Args.integer("max_module_instances", config.getMaxModuleInstances()));
    arguments.addAll(Args.bool("use_mtime_file_watcher", config.getUseMtimeFileWatcher()));
    arguments.addAll(Args.string("threadsafe_override", config.getThreadsafeOverride()));
    arguments.addAll(Args.string("python_startup_script", config.getPythonStartupScript()));
    arguments.addAll(Args.string("python_startup_args", config.getPythonStartupArgs()));
    arguments.addAll(Args.strings("jvm_flag", config.getJvmFlags()));
    arguments.addAll(Args.string("custom_entrypoint", config.getCustomEntrypoint()));
    arguments.addAll(Args.string("runtime", config.getRuntime()));
    arguments.addAll(Args.bool("allow_skipped_files", config.getAllowSkippedFiles()));
    arguments.addAll(Args.integer("api_port", config.getApiPort()));
    arguments.addAll(Args.bool("automatic_restart", config.getAutomaticRestart()));
    arguments.addAll(Args.string("dev_appserver_log_level", config.getDevAppserverLogLevel()));
    arguments.addAll(Args.bool("skip_sdk_update_check", config.getSkipSdkUpdateCheck()));
    arguments.addAll(Args.string("default_gcs_bucket_name", config.getDefaultGcsBucketName()));

    try {
      sdkCli.runDevAppServerCommand(arguments);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  /**
   * Stops the local development server.
   */
  @Override
  public void stop(StopConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);

    try {
      URL adminServerUrl = new URL(
          "http",
          configuration.getAdminHost() != null ? configuration.getAdminHost() : DEFAULT_ADMIN_HOST,
          configuration.getAdminPort() != null ? configuration.getAdminPort() : DEFAULT_ADMIN_PORT,
          "/quit");
      HttpURLConnection connection = (HttpURLConnection) adminServerUrl.openConnection();

      connection.setReadTimeout(4000);
      connection.connect();
      connection.disconnect();
      int responseCode = connection.getResponseCode();
      if (responseCode < 200 || responseCode > 299) {
        throw new AppEngineException(
            "The development server responded with " + connection.getResponseMessage() + ".");
      }
    } catch (IOException e) {
      throw new AppEngineException(e);
    }
  }
}
