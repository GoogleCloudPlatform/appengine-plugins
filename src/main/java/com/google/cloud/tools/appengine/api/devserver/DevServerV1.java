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

package com.google.cloud.tools.appengine.api.devserver;

import com.google.cloud.tools.appengine.AppEngineDescriptor;
import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.cloudsdk.DevAppServerRunner;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.DevAppServerArgs;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
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
import javax.annotation.Nullable;
import org.xml.sax.SAXException;

/** Classic Java SDK based implementation of {@link DevServer}. */
public class DevServerV1 implements DevServer {

  private static final Logger log = Logger.getLogger(DevServerV1.class.getName());

  private final CloudSdk sdk;
  private final DevAppServerRunner runner;

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;

  public DevServerV1(CloudSdk sdk, DevAppServerRunner runner) {
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
    arguments.addAll(DevAppServerArgs.get("application", config.getProjectId()));

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

    boolean isSandboxEnforced = isSandboxEnforced(config.getServices());

    if (!isSandboxEnforced) {
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
    for (Path service : config.getServices()) {
      arguments.add(service.toString());
    }

    Map<String, String> appEngineEnvironment =
        getAllAppEngineWebXmlEnvironmentVariables(config.getServices());
    if (!appEngineEnvironment.isEmpty()) {
      log.info(
          "Setting appengine-web.xml configured environment variables: "
              + Joiner.on(",").withKeyValueSeparator("=").join(appEngineEnvironment));
    }

    String gaeRuntime = getGaeRuntimeJava(config.getServices());
    appEngineEnvironment.putAll(getLocalAppEngineEnvironmentVariables(gaeRuntime));

    Map<String, String> configEnvironment = config.getEnvironment();
    if (configEnvironment != null) {
      appEngineEnvironment.putAll(configEnvironment);
    }

    try {
      Path workingDirectory = null;
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
  void checkAndWarnIgnored(@Nullable Object propertyToIgnore, String propertyName) {
    if (propertyToIgnore != null) {
      log.warning(
          propertyName
              + " only applies to Dev Appserver v2 and will be ignored by Dev Appserver v1");
    }
  }

  /**
   * This method determines if the dev app server should run with sandbox restrictions based on the
   * runtime parsed from appengine-web.xml on all services. If one service can run with no sandbox
   * restrictions, all other services will also run locally with no restrictions.
   *
   * @param services a list of app engine standard service directories
   * @return {@code false} if there no need to enforce the sandbox restrictions.
   */
  @VisibleForTesting
  boolean isSandboxEnforced(List<Path> services) throws AppEngineException {
    boolean relaxSandbox = false;
    boolean enforceSandbox = false;
    for (Path serviceDirectory : services) {
      Path appengineWebXml = serviceDirectory.resolve("WEB-INF/appengine-web.xml");
      try (InputStream is = Files.newInputStream(appengineWebXml)) {
        if (AppEngineDescriptor.parse(is).isSandboxEnforced()) {
          enforceSandbox = true;
        } else {
          relaxSandbox = true;
        }
      } catch (IOException | SAXException ex) {
        throw new AppEngineException(ex);
      }
    }
    if (relaxSandbox && enforceSandbox) {
      log.warning("Mixed runtimes detected, will not enforce sandbox restrictions.");
    }
    return !relaxSandbox;
  }

  private static Map<String, String> getAllAppEngineWebXmlEnvironmentVariables(List<Path> services)
      throws AppEngineException {
    Map<String, String> allAppEngineEnvironment = Maps.newHashMap();
    for (Path serviceDirectory : services) {
      Path appengineWebXml = serviceDirectory.resolve("WEB-INF/appengine-web.xml");
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
   * DevServer}.
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
   * Gets the App Engine runtime ID for Java runtimes. Order of precedence is 11 > 8 > 7. With
   * default being 7.
   */
  @VisibleForTesting
  static String getGaeRuntimeJava(List<Path> services) throws AppEngineException {
    boolean hasJava8 = false;
    for (Path serviceDirectory : services) {
      Path appengineWebXml = serviceDirectory.resolve("WEB-INF/appengine-web.xml");
      try (InputStream is = Files.newInputStream(appengineWebXml)) {
        String serviceRuntime = AppEngineDescriptor.parse(is).getRuntime();
        if (serviceRuntime.startsWith("java11")) {
          return "java11";
        } else if (serviceRuntime.startsWith("java8")) {
          hasJava8 = true;
        }
      } catch (IOException | SAXException ex) {
        throw new AppEngineException(ex);
      }
    }
    if (hasJava8) {
      return "java8";
    }
    return "java7";
  }

  private static void checkAndWarnDuplicateEnvironmentVariables(
      Map<String, String> newEnvironment,
      Map<String, String> existingEnvironment,
      @Nullable String service) {
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
