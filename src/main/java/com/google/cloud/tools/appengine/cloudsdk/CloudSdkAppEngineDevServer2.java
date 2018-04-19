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
import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.appengine.api.devserver.RunConfiguration;
import com.google.cloud.tools.appengine.api.devserver.StopConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.DevAppServerArgs;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

/** Cloud SDK based implementation of {@link AppEngineDevServer}. */
public class CloudSdkAppEngineDevServer2 implements AppEngineDevServer {

  private static final String DEFAULT_ADMIN_HOST = "localhost";
  private static final int DEFAULT_ADMIN_PORT = 8000;

  private final DevAppServerRunner runner;

  public CloudSdkAppEngineDevServer2(DevAppServerRunner runner) {
    this.runner = Preconditions.checkNotNull(runner);
  }

  /**
   * Starts the local development server, synchronously or asynchronously.
   *
   * @throws InvalidPathException when Python can't be located
   * @throws CloudSdkNotFoundException when Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when Cloud SDK is out of date
   * @throws AppEngineException I/O error in the dev-appserver
   */
  @Override
  public void run(RunConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getServices());
    Preconditions.checkArgument(config.getServices().size() > 0);

    List<String> arguments = new ArrayList<>();
    for (File serviceDirectory : config.getServices()) {
      arguments.add(serviceDirectory.toPath().toString());
    }

    arguments.addAll(DevAppServerArgs.get("host", config.getHost()));
    arguments.addAll(DevAppServerArgs.get("port", config.getPort()));
    arguments.addAll(DevAppServerArgs.get("admin_host", config.getAdminHost()));
    arguments.addAll(DevAppServerArgs.get("admin_port", config.getAdminPort()));
    arguments.addAll(DevAppServerArgs.get("auth_domain", config.getAuthDomain()));
    arguments.addAll(DevAppServerArgs.get("storage_path", config.getStoragePath()));
    arguments.addAll(DevAppServerArgs.get("log_level", config.getLogLevel()));
    arguments.addAll(DevAppServerArgs.get("max_module_instances", config.getMaxModuleInstances()));
    arguments.addAll(
        DevAppServerArgs.get("use_mtime_file_watcher", config.getUseMtimeFileWatcher()));
    arguments.addAll(DevAppServerArgs.get("threadsafe_override", config.getThreadsafeOverride()));
    arguments.addAll(
        DevAppServerArgs.get("python_startup_script", config.getPythonStartupScript()));
    arguments.addAll(DevAppServerArgs.get("python_startup_args", config.getPythonStartupArgs()));
    arguments.addAll(DevAppServerArgs.get("jvm_flag", config.getJvmFlags()));
    arguments.addAll(DevAppServerArgs.get("custom_entrypoint", config.getCustomEntrypoint()));
    arguments.addAll(DevAppServerArgs.get("runtime", config.getRuntime()));
    arguments.addAll(DevAppServerArgs.get("allow_skipped_files", config.getAllowSkippedFiles()));
    arguments.addAll(DevAppServerArgs.get("api_port", config.getApiPort()));
    arguments.addAll(DevAppServerArgs.get("automatic_restart", config.getAutomaticRestart()));
    arguments.addAll(
        DevAppServerArgs.get("dev_appserver_log_level", config.getDevAppserverLogLevel()));
    arguments.addAll(DevAppServerArgs.get("skip_sdk_update_check", config.getSkipSdkUpdateCheck()));
    arguments.addAll(
        DevAppServerArgs.get("default_gcs_bucket_name", config.getDefaultGcsBucketName()));
    arguments.addAll(DevAppServerArgs.get("clear_datastore", config.getClearDatastore()));
    arguments.addAll(DevAppServerArgs.get("datastore_path", config.getDatastorePath()));
    arguments.addAll(DevAppServerArgs.get("env_var", config.getEnvironment()));

    List<String> additionalArguments = config.getAdditionalArguments();
    if (additionalArguments != null) {
      arguments.addAll(additionalArguments);
    }

    try {
      runner.runV2(arguments);
    } catch (ProcessHandlerException | IOException ex) {
      throw new AppEngineException(ex);
    }
  }

  /** Stops the local development server. */
  @Override
  public void stop(StopConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);

    try {
      URL adminServerUrl =
          new URL(
              "http",
              config.getAdminHost() != null ? config.getAdminHost() : DEFAULT_ADMIN_HOST,
              config.getAdminPort() != null ? config.getAdminPort() : DEFAULT_ADMIN_PORT,
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
