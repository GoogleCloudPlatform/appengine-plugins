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
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

/** Classic Java SDK based implementation of {@link AppEngineDevServer}. */
public class CloudSdkAppEngineDevServer1 implements AppEngineDevServer {

  private static final Logger log = Logger.getLogger(CloudSdkAppEngineDevServer1.class.getName());

  private final CloudSdk sdk;
  private final DevAppServerRunner runner;

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;

  public CloudSdkAppEngineDevServer1(CloudSdk sdk, DevAppServerRunner runner) {
    this.sdk = Preconditions.checkNotNull(sdk);
    this.runner = Preconditions.checkNotNull(runner);
  }

  /**
   * Starts the local development server, synchronously or asynchronously.
   *
   * @throws AppEngineException I/O error in the Java dev server
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  @Override
  public void run(RunConfiguration config) throws AppEngineException {
    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getServices());
    Preconditions.checkArgument(config.getServices().size() > 0);
    List<String> arguments = new ArrayList<>();

    List<String> jvmArguments = new ArrayList<>();
    arguments.addAll(DevAppServerArgs.get("address", config.getHost()));
    arguments.addAll(DevAppServerArgs.get("port", config.getPort()));
    if (Boolean.TRUE.equals(config.getAutomaticRestart())) {
      jvmArguments.add("-Dappengine.fullscan.seconds=1");
    }
    if (config.getJvmFlags() != null) {
      jvmArguments.addAll(config.getJvmFlags());
    }

    arguments.addAll(DevAppServerArgs.get("default_gcs_bucket", config.getDefaultGcsBucketName()));

    // Arguments ignored by dev appserver 1
    checkAndWarnIgnored(config.getAdminHost(), "adminHost");
    checkAndWarnIgnored(config.getAdminPort(), "adminPort");
    checkAndWarnIgnored(config.getAllowSkippedFiles(), "allowSkippedFiles");
    checkAndWarnIgnored(config.getApiPort(), "apiPort");
    checkAndWarnIgnored(config.getAuthDomain(), "authDomain");
    checkAndWarnIgnored(config.getClearDatastore(), "clearDatastore");
    checkAndWarnIgnored(config.getCustomEntrypoint(), "customEntrypoint");
    checkAndWarnIgnored(config.getDatastorePath(), "datastorePath");
    checkAndWarnIgnored(config.getDevAppserverLogLevel(), "devAppserverLogLevel");
    checkAndWarnIgnored(config.getLogLevel(), "logLevel");
    checkAndWarnIgnored(config.getMaxModuleInstances(), "maxModuleInstances");
    checkAndWarnIgnored(config.getPythonStartupArgs(), "pythonStartupArgs");
    checkAndWarnIgnored(config.getPythonStartupScript(), "pythonStartupScript");
    checkAndWarnIgnored(config.getRuntime(), "runtime");
    checkAndWarnIgnored(config.getSkipSdkUpdateCheck(), "skipSdkUpdateCheck");
    checkAndWarnIgnored(config.getStoragePath(), "storagePath");
    checkAndWarnIgnored(config.getThreadsafeOverride(), "threadsafeOverride");
    checkAndWarnIgnored(config.getUseMtimeFileWatcher(), "useMtimeFileWatcher");

    arguments.add("--allow_remote_shutdown");
    arguments.add("--disable_update_check");
    List<String> additionalArguments = config.getAdditionalArguments();
    if (additionalArguments != null) {
      arguments.addAll(additionalArguments);
    }

    boolean isJava8 = isJava8(config.getServices());

    if (isJava8) {
      jvmArguments.add("-Duse_jetty9_runtime=true");
      jvmArguments.add("-D--enable_all_permissions=true");
      arguments.add("--no_java_agent");
    } else {
      // Add in the appengine agent
      String appengineAgentJar =
          sdk.getAppEngineSdkForJavaPath()
              .resolve("agent/appengine-agent.jar")
              .toAbsolutePath()
              .toString();
      jvmArguments.add("-javaagent:" + appengineAgentJar);
    }
    for (File service : config.getServices()) {
      arguments.add(service.toPath().toString());
    }

    Map<String, String> appEngineEnvironment =
        getAllAppEngineWebXmlEnvironmentVariables(config.getServices());
    if (!appEngineEnvironment.isEmpty()) {
      log.info(
          "Setting appengine-web.xml configured environment variables: "
              + Joiner.on(",").withKeyValueSeparator("=").join(appEngineEnvironment));
    }

    String gaeRuntime = getGaeRuntimeJava(isJava8);
    appEngineEnvironment.putAll(getLocalAppEngineEnvironmentVariables(gaeRuntime));

    if (config.getEnvironment() != null) {
      appEngineEnvironment.putAll(config.getEnvironment());
    }

    try {
      File workingDirectory = null;
      if (config.getServices().size() == 1) {
        workingDirectory = config.getServices().get(0);
      }
      runner.runV1(jvmArguments, arguments, appEngineEnvironment, workingDirectory);
    } catch (ProcessHandlerException | IOException ex) {
      throw new AppEngineException(ex);
    }
  }

  /** Stops the local development server. */
  @Override
  public void stop(StopConfiguration configuration) throws AppEngineException {
    Preconditions.checkNotNull(configuration);
    HttpURLConnection connection = null;
    String adminHost =
        configuration.getAdminHost() != null ? configuration.getAdminHost() : DEFAULT_HOST;
    int adminPort =
        configuration.getAdminPort() != null ? configuration.getAdminPort() : DEFAULT_PORT;
    URL adminServerUrl = null;
    try {
      adminServerUrl = new URL("http", adminHost, adminPort, "/_ah/admin/quit");
      connection = (HttpURLConnection) adminServerUrl.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setRequestMethod("POST");
      connection.getOutputStream().write('\n');
      connection.disconnect();
      int responseCode = connection.getResponseCode();
      if (responseCode < 200 || responseCode > 299) {
        throw new AppEngineException(
            adminServerUrl + " responded with " + connection.getResponseMessage() + ".");
      }
    } catch (IOException ex) {
      throw new AppEngineException("Error connecting to " + adminServerUrl, ex);
    } finally {
      if (connection != null) {
        try {
          connection.getInputStream().close();
        } catch (IOException ignore) {
          // ignored
        }
      }
    }
  }

