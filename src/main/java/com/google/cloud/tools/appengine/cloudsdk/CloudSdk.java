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
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.DefaultProcessRunner;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ExitCodeRecorderProcessExitListener;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunner;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.StringBuilderProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.WaitingProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessStartListener;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkComponent;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Cloud SDK CLI wrapper.
 */
public class CloudSdk {
  private static final int MINIMUM_VERSION = 131;
  private static final Logger logger = Logger.getLogger(CloudSdk.class.toString());
  private static final Joiner WHITESPACE_JOINER = Joiner.on(" ");

  private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
  private static final String GCLOUD = "bin/gcloud";
  private static final String DEV_APPSERVER_PY = "bin/dev_appserver.py";
  private static final String JAVA_APPENGINE_SDK_PATH =
      "platform/google_appengine/google/appengine/tools/java/lib";
  private static final String JAVA_TOOLS_JAR = "appengine-tools-api.jar";
  private static final Map<String, Path> JAR_LOCATIONS = new HashMap<>();
  private static final String WINDOWS_BUNDLED_PYTHON = "platform/bundledpython/python.exe";

  private final Path sdkPath;
  private final ProcessRunner processRunner;
  private final String appCommandMetricsEnvironment;
  private final String appCommandMetricsEnvironmentVersion;
  @Nullable
  private final File appCommandCredentialFile;
  private final String appCommandOutputFormat;
  private final WaitingProcessOutputLineListener runDevAppServerWaitListener;

  private CloudSdk(Path sdkPath,
                   String appCommandMetricsEnvironment,
                   String appCommandMetricsEnvironmentVersion,
                   @Nullable File appCommandCredentialFile,
                   String appCommandOutputFormat,
                   ProcessRunner processRunner,
                   WaitingProcessOutputLineListener runDevAppServerWaitListener) {
    this.sdkPath = sdkPath;
    this.appCommandMetricsEnvironment = appCommandMetricsEnvironment;
    this.appCommandMetricsEnvironmentVersion = appCommandMetricsEnvironmentVersion;
    this.appCommandCredentialFile = appCommandCredentialFile;
    this.appCommandOutputFormat = appCommandOutputFormat;
    this.processRunner = processRunner;
    this.runDevAppServerWaitListener = runDevAppServerWaitListener;

    // Populate jar locations.
    // TODO(joaomartins): Consider case where SDK doesn't contain these jars. Only App Engine
    // SDK does.
    JAR_LOCATIONS.put("servlet-api.jar",
        getJavaAppEngineSdkPath().resolve("shared/servlet-api.jar"));
    JAR_LOCATIONS.put("jsp-api.jar", getJavaAppEngineSdkPath().resolve("shared/jsp-api.jar"));
    JAR_LOCATIONS.put(JAVA_TOOLS_JAR,
        sdkPath.resolve(JAVA_APPENGINE_SDK_PATH).resolve(JAVA_TOOLS_JAR));
  }

  /**
   * Uses the process runner to execute the gcloud app command with the provided arguments.
   *
   * @param args The arguments to pass to gcloud command
   * @throws ProcessRunnerException when there is an issue running the gcloud process
   */
  public void runAppCommand(List<String> args) throws ProcessRunnerException {
    runGcloudCommand(args, "app");
  }

  /**
   * Runs a source command, i.e., gcloud beta debug source ...
   *
   * @param args The command arguments, including the main command and flags. For example,
   *             gen-repo-info-file --output_directory [OUTPUT_DIRECTORY] etc.
   * @throws ProcessRunnerException when there is an issue running the gcloud process
   */
  public void runSourceCommand(List<String> args) throws ProcessRunnerException {
    runDebugCommand(args, "source");
  }

  private void runDebugCommand(List<String> args, String group) throws ProcessRunnerException {
    runGcloudCommand(args, "beta", "debug", group);
  }

