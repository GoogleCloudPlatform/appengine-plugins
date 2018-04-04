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
import com.google.cloud.tools.appengine.cloudsdk.internal.process.WaitingProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessStartListener;
import com.google.cloud.tools.appengine.cloudsdk.process.StringBuilderProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkComponent;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

/** Cloud SDK CLI wrapper. */
public class CloudSdk {

  public static final CloudSdkVersion MINIMUM_VERSION = new CloudSdkVersion("171.0.0");

  private static final Logger logger = Logger.getLogger(CloudSdk.class.getName());
  private static final Joiner WHITESPACE_JOINER = Joiner.on(" ");

  private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
  private static final String GCLOUD = "bin/gcloud";
  private static final String DEV_APPSERVER_PY = "bin/dev_appserver.py";
  private static final String JAVA_APPENGINE_SDK_PATH =
      "platform/google_appengine/google/appengine/tools/java/lib";
  private static final String JAVA_TOOLS_JAR = "appengine-tools-api.jar";
  private static final String WINDOWS_BUNDLED_PYTHON = "platform/bundledpython/python.exe";
  private static final String VERSION_FILE_NAME = "VERSION";

  private final Map<String, Path> jarLocations = new HashMap<>();
  private final Path sdkPath;
  private final Path javaHomePath;
  private final ProcessRunner processRunner;
  private final String appCommandMetricsEnvironment;
  private final String appCommandMetricsEnvironmentVersion;
  private final File appCommandCredentialFile;
  private final String appCommandOutputFormat;
  private final String appCommandShowStructuredLogs;
  private final WaitingProcessOutputLineListener runDevAppServerWaitListener;

  private CloudSdk(
      Path sdkPath,
      @Nullable Path javaHomePath,
      @Nullable String appCommandMetricsEnvironment,
      @Nullable String appCommandMetricsEnvironmentVersion,
      @Nullable File appCommandCredentialFile,
      @Nullable String appCommandOutputFormat,
      @Nullable String appCommandShowStructuredLogs,
      ProcessRunner processRunner,
      WaitingProcessOutputLineListener runDevAppServerWaitListener) {
    this.sdkPath = sdkPath;
    this.javaHomePath = javaHomePath;
    this.appCommandMetricsEnvironment = appCommandMetricsEnvironment;
    this.appCommandMetricsEnvironmentVersion = appCommandMetricsEnvironmentVersion;
    this.appCommandCredentialFile = appCommandCredentialFile;
    this.appCommandOutputFormat = appCommandOutputFormat;
    this.appCommandShowStructuredLogs = appCommandShowStructuredLogs;
    this.processRunner = processRunner;
    this.runDevAppServerWaitListener = runDevAppServerWaitListener;

    // Populate jar locations.
    // TODO(joaomartins): Consider case where SDK doesn't contain these jars. Only App Engine
    // SDK does.
    jarLocations.put(
        "servlet-api.jar", getJavaAppEngineSdkPath().resolve("shared/servlet-api.jar"));
    jarLocations.put("jsp-api.jar", getJavaAppEngineSdkPath().resolve("shared/jsp-api.jar"));
    jarLocations.put(
        JAVA_TOOLS_JAR, sdkPath.resolve(JAVA_APPENGINE_SDK_PATH).resolve(JAVA_TOOLS_JAR));
  }