  @VisibleForTesting
  void checkAndWarnIgnored(Object propertyToIgnore, String propertyName) {
    if (propertyToIgnore != null) {
      log.warning(
          propertyName
              + " only applies to Dev Appserver v2 and will be ignored by Dev Appserver v1");
    }
  }

  /**
   * This method tries to guess the runtime based on the appengine-web.xml of all services that are
   * expected to run.
   *
   * @param services a list of app engine standard service directories
   * @return {@code false} if only java7 modules are found or {@code true} is at least one java8
   *     module is found (i.e. pure java8 or mixed java7/java8)
   */
  @VisibleForTesting
  boolean isJava8(List<File> services) throws AppEngineException {
    boolean java8Detected = false;
    boolean java7Detected = false;
    for (File serviceDirectory : services) {
      Path appengineWebXml = serviceDirectory.toPath().resolve("WEB-INF/appengine-web.xml");
      try (InputStream is = Files.newInputStream(appengineWebXml)) {
        if (AppEngineDescriptor.parse(is).isJava8()) {
          java8Detected = true;
        } else {
          java7Detected = true;
        }
      } catch (IOException | SAXException ex) {
        throw new AppEngineException(ex);
      }
    }
    if (java8Detected && java7Detected) {
      log.warning("Mixed runtimes java7/java8 detected, will use java8 settings");
    }
    return java8Detected;
  }

  private static Map<String, String> getAllAppEngineWebXmlEnvironmentVariables(List<File> services)
      throws AppEngineException {
    Map<String, String> allAppEngineEnvironment = Maps.newHashMap();
    for (File serviceDirectory : services) {
      Path appengineWebXml = serviceDirectory.toPath().resolve("WEB-INF/appengine-web.xml");
      try (InputStream is = Files.newInputStream(appengineWebXml)) {
        AppEngineDescriptor appEngineDescriptor = AppEngineDescriptor.parse(is);
        Map<String, String> appEngineEnvironment = appEngineDescriptor.getEnvironment();
        if (appEngineEnvironment != null) {
          checkAndWarnDuplicateEnvironmentVariables(
              appEngineEnvironment, allAppEngineEnvironment, appEngineDescriptor.getServiceId());

          allAppEngineEnvironment.putAll(appEngineEnvironment);
        }
      } catch (IOException | SAXException ex) {
        throw new AppEngineException(ex);
      }
    }
    return allAppEngineEnvironment;
  }

  /**
   * Gets a {@code Map<String, String>} of the environment variables for running the {@link
   * AppEngineDevServer}.
   *
   * @param gaeRuntime the runtime ID to set the environment variable GAE_RUNTIME to
   * @return {@code Map<String, String>} that maps from the environment variable name to its value
   */
  @VisibleForTesting
  static Map<String, String> getLocalAppEngineEnvironmentVariables(String gaeRuntime) {
    Map<String, String> environment = Maps.newHashMap();

    String gaeEnv = "localdev";
    environment.put("GAE_ENV", gaeEnv);
    environment.put("GAE_RUNTIME", gaeRuntime);

    return environment;
  }

  /**
   * Gets the App Engine runtime ID for Java runtimes.
   *
   * @param isJava8 if {@code true}, use Java 8; otherwise, use Java 7
   * @return "java8" if {@code isJava8} is true; otherwise, returns "java7"
   */
  @VisibleForTesting
  static String getGaeRuntimeJava(boolean isJava8) {
    return isJava8 ? "java8" : "java7";
  }

  private static void checkAndWarnDuplicateEnvironmentVariables(
      Map<String, String> newEnvironment, Map<String, String> existingEnvironment, String service) {
    for (String key : newEnvironment.keySet()) {
      if (existingEnvironment.containsKey(key)) {
        log.warning(
            String.format(
                "Found duplicate environment variable key '%s' across "
                    + "appengine-web.xml files in the following service: %s",
                key, service));
      }
    }
  }
}