  private void runGcloudCommand(List<String> args, String... topLevelCommand)
      throws ProcessRunnerException {
    validateCloudSdk();

    List<String> command = new ArrayList<>();
    command.add(getGCloudPath().toString());
    for (String commandToken : topLevelCommand) {
      command.add(commandToken);
    }
    command.addAll(args);

    command.add("--quiet");
    command.addAll(GcloudArgs.get("format", appCommandOutputFormat));

    Map<String, String> environment = Maps.newHashMap();
    if (appCommandCredentialFile != null) {
      command.addAll(GcloudArgs.get("credential-file-override", appCommandCredentialFile));
      environment.put("CLOUDSDK_APP_USE_GSUTIL", "0");
    }
    if (appCommandMetricsEnvironment != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT", appCommandMetricsEnvironment);
    }
    if (appCommandMetricsEnvironmentVersion != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT_VERSION", appCommandMetricsEnvironmentVersion);
    }
    // This is to ensure IDE credentials get correctly passed to the gcloud commands, in Windows.
    // It's a temporary workaround until a fix is released.
    // https://github.com/GoogleCloudPlatform/google-cloud-intellij/issues/985
    if (System.getProperty("os.name").contains("Windows")) {
      environment.put("CLOUDSDK_APP_NUM_FILE_UPLOAD_PROCESSES", "1");
    }
    logCommand(command);
    processRunner.setEnvironment(environment);
    processRunner.run(command.toArray(new String[command.size()]));
  }

  // Runs a gcloud command synchronously, with a new ProcessRunner. This method is intended to be
  // used for the execution of short-running gcloud commands, especially when we need to do some
  // additional processing of the gcloud command's output before returning. In all other cases, this
  // class's main configured ProcessRunner should be used.
  private String runSynchronousGcloudCommand(List<String> args)
      throws ProcessRunnerException {
    validateCloudSdkLocation();
    
    StringBuilderProcessOutputLineListener stdOutListener =
        new StringBuilderProcessOutputLineListener();
    ExitCodeRecorderProcessExitListener exitListener = new ExitCodeRecorderProcessExitListener();

    // instantiate a separate synchronous process runner
    ProcessRunner runner = new DefaultProcessRunner(
        false,                                                       /* async */
        ImmutableList.<ProcessExitListener>of(exitListener),         /* exitListeners */
        ImmutableList.<ProcessStartListener>of(),                    /* startListeners */
        ImmutableList.<ProcessOutputLineListener>of(stdOutListener), /* stdOutLineListeners */
        ImmutableList.<ProcessOutputLineListener>of());              /* stdErrLineListeners */

    // build and run the command
    List<String> command = new ImmutableList.Builder<String>()
        .add(getGCloudPath().toString())
        .addAll(args)
        .build();

    runner.run(command.toArray(new String[command.size()]));

    if (exitListener.getMostRecentExitCode() != null
        && !exitListener.getMostRecentExitCode().equals(0)) {
      throw new ProcessRunnerException("Process exited unsuccessfully");
    }

    return stdOutListener.toString();
  }

  /**
   * Uses the process runner to execute a dev_appserver.py command.
   *
   * @param args the arguments to pass to dev_appserver.py
   * @throws InvalidPathException      when Python can't be located
   * @throws ProcessRunnerException    when process runner encounters an error
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws AppEngineException        when dev_appserver.py cannot be found
   */
  public void runDevAppServerCommand(List<String> args) throws ProcessRunnerException {
    runDevAppServerCommand(args, new HashMap<String, String>());
  }

  /**
   * Uses the process runner to execute a dev_appserver.py command.
   *
   * @param args the arguments to pass to dev_appserver.py
   * @param environment map of environment variables to set for the dev_appserver process
   * @throws InvalidPathException      when Python can't be located
   * @throws ProcessRunnerException    when process runner encounters an error
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws AppEngineException        when dev_appserver.py cannot be found
   */
  public void runDevAppServerCommand(List<String> args, Map<String,String> environment)
      throws ProcessRunnerException {
    Preconditions.checkNotNull(environment);
    validateCloudSdk();

    List<String> command = new ArrayList<>();

    if (IS_WINDOWS) {
      command.add(getWindowsPythonPath().toString());
    }

    command.add(getDevAppServerPath().toString());
    command.addAll(args);

    logCommand(command);

    // set quiet mode and consequently auto-install of app-engine-java component
    environment.put("CLOUDSDK_CORE_DISABLE_PROMPTS", "1");

    processRunner.setEnvironment(environment);
    processRunner.run(command.toArray(new String[command.size()]));

    // wait for start if configured
    if (runDevAppServerWaitListener != null) {
      runDevAppServerWaitListener.await();
    }
  }