  /**
   * Uses the process runner to execute the gcloud app command with the provided arguments.
   *
   * @param args the arguments to pass to gcloud command
   * @throws ProcessRunnerException when there is an issue running the gcloud process
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  public void runAppCommand(List<String> args)
      throws ProcessRunnerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, InvalidJavaSdkException {
    runGcloudCommand(args, null, "app");
  }

  /**
   * Uses the process runner to execute the gcloud auth command with the provided arguments.
   *
   * @param args the arguments to pass to gcloud command
   * @throws ProcessRunnerException when there is an issue running the gcloud process
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  public void runAuthCommand(List<String> args)
      throws ProcessRunnerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, InvalidJavaSdkException {
    runGcloudCommand(args, null, "auth");
  }

  /**
   * Uses the process runner to execute the gcloud app command with the provided arguments.
   *
   * @param args the arguments to pass to gcloud command
   * @param workingDirectory the working directory in which to run the command
   * @throws ProcessRunnerException when there is an issue running the gcloud process
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  public void runAppCommandInWorkingDirectory(List<String> args, File workingDirectory)
      throws ProcessRunnerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, InvalidJavaSdkException {
    runGcloudCommand(args, workingDirectory, "app");
  }

  /**
   * Runs a source command. That is <code>gcloud beta debug source ...</code>
   *
   * @param args the command arguments, including the main command and flags. For example,
   *     gen-repo-info-file --output_directory [OUTPUT_DIRECTORY] etc.
   * @throws ProcessRunnerException when there is an issue running the gcloud process
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  public void runSourceCommand(List<String> args)
      throws ProcessRunnerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, InvalidJavaSdkException {
    runDebugCommand(args, "source");
  }

  private void runDebugCommand(List<String> args, String group)
      throws ProcessRunnerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, InvalidJavaSdkException {
    runGcloudCommand(args, null, "beta", "debug", group);
  }

  private void runGcloudCommand(List<String> args, File workingDirectory, String... topLevelCommand)
      throws ProcessRunnerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, InvalidJavaSdkException {
    validateCloudSdk();

    List<String> command = new ArrayList<>();
    command.add(getGCloudPath().toString());

    for (String commandToken : topLevelCommand) {
      command.add(commandToken);
    }

    command.addAll(args);
    command.addAll(GcloudArgs.get("format", appCommandOutputFormat));

    if (appCommandCredentialFile != null) {
      command.addAll(GcloudArgs.get("credential-file-override", appCommandCredentialFile));
    }

    logCommand(command);
    processRunner.setEnvironment(getGcloudCommandEnvironment());
    processRunner.setWorkingDirectory(workingDirectory);
    processRunner.run(command.toArray(new String[command.size()]));
  }

  @VisibleForTesting
  Map<String, String> getGcloudCommandEnvironment() {
    Map<String, String> environment = Maps.newHashMap();
    if (appCommandCredentialFile != null) {
      environment.put("CLOUDSDK_APP_USE_GSUTIL", "0");
    }
    if (appCommandMetricsEnvironment != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT", appCommandMetricsEnvironment);
    }
    if (appCommandMetricsEnvironmentVersion != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT_VERSION", appCommandMetricsEnvironmentVersion);
    }
    if (appCommandShowStructuredLogs != null) {
      environment.put("CLOUDSDK_CORE_SHOW_STRUCTURED_LOGS", appCommandShowStructuredLogs);
    }
    // This is to ensure IDE credentials get correctly passed to the gcloud commands, in Windows.
    // It's a temporary workaround until a fix is released.
    // https://github.com/GoogleCloudPlatform/google-cloud-intellij/issues/985
    if (IS_WINDOWS) {
      environment.put("CLOUDSDK_APP_NUM_FILE_UPLOAD_PROCESSES", "1");
    }

    environment.put("CLOUDSDK_CORE_DISABLE_PROMPTS", "1");

    return environment;
  }

  // Runs a gcloud command synchronously, with a new ProcessRunner. This method is intended to be
  // used for the execution of short-running gcloud commands, especially when we need to do some
  // additional processing of the gcloud command's output before returning. In all other cases, this
  // class's main configured ProcessRunner should be used.
  private String runSynchronousGcloudCommand(List<String> args)
      throws ProcessRunnerException, CloudSdkNotFoundException {
    validateCloudSdkLocation();

    StringBuilderProcessOutputLineListener stdOutListener =
        new StringBuilderProcessOutputLineListener();
    ExitCodeRecorderProcessExitListener exitListener = new ExitCodeRecorderProcessExitListener();

    // instantiate a separate synchronous process runner
    ProcessRunner runner =
        new DefaultProcessRunner(
            false, /* async */
            ImmutableList.<ProcessExitListener>of(exitListener), /* exitListeners */
            ImmutableList.<ProcessStartListener>of(), /* startListeners */
            ImmutableList.<ProcessOutputLineListener>of(stdOutListener), /* stdOutLineListeners */
            ImmutableList.<ProcessOutputLineListener>of()); /* stdErrLineListeners */

    // build and run the command
    List<String> command =
        new ImmutableList.Builder<String>().add(getGCloudPath().toString()).addAll(args).build();

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
   * @throws InvalidPathException when Python can't be located
   * @throws ProcessRunnerException when process runner encounters an error
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   * @throws AppEngineException when dev_appserver.py cannot be found
   */
  void runDevAppServerCommand(List<String> args)
      throws ProcessRunnerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, InvalidJavaSdkException {
    validateCloudSdk();

    List<String> command = new ArrayList<>();

    if (IS_WINDOWS) {
      command.add(getWindowsPythonPath().toString());
    }

    command.add(getDevAppServerPath().toString());
    command.addAll(args);

    logCommand(command);

    Map<String, String> environment = Maps.newHashMap();
    environment.put("JAVA_HOME", javaHomePath.toAbsolutePath().toString());
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
   * Uses the process runner to execute the classic Java SDK devappsever command.
   *
   * @param args the arguments to pass to devappserver
   * @param environment the environment to set on the devappserver process
   * @throws ProcessRunnerException when process runner encounters an error
   * @throws AppEngineJavaComponentsNotInstalledException Cloud SDK is installed but App Engine Java
   *     components are not
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   * @throws AppEngineException when dev appserver cannot be found
   */
  void runDevAppServer1Command(
      List<String> jvmArgs,
      List<String> args,
      Map<String, String> environment,
      File workingDirectory)
      throws ProcessRunnerException, AppEngineJavaComponentsNotInstalledException,
          InvalidJavaSdkException {
    validateAppEngineJavaComponents();
    validateJdk();

    List<String> command = new ArrayList<>();

    command.add(getJavaExecutablePath().toString());

    command.addAll(jvmArgs);
    command.add("-Dappengine.sdk.root=" + getJavaAppEngineSdkPath().getParent().toString());
    command.add("-cp");
    command.add(jarLocations.get(JAVA_TOOLS_JAR).toString());
    command.add("com.google.appengine.tools.development.DevAppServerMain");

    command.addAll(args);

    logCommand(command);

    Map<String, String> devServerEnvironment = Maps.newHashMap(environment);
    devServerEnvironment.put("JAVA_HOME", javaHomePath.toAbsolutePath().toString());
    processRunner.setEnvironment(devServerEnvironment);
    processRunner.setWorkingDirectory(workingDirectory);
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
   *     not installed in the Cloud SDK
   * @throws InvalidJavaSdkException java not found
   */
  public void runAppCfgCommand(List<String> args)
      throws ProcessRunnerException, AppEngineJavaComponentsNotInstalledException,
          InvalidJavaSdkException {
    validateAppEngineJavaComponents();
    validateJdk();

    // AppEngineSdk requires this system property to be set.
    System.setProperty("appengine.sdk.root", getJavaAppEngineSdkPath().toString());

    List<String> command = new ArrayList<>();
    command.add(getJavaExecutablePath().toString());
    command.add("-cp");
    command.add(jarLocations.get(JAVA_TOOLS_JAR).toString());
    command.add("com.google.appengine.tools.admin.AppCfg");
    command.addAll(args);

    logCommand(command);

    processRunner.run(command.toArray(new String[command.size()]));
  }

  /**
   * Returns the version of the Cloud SDK installation. Version is determined by reading the VERSION
   * file located in the Cloud SDK directory.
   */
  public CloudSdkVersion getVersion() throws CloudSdkVersionFileException {
    Path versionFile = getSdkPath().resolve(VERSION_FILE_NAME);

    if (!Files.isRegularFile(versionFile)) {
      throw new CloudSdkVersionFileNotFoundException(
          "Cloud SDK version file not found at " + versionFile.toString());
    }

    String contents = "";
    try {
      List<String> lines = Files.readAllLines(versionFile, StandardCharsets.UTF_8);
      if (lines.size() > 0) {
        // expect only a single line
        contents = lines.get(0);
      }
      return new CloudSdkVersion(contents);
    } catch (IOException ex) {
      throw new CloudSdkVersionFileException(ex);
    } catch (IllegalArgumentException ex) {
      throw new CloudSdkVersionFileParseException(
          "Pattern found in the Cloud SDK version file could not be parsed: " + contents, ex);
    }
  }

  /**
   * Returns the list of Cloud SDK Components and their settings, reported by the current gcloud
   * installation. Unlike other methods in this class that call gcloud, this method always uses a
   * synchronous ProcessRunner and will block until the gcloud process returns.
   *
   * @throws ProcessRunnerException when process runner encounters an error
   * @throws JsonSyntaxException when the cloud SDK output cannot be parsed
   * @throws CloudSdkNotFoundException when the Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when the installed Cloud SDK is too old
   */
  public List<CloudSdkComponent> getComponents()
      throws ProcessRunnerException, JsonSyntaxException, CloudSdkNotFoundException,
          CloudSdkOutOfDateException, CloudSdkVersionFileException, InvalidJavaSdkException {
    validateCloudSdk();

    // gcloud components list --show-versions --format=json
    List<String> command =
        new ImmutableList.Builder<String>()
            .add("components", "list")
            .addAll(GcloudArgs.get("show-versions", true))
            .addAll(GcloudArgs.get("format", "json"))
            .build();

    String componentsJson = runSynchronousGcloudCommand(command);
    return CloudSdkComponent.fromJsonList(componentsJson);
  }

  private static void logCommand(List<String> command) {
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

  @VisibleForTesting
  Path getJavaExecutablePath() {
    return javaHomePath.toAbsolutePath().resolve(IS_WINDOWS ? "bin/java.exe" : "bin/java");
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
    return jarLocations.get(jarName);
  }

  /**
   * Checks whether the Cloud SDK path and version are valid.
   *
   * @throws CloudSdkNotFoundException when Cloud SDK is not installed where expected
   * @throws CloudSdkOutOfDateException when Cloud SDK is out of date
   * @throws CloudSdkVersionFileException VERSION file could not be read
   */
  public void validateCloudSdk()
      throws CloudSdkNotFoundException, CloudSdkOutOfDateException, CloudSdkVersionFileException,
          InvalidJavaSdkException {
    validateCloudSdkLocation();
    validateCloudSdkVersion();
  }

  private void validateCloudSdkVersion()
      throws CloudSdkOutOfDateException, CloudSdkVersionFileException {
    try {
      CloudSdkVersion version = getVersion();
      if (version.compareTo(MINIMUM_VERSION) < 0) {
        throw new CloudSdkOutOfDateException(version, MINIMUM_VERSION);
      }
    } catch (CloudSdkVersionFileNotFoundException ex) {
      // this is likely a version of the Cloud SDK prior to when VERSION files were introduced
      throw new CloudSdkOutOfDateException(MINIMUM_VERSION);
    }
  }

  private void validateCloudSdkLocation() throws CloudSdkNotFoundException {
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
              + getDevAppServerPath()
              + "' is not a file.");
    }
  }

  private void validateJdk() throws InvalidJavaSdkException {
    if (!Files.exists(getJavaExecutablePath())) {
      throw new InvalidJavaSdkException(
          "Invalid Java SDK. " + getJavaExecutablePath().toString() + " does not exist.");
    }
  }

  /**
   * Checks whether the App Engine Java components are installed in the expected location in the
   * Cloud SDK.
   *
   * @throws AppEngineJavaComponentsNotInstalledException when the App Engine Java components are
   *     not installed in the Cloud SDK
   */
  public void validateAppEngineJavaComponents()
      throws AppEngineJavaComponentsNotInstalledException {
    if (!Files.isDirectory(getJavaAppEngineSdkPath())) {
      throw new AppEngineJavaComponentsNotInstalledException(
          "Validation Error: Java App Engine components not installed."
              + " Fix by running 'gcloud components install app-engine-java' on command-line.");
    }
    if (!Files.isRegularFile(jarLocations.get(JAVA_TOOLS_JAR))) {
      throw new AppEngineJavaComponentsNotInstalledException(
          "Validation Error: Java Tools jar location '"
              + jarLocations.get(JAVA_TOOLS_JAR)
              + "' is not a file.");
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
    private File appCommandCredentialFile;
    private String appCommandOutputFormat;
    private String appCommandShowStructuredLogs;
    private boolean async = false;
    private List<ProcessOutputLineListener> stdOutLineListeners = new ArrayList<>();
    private List<ProcessOutputLineListener> stdErrLineListeners = new ArrayList<>();
    private List<ProcessExitListener> exitListeners = new ArrayList<>();
    private List<ProcessStartListener> startListeners = new ArrayList<>();
    private List<CloudSdkResolver> resolvers;
    private int runDevAppServerWaitSeconds;
    private boolean inheritProcessOutput;
    private Path javaHomePath = Paths.get(System.getProperty("java.home"));

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

    /** The metrics environment. */
    public Builder appCommandMetricsEnvironment(String appCommandMetricsEnvironment) {
      this.appCommandMetricsEnvironment = appCommandMetricsEnvironment;
      return this;
    }

    /** The metrics environment version. */
    public Builder appCommandMetricsEnvironmentVersion(String appCommandMetricsEnvironmentVersion) {
      this.appCommandMetricsEnvironmentVersion = appCommandMetricsEnvironmentVersion;
      return this;
    }

    /** Sets the path the credential override file. */
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
     * Sets structured json logs for the stderr output. Supported values include 'never' (default),
     * 'always', 'terminal', etc.
     */
    public Builder appCommandShowStructuredLogs(String appCommandShowStructuredLogs) {
      this.appCommandShowStructuredLogs = appCommandShowStructuredLogs;
      return this;
    }

    /** Whether to run commands asynchronously. */
    public Builder async(boolean async) {
      this.async = async;
      return this;
    }

    /**
     * Adds a client consumer of process standard output. If none, output will be inherited by
     * parent process.
     */
    public Builder addStdOutLineListener(ProcessOutputLineListener stdOutLineListener) {
      // Verify there aren't listeners if subprocess inherits output.
      // If output is inherited, then listeners won't receive anything.
      if (inheritProcessOutput) {
        throw new IllegalStateException(
            "You cannot specify subprocess output inheritance and output listeners.");
      }
      this.stdOutLineListeners.add(stdOutLineListener);
      return this;
    }

    /**
     * Adds a client consumer of process error output. If none, output will be inherited by parent
     * process.
     */
    public Builder addStdErrLineListener(ProcessOutputLineListener stdErrLineListener) {
      // Verify there aren't listeners if subprocess inherits output.
      // If output is inherited, then listeners won't receive anything.
      if (inheritProcessOutput) {
        throw new IllegalStateException(
            "You cannot specify subprocess output inheritance and output listeners.");
      }
      this.stdErrLineListeners.add(stdErrLineListener);
      return this;
    }

    /** The client listener of the process exit with code. */
    public Builder exitListener(ProcessExitListener exitListener) {
      this.exitListeners.clear();
      this.exitListeners.add(exitListener);
      return this;
    }

    /** The client listener of the process start. Allows access to the underlying process. */
    public Builder startListener(ProcessStartListener startListener) {
      this.startListeners.clear();
      this.startListeners.add(startListener);
      return this;
    }

    /**
     * When run asynchronously, configure the Dev App Server command to wait for successful start of
     * the server. Setting this will force process output not to be inherited by the caller.
     *
     * @param runDevAppServerWaitSeconds Number of seconds to wait > 0
     */
    public Builder runDevAppServerWait(int runDevAppServerWaitSeconds) {
      this.runDevAppServerWaitSeconds = runDevAppServerWaitSeconds;
      return this;
    }

    /**
     * Causes the generated gcloud or devappserver subprocess to inherit the calling process's
     * stdout and stderr. If this is set to {@code true}, no stdout and stderr listeners can be
     * specified.
     *
     * @param inheritProcessOutput if true, stdout and stderr are redirected to the parent process
     */
    public Builder inheritProcessOutput(boolean inheritProcessOutput) {
      // Verify there aren't listeners if subprocess inherits output.
      // If output is inherited, then listeners won't receive anything.
      if (inheritProcessOutput
          && (stdOutLineListeners.size() > 0 || stdErrLineListeners.size() > 0)) {
        throw new IllegalStateException(
            "You cannot specify subprocess output inheritance and output listeners.");
      }
      this.inheritProcessOutput = inheritProcessOutput;
      return this;
    }

    /**
     * Sets the desired Java SDK path, used in devappserver runs and App Engine standard staging.
     */
    public Builder javaHome(Path javaHomePath) {
      this.javaHomePath = javaHomePath;
      return this;
    }

    /**
     * Create a new instance of {@link CloudSdk}. If {@code sdkPath} is not set, this method looks
     * for the SDK in known install locations.
     */
    public CloudSdk build() throws CloudSdkNotFoundException {

      // Default SDK path
      if (sdkPath == null) {
        sdkPath = discoverSdkPath();
      }

      // Construct process runner.
      ProcessRunner processRunner;
      WaitingProcessOutputLineListener runDevAppServerWaitListener = null;
      if (stdOutLineListeners.size() > 0 || stdErrLineListeners.size() > 0) {
        // Configure listeners for async dev app server start with waiting.
        if (async && runDevAppServerWaitSeconds > 0) {
          runDevAppServerWaitListener =
              new WaitingProcessOutputLineListener(
                  ".*(Dev App Server is now running|INFO:oejs\\.Server:main: Started).*",
                  runDevAppServerWaitSeconds);

          stdOutLineListeners.add(runDevAppServerWaitListener);
          stdErrLineListeners.add(runDevAppServerWaitListener);
          exitListeners.add(0, runDevAppServerWaitListener);
        }

        processRunner =
            new DefaultProcessRunner(
                async, exitListeners, startListeners, stdOutLineListeners, stdErrLineListeners);
      } else {
        processRunner =
            new DefaultProcessRunner(async, exitListeners, startListeners, inheritProcessOutput);
      }

      return new CloudSdk(
          sdkPath,
          javaHomePath,
          appCommandMetricsEnvironment,
          appCommandMetricsEnvironmentVersion,
          appCommandCredentialFile,
          appCommandOutputFormat,
          appCommandShowStructuredLogs,
          processRunner,
          runDevAppServerWaitListener);
    }

    /**
     * Attempt to find the Google Cloud SDK in various places.
     *
     * @return the path to the root of the Google Cloud SDK
     * @throws CloudSdkNotFoundException if not found
     */
    @Nonnull
    private Path discoverSdkPath() throws CloudSdkNotFoundException {
      for (CloudSdkResolver resolver : getResolvers()) {
        try {
          Path discoveredSdkPath = resolver.getCloudSdkPath();
          if (discoveredSdkPath != null) {
            return discoveredSdkPath;
          }
        } catch (RuntimeException ex) {
          // prevent interference from exceptions in other resolvers
          logger.log(
              Level.SEVERE,
              resolver.getClass().getName()
                  + ": exception thrown when searching for Google Cloud SDK",
              ex);
        }
      }
      throw new CloudSdkNotFoundException(
          "The Google Cloud SDK could not be found in the customary"
              + " locations and no path was provided.");
    }

    /** Return the configured SDK resolvers. */
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

  /** Compare two {@link CloudSdkResolver} instances by their rank. */
  private static class ResolverComparator implements Comparator<CloudSdkResolver> {
    @Override
    public int compare(CloudSdkResolver o1, CloudSdkResolver o2) {
      return o1.getRank() - o2.getRank();
    }
  }
}
