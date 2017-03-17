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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.AppEngineDescriptor;
import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.appengine.api.devserver.RunConfiguration;
import com.google.cloud.tools.appengine.api.devserver.StopConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.DevAppServerArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Classic Java SDK based implementation of {@link AppEngineDevServer}.
 */
public class CloudSdkAppEngineDevServer1 implements AppEngineDevServer {

  private static final Logger log = Logger.getLogger(CloudSdkAppEngineDevServer1.class.getName());

  private final CloudSdk sdk;

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;

  public CloudSdkAppEngineDevServer1(CloudSdk sdk) {
    this.sdk = Preconditions.checkNotNull(sdk);
  }

  /**
   * Starts the local development server, synchronously or asynchronously.
   *
   * @throws AppEngineException I/O error in the Java dev server
   */
  @Override
  public void run(RunConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getServices());
    Preconditions.checkArgument(config.getServices().size() > 0);
    boolean isJava8 = determineJavaRuntimeVersion(config.getServices()).equals("java8");
    List<String> arguments = new ArrayList<>();

    List<String> jvmArguments = new ArrayList<>();
    arguments.addAll(DevAppServerArgs.get("address", config.getHost()));
    arguments.addAll(DevAppServerArgs.get("port", config.getPort()));
    if (config.getJvmFlags() != null) {
      jvmArguments.addAll(config.getJvmFlags());
    }
    arguments.addAll(DevAppServerArgs.get("default_gcs_bucket", config.getDefaultGcsBucketName()));
    // TODO: It's not really clear what the behavior here is when this is specified
    arguments.addAll(DevAppServerArgs.get("runtime", config.getRuntime()));

    // Arguments ignore by dev appserver 1
    checkAndWarnIgnored(config.getAppYamls(), "appYamls");
    checkAndWarnIgnored(config.getAdminHost(), "adminHost");
    checkAndWarnIgnored(config.getAdminPort(), "adminPort");
    checkAndWarnIgnored(config.getAuthDomain(), "authDomain");
    checkAndWarnIgnored(config.getStoragePath(), "storagePath");
    checkAndWarnIgnored(config.getLogLevel(), "logLevel");
    checkAndWarnIgnored(config.getMaxModuleInstances(), "maxModuleInstances");
    checkAndWarnIgnored(config.getUseMtimeFileWatcher(), "useMtimeFileWatcher");
    checkAndWarnIgnored(config.getThreadsafeOverride(), "threadsafeOverride");
    checkAndWarnIgnored(config.getPythonStartupScript(), "pythonStartupScript");
    checkAndWarnIgnored(config.getPythonStartupArgs(), "pythonStartupArgs");
    checkAndWarnIgnored(config.getCustomEntrypoint(), "customEntrypoint");
    checkAndWarnIgnored(config.getAllowSkippedFiles(), "allowSkippedFiles");
    checkAndWarnIgnored(config.getApiPort(), "apiPort");
    checkAndWarnIgnored(config.getAutomaticRestart(), "automaticRestart");
    checkAndWarnIgnored(config.getClearDatastore(), "clearDatastore");
    checkAndWarnIgnored(config.getDevAppserverLogLevel(), "devAppserverLogLevel");
    checkAndWarnIgnored(config.getSkipSdkUpdateCheck(), "skipSdkUpdateCheck");

    arguments.add("--allow_remote_shutdown");
    arguments.add("--disable_update_check");
    if (isJava8) {
      jvmArguments.add("-Duse_jetty9_runtime=true");
      jvmArguments.add("-D--enable_all_permissions=true");
      arguments.add("--no_java_agent");
    } else {
      // Add in the appengine agent
      String appengineAgentJar = sdk.getJavaAppEngineSdkPath().resolve("agent/appengine-agent.jar").toAbsolutePath().toString();
      jvmArguments.add("-javaagent:" + appengineAgentJar);
    }
    for (File service : config.getServices()) {
      arguments.add(service.toPath().toString());
    }
    try {
      sdk.runDevAppServer1Command(jvmArguments, arguments);
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
    HttpURLConnection connection = null;
    try {
      URL adminServerUrl = new URL(
              "http",
              configuration.getAdminHost() != null
              ? configuration.getAdminHost() : DEFAULT_HOST,
              DEFAULT_PORT,
              "/_ah/admin/quit");
      connection = (HttpURLConnection) adminServerUrl.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setRequestMethod("POST");
      connection.getOutputStream().write('\n');
      byte[] responses = ByteStreams.toByteArray(connection.getInputStream());
      connection.disconnect();
      int responseCode = connection.getResponseCode();
      if (responseCode < 200 || responseCode > 299) {
        throw new AppEngineException(
                "The development server responded with " + connection.getResponseMessage() + ".");
      }
    } catch (IOException e) {
      throw new AppEngineException(e);
    } finally {
      if (connection != null) {
        try {
          connection.getInputStream().close();
        } catch (IOException ex) {
          throw new AppEngineException(ex);
        }
      }
    }
  }

  @VisibleForTesting
  void checkAndWarnIgnored(Object valueToIgnore, String propertyName) {
    if (valueToIgnore != null) {
      log.warning(propertyName + " will be ignored by Dev Appserver v1");
    }
  }

  @VisibleForTesting
  String determineJavaRuntimeVersion(List<File> services) {
    boolean java8Detected = false;
    boolean java7Detected = false;
    for (File serviceDirectory : services) {
      File appengineWebXml = new File(serviceDirectory, "WEB-INF/appengine-web.xml");
      try (FileInputStream is = new FileInputStream(appengineWebXml)) {
        if (AppEngineDescriptor.parse(is).isJava8()) {
          java8Detected = true;
        }
        else {
          java7Detected = true;
        }
      } catch (IOException e) {
        throw new AppEngineException(e);
      }
    }
    if (java8Detected && java7Detected) {
      log.warning("Mixed runtimes java7/java8 detected, will use java8 settings");
    }
    return java8Detected ? "java8" : "java7";
  }
}