  /**
   * Executes an App Engine SDK CLI command.
   *
   * @throws AppEngineJavaComponentsNotInstalledException when the App Engine Java components are
   *                                                      not installed in the Cloud SDK
   */
  public void runAppCfgCommand(List<String> args) throws ProcessRunnerException {
    validateAppEngineJavaComponents();

    // AppEngineSdk requires this system property to be set.
    System.setProperty("appengine.sdk.root", getJavaAppEngineSdkPath().toString());

    List<String> command = new ArrayList<>();
    command.add(
        Paths.get(System.getProperty("java.home")).resolve("bin/java").toAbsolutePath().toString());
    command.add("-cp");
    command.add(JAR_LOCATIONS.get(JAVA_TOOLS_JAR).toString());
    command.add("com.google.appengine.tools.admin.AppCfg");
    command.addAll(args);

    logCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
  }

  /**
   * Returns the version of the Cloud SDK installation. Unlike other methods in this class that call
   * gcloud, this method always uses a synchronous ProcessRunner and will block until the gcloud
   * process returns.
   *
   * @throws ProcessRunnerException when process runner encounters an error
   */
  public CloudSdkVersion getVersion() throws ProcessRunnerException {
    // gcloud info --format="value(basic.version)"
    List<String> command = new ImmutableList.Builder<String>()
        .add("info")
        .addAll(GcloudArgs.get("format", "value(basic.version)"))
        .build();

    return new CloudSdkVersion(runSynchronousGcloudCommand(command));
  }

  /**
   * Returns the list of Cloud SDK Components and their settings, reported by the current gcloud
   * installation. Unlike other methods in this class that call gcloud, this method always uses a
   * synchronous ProcessRunner and will block until the gcloud process returns.
   *
   * @throws ProcessRunnerException when process runner encounters an error
   * @throws JsonSyntaxException when the cloud SDK output cannot be parsed
   */
  public List<CloudSdkComponent> getComponents()
      throws ProcessRunnerException, JsonSyntaxException {
    validateCloudSdk();

    // gcloud components list --show-versions --format=json
    List<String> command = new ImmutableList.Builder<String>()
        .add("components", "list")
        .addAll(GcloudArgs.get("show-versions", true))
        .addAll(GcloudArgs.get("format", "json"))
        .build();

    String componentsJson = runSynchronousGcloudCommand(command);
    return CloudSdkComponent.fromJsonList(componentsJson);
  }

  private void logCommand(List<String> command) {
    logger.info("submitting command: " + WHITESPACE_JOINER.join(command));
  }

  public Path getSdkPath() {
    return sdkPath;
  }

  private Path getGCloudPath() {
    String gcloud = GCLOUD;
    if (IS_WINDOWS) {
      gcloud += ".cmd";
    }
    return getSdkPath().resolve(gcloud);
  }

  private Path getDevAppServerPath() {
    return getSdkPath().resolve(DEV_APPSERVER_PY);
  }

  public Path getJavaAppEngineSdkPath() {
    return getSdkPath().resolve(JAVA_APPENGINE_SDK_PATH);
  }

