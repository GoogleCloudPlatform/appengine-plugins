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

import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
  private static final String APPENGINE_SDK_FOR_JAVA_PATH =
      "platform/google_appengine/google/appengine/tools/java/lib";
  private static final String JAVA_TOOLS_JAR = "appengine-tools-api.jar";
  private static final String WINDOWS_BUNDLED_PYTHON = "platform/bundledpython/python.exe";
  private static final String VERSION_FILE_NAME = "VERSION";

  private final Map<String, Path> jarLocations = new HashMap<>();
  private final Path sdkPath;
  private final Path javaHomePath;

  private CloudSdk(Path sdkPath, @Nullable Path javaHomePath) {
    this.sdkPath = sdkPath;
    this.javaHomePath = javaHomePath;

    // Populate jar locations.
    // TODO(joaomartins): Consider case where SDK doesn't contain these jars. Only App Engine
    // SDK does.
    jarLocations.put(
        "servlet-api.jar", getAppEngineSdkForJavaPath().resolve("shared/servlet-api.jar"));
    jarLocations.put("jsp-api.jar", getAppEngineSdkForJavaPath().resolve("shared/jsp-api.jar"));
    jarLocations.put(
        JAVA_TOOLS_JAR, sdkPath.resolve(APPENGINE_SDK_FOR_JAVA_PATH).resolve(JAVA_TOOLS_JAR));
  }

  /**
   * Returns the version of the Cloud SDK installation. Version is determined by reading the VERSION
   * file located in the Cloud SDK directory.
   */
  public CloudSdkVersion getVersion() throws CloudSdkVersionFileException {
    Path versionFile = getPath().resolve(VERSION_FILE_NAME);

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

  public Path getPath() {
    return sdkPath;
  }

  /** Get an OS specific path to gcloud. */
  public Path getGCloudPath() {
    String gcloud = GCLOUD;
    if (IS_WINDOWS) {
      gcloud += ".cmd";
    }
    return getPath().resolve(gcloud);
  }

  Path getDevAppServerPath() {
    return getPath().resolve(DEV_APPSERVER_PY);
  }

  /**
   * Returns the directory containing JAR files bundled with the Cloud SDK.
   *
   * @return the directory containing JAR files bundled with the Cloud SDK
   */
  public Path getAppEngineSdkForJavaPath() {
    return getPath().resolve(APPENGINE_SDK_FOR_JAVA_PATH);
  }

  @VisibleForTesting
  Path getJavaExecutablePath() {
    return javaHomePath.toAbsolutePath().resolve(IS_WINDOWS ? "bin/java.exe" : "bin/java");
  }

  public Path getJavaHomePath() {
    return javaHomePath;
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

    Path pythonPath = getPath().resolve(WINDOWS_BUNDLED_PYTHON);
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
      throws CloudSdkNotFoundException, CloudSdkOutOfDateException, CloudSdkVersionFileException {
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

  void validateCloudSdkLocation() throws CloudSdkNotFoundException {
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

  void validateJdk() throws InvalidJavaSdkException {
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
    if (!Files.isDirectory(getAppEngineSdkForJavaPath())) {
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

  public Path getAppEngineToolsJar() {
    return jarLocations.get(JAVA_TOOLS_JAR);
  }

  public static class Builder {
    private Path sdkPath;
    private List<CloudSdkResolver> resolvers;
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

      return new CloudSdk(sdkPath, javaHomePath);
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
      resolvers.sort(new ResolverComparator());
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
  }

  /** Compare two {@link CloudSdkResolver} instances by their rank. */
  private static class ResolverComparator implements Comparator<CloudSdkResolver> {
    @Override
    public int compare(CloudSdkResolver o1, CloudSdkResolver o2) {
      return o1.getRank() - o2.getRank();
    }
  }
}