  // https://github.com/GoogleCloudPlatform/appengine-plugins-core/issues/189
  @VisibleForTesting
  Path getWindowsPythonPath() {
    String cloudSdkPython = System.getenv("CLOUDSDK_PYTHON");
    if (cloudSdkPython != null) {
      Path cloudSdkPythonPath = Paths.get(cloudSdkPython);
      if (Files.exists(cloudSdkPythonPath)) {
        return cloudSdkPythonPath;
      } else {
        throw new InvalidPathException(cloudSdkPython, "python binary not in specified location");
      }
    }

    Path pythonPath = getSdkPath().resolve(WINDOWS_BUNDLED_PYTHON);
    if (Files.exists(pythonPath)) {
      return pythonPath;
    } else {
      return Paths.get("python");
    }

  }

  /**
   * Gets the file system location for an SDK jar.
   *
   * @param jarName the jar file name. For example, "servlet-api.jar"
   * @return the path in the file system
   */
  public Path getJarPath(String jarName) {
    return JAR_LOCATIONS.get(jarName);
  }

  /**
   * Checks whether the Cloud SDK path and version are valid.
   *
   * @throws CloudSdkNotFoundException when an up-to-date Cloud SDK is not installed where expected
   */
  public void validateCloudSdk() {
    validateCloudSdkLocation();
    validateCloudSdkVersion();
  }

  private void validateCloudSdkVersion() {
    try {
      CloudSdkVersion version = getVersion();
      if (version.getMajorVersion() < MINIMUM_VERSION) {
        throw new CloudSdkOutOfDateException(
            "Cloud SDK version " + version + " is too old. Please update.");        
      }
    } catch (ProcessRunnerException ex) {
      throw new CloudSdkNotFoundException(ex);              
    }
  }

  private void validateCloudSdkLocation() {
    if (sdkPath == null) {
      throw new CloudSdkNotFoundException("Validation Error: Cloud SDK path is null");
    }
    if (!Files.isDirectory(sdkPath)) {
      throw new CloudSdkNotFoundException(
          "Validation Error: SDK location '" + sdkPath + "' is not a directory.");
    }
    if (!Files.isRegularFile(getGCloudPath())) {
      throw new CloudSdkNotFoundException(
          "Validation Error: gcloud location '" + getGCloudPath() + "' is not a file.");
    }
    if (!Files.isRegularFile(getDevAppServerPath())) {
      throw new CloudSdkNotFoundException(
          "Validation Error: dev_appserver.py location '"
              + getDevAppServerPath() + "' is not a file.");
    }
  }

  /**
   * Checks whether the App Engine Java components are installed in the expected location in the
   * Cloud SDK.
   *
   * @throws AppEngineJavaComponentsNotInstalledException when the App Engine Java components are
   *                                                      not installed in the Cloud SDK
   */
  public void validateAppEngineJavaComponents()
      throws AppEngineJavaComponentsNotInstalledException {
    if (!Files.isDirectory(getJavaAppEngineSdkPath())) {
      throw new AppEngineJavaComponentsNotInstalledException(
          "Validation Error: Java App Engine components not installed."
              + " Fix by running 'gcloud components install app-engine-java' on command-line.");
    }
    if (!Files.isRegularFile(JAR_LOCATIONS.get(JAVA_TOOLS_JAR))) {
      throw new AppEngineJavaComponentsNotInstalledException(
          "Validation Error: Java Tools jar location '"
              + JAR_LOCATIONS.get(JAVA_TOOLS_JAR) + "' is not a file.");
    }
  }

  @VisibleForTesting
  WaitingProcessOutputLineListener getRunDevAppServerWaitListener() {
    return runDevAppServerWaitListener;
  }

  public static class Builder {
    private Path sdkPath;
    private String appCommandMetricsEnvironment;
    private String appCommandMetricsEnvironmentVersion;
    @Nullable
    private File appCommandCredentialFile;
    private String appCommandOutputFormat;
    private boolean async = false;
    private List<ProcessOutputLineListener> stdOutLineListeners = new ArrayList<>();
    private List<ProcessOutputLineListener> stdErrLineListeners = new ArrayList<>();
    private List<ProcessExitListener> exitListeners = new ArrayList<>();
    private List<ProcessStartListener> startListeners = new ArrayList<>();
    private List<CloudSdkResolver> resolvers;
    private int runDevAppServerWaitSeconds;
    private boolean inheritProcessOutput;

    /**
     * The home directory of Google Cloud SDK.
     *
     * @param sdkPath the root path for the Cloud SDK
     */
    public Builder sdkPath(Path sdkPath) {
      if (sdkPath != null) {
        this.sdkPath = sdkPath;
      }
      return this;
    }

    /**
     * The metrics environment.
     */
    public Builder appCommandMetricsEnvironment(String appCommandMetricsEnvironment) {
      this.appCommandMetricsEnvironment = appCommandMetricsEnvironment;
      return this;
    }

    /**
     * The metrics environment version.
     */
    public Builder appCommandMetricsEnvironmentVersion(
        String appCommandMetricsEnvironmentVersion) {
      this.appCommandMetricsEnvironmentVersion = appCommandMetricsEnvironmentVersion;
      return this;
    }

    /**
     * Sets the path the credential override file.
     */
    public Builder appCommandCredentialFile(File appCommandCredentialFile) {
      this.appCommandCredentialFile = appCommandCredentialFile;
      return this;
    }

    /**
     * Sets the format for printing command output resources. The default is a command-specific
     * human-friendly output format. The supported formats are: csv, default, flattened, json, list,
     * multi, none, table, text, value, yaml. For more details run $ gcloud topic formats.
     */
    public Builder appCommandOutputFormat(String appCommandOutputFormat) {
      this.appCommandOutputFormat = appCommandOutputFormat;
      return this;
    }

    /**
     * Whether to run commands asynchronously.
     */
    public Builder async(boolean async) {
      this.async = async;
      return this;
    }

    /**
     * Adds a client consumer of process standard output. If none, output will be inherited by
     * parent process.
     */
    public Builder addStdOutLineListener(ProcessOutputLineListener stdOutLineListener) {
      this.stdOutLineListeners.add(stdOutLineListener);
      return this;
    }

    /**
     * Adds a client consumer of process error output. If none, output will be inherited by parent
     * process.
     */
    public Builder addStdErrLineListener(ProcessOutputLineListener stdErrLineListener) {
      this.stdErrLineListeners.add(stdErrLineListener);
      return this;
    }

    /**
     * The client listener of the process exit with code.
     */
    public Builder exitListener(ProcessExitListener exitListener) {
      this.exitListeners.clear();
      this.exitListeners.add(exitListener);
      return this;
    }

    /**
     * The client listener of the process start. Allows access to the underlying process.
     */
    public Builder startListener(ProcessStartListener startListener) {
      this.startListeners.clear();
      this.startListeners.add(startListener);
      return this;
    }

    /**
     * When run asynchronously, configure the Dev App Server command to wait for successful start of
     * the server. Setting this will force process output not to be inherited by the caller.
     *
     * @param runDevAppServerWaitSeconds Number of seconds to wait > 0.
     */
    public Builder runDevAppServerWait(int runDevAppServerWaitSeconds) {
      this.runDevAppServerWaitSeconds = runDevAppServerWaitSeconds;
      return this;
    }

    /**
     * Causes the generated gcloud or devappserver subprocess to inherit the calling process's
     * stdout and stderr.
     *
     * <p>If this is set to {@code true}, no stdout and stderr listeners can be specified.
     *
     * @param inheritProcessOutput If true, stdout and stderr are redirected to the parent process
     */
    public Builder inheritProcessOutput(boolean inheritProcessOutput) {
      this.inheritProcessOutput = inheritProcessOutput;
      return this;
    }

    /**
     * Create a new instance of {@link CloudSdk}.
     *
     * <p>If {@code sdkPath} is not set, this method will look for the SDK in known install
     * locations.
     */
    public CloudSdk build() {

      // Default SDK path
      if (sdkPath == null) {
        sdkPath = discoverSdkPath();
      }

      // Verify there aren't listeners if subprocess inherits output.
      // If output is inherited, then listeners won't receive anything.
      if (inheritProcessOutput
          && (stdOutLineListeners.size() > 0 || stdErrLineListeners.size() > 0)) {
        throw new AppEngineException("You cannot specify subprocess output inheritance and"
            + " output listeners.");
      }

      // Construct process runner.
      ProcessRunner processRunner;
      WaitingProcessOutputLineListener runDevAppServerWaitListener = null;
      if (stdOutLineListeners.size() > 0 || stdErrLineListeners.size() > 0) {
        // Configure listeners for async dev app server start with waiting.
        if (async && runDevAppServerWaitSeconds > 0) {
          runDevAppServerWaitListener = new WaitingProcessOutputLineListener(
              ".*(Dev App Server is now running|INFO:oejs\\.Server:main: Started).*",
              runDevAppServerWaitSeconds);

          stdOutLineListeners.add(runDevAppServerWaitListener);
          stdErrLineListeners.add(runDevAppServerWaitListener);
          exitListeners.add(0, runDevAppServerWaitListener);
        }

        processRunner = new DefaultProcessRunner(async, exitListeners, startListeners,
            stdOutLineListeners, stdErrLineListeners);
      } else {
        processRunner = new DefaultProcessRunner(async, exitListeners, startListeners,
            inheritProcessOutput);
      }

      return new CloudSdk(sdkPath, appCommandMetricsEnvironment,
          appCommandMetricsEnvironmentVersion, appCommandCredentialFile, appCommandOutputFormat,
          processRunner, runDevAppServerWaitListener);
    }

    /**
     * Attempt to find the Google Cloud SDK in various places.
     *
     * @return the path to the root of the Google Cloud SDK
     * @throws AppEngineException if not found
     */
    @Nonnull
    private Path discoverSdkPath() {
      for (CloudSdkResolver resolver : getResolvers()) {
        try {
          Path discoveredSdkPath = resolver.getCloudSdkPath();
          if (discoveredSdkPath != null) {
            return discoveredSdkPath;
          }
        } catch (RuntimeException ex) {
          // prevent interference from exceptions in other resolvers
          logger.log(Level.SEVERE, resolver.getClass().getName()
              + ": exception thrown when searching for Google Cloud SDK", ex);
        }
      }
      throw new AppEngineException("Google Cloud SDK path was not provided and could not be"
          + " found in any known install locations.");
    }

    /**
     * Return the configured SDK resolvers.
     */
    @VisibleForTesting
    public List<CloudSdkResolver> getResolvers() {
      List<CloudSdkResolver> resolvers;
      if (this.resolvers != null) {
        resolvers = new ArrayList<>(this.resolvers);
      } else {
        // Explicitly specify classloader rather than use the Thread Context Class Loader
        ServiceLoader<CloudSdkResolver> services =
            ServiceLoader.load(CloudSdkResolver.class, getClass().getClassLoader());
        resolvers = Lists.newArrayList(services);
        // Explicitly add the PATH-based resolver
        resolvers.add(new PathResolver());
      }
      Collections.sort(resolvers, new ResolverComparator());
      return resolvers;
    }

    /*
     * Set the list of path resolvers to locate the Google Cloud SDK. Intended for tests to
     * precisely control where the SDK may be found.
     */
    @VisibleForTesting
    public Builder resolvers(List<CloudSdkResolver> resolvers) {
      this.resolvers = resolvers;
      return this;
    }

    @VisibleForTesting
    List<ProcessOutputLineListener> getStdOutLineListeners() {
      return stdOutLineListeners;
    }

    @VisibleForTesting
    List<ProcessOutputLineListener> getStdErrLineListeners() {
      return stdErrLineListeners;
    }

    @VisibleForTesting
    List<ProcessExitListener> getExitListeners() {
      return exitListeners;
    }
  }

  /**
   * Compare two {@link CloudSdkResolver} instances by their rank.
   */
  private static class ResolverComparator implements Comparator<CloudSdkResolver> {
    @Override
    public int compare(CloudSdkResolver o1, CloudSdkResolver o2) {
      return o1.getRank() - o2.getRank();
    }

  }
}
