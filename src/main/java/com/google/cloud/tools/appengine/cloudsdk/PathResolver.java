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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Resolve paths with Google Cloud SDK and Python defaults. */
public class PathResolver implements CloudSdkResolver {

  private static final Logger logger = Logger.getLogger(PathResolver.class.getName());
  private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");

  /**
   * Attempts to find the path to Google Cloud SDK.
   *
   * @return path to Google Cloud SDK or null
   */
  @Override
  @Nullable
  public Path getCloudSdkPath() {
    // search system environment PATH
    List<String> possiblePaths = getLocationsFromPath(System.getenv("PATH"));

    // try environment variable GOOGLE_CLOUD_SDK_HOME
    possiblePaths.add(System.getenv("GOOGLE_CLOUD_SDK_HOME"));

    // search program files
    if (IS_WINDOWS) {
      possiblePaths.add(getLocalAppDataLocation());
      possiblePaths.add(getProgramFilesLocation());
    } else {
      // home directory
      possiblePaths.add(System.getProperty("user.home") + "/google-cloud-sdk");
      // usr directory
      possiblePaths.add("/usr/lib/google-cloud-sdk");
      // try devshell VM
      possiblePaths.add("/google/google-cloud-sdk");
      // try bitnami Jenkins VM:
      possiblePaths.add("/usr/local/share/google/google-cloud-sdk");
    }

    Path finalPath = searchPaths(possiblePaths);
    logger.log(Level.FINE, "Resolved SDK path : " + finalPath);
    return finalPath;
  }

  /** The default location for a single-user install of Cloud SDK on Windows. */
  @Nullable
  private static String getLocalAppDataLocation() {
    String localAppData = System.getenv("LOCALAPPDATA");
    if (localAppData != null) {
      return localAppData + "\\Google\\Cloud SDK\\google-cloud-sdk";
    } else {
      return null;
    }
  }

  @VisibleForTesting
  static List<String> getLocationsFromPath(String pathEnv) {
    List<String> possiblePaths = new ArrayList<>();

    if (pathEnv != null) {
      for (String path : Splitter.on(File.pathSeparator).split(pathEnv)) {
        // Windows sometimes quotes paths so we need to strip these.
        // However quotes are legal in Unix paths.
        if (IS_WINDOWS) {
          path = unquote(path);
        }
        // strip out trailing path separator
        if (path.endsWith(File.separator)) {
          path = path.substring(0, path.length() - 1);
        }
        if (path.endsWith("google-cloud-sdk" + File.separator + "bin")) {
          possiblePaths.add(path.substring(0, path.length() - 4));
        }

        try {
          Path possibleLink = Paths.get(path, "gcloud");
          if (Files.isSymbolicLink(possibleLink)) {
            getLocationsFromLink(possiblePaths, possibleLink);
          }
        } catch (InvalidPathException ex) {
          // not a possible path
        }
      }
    }
    return possiblePaths;
  }

  @VisibleForTesting
  static String unquote(String path) {
    if (path.startsWith("\"")) {
      path = path.substring(1, path.length() - 1);
    }
    if (path.endsWith("\"")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  // resolve symlinks to a path that could be the bin directory of the cloud sdk
  @VisibleForTesting
  static void getLocationsFromLink(List<String> possiblePaths, Path link) {
    try {
      Path resolvedLink = link.toRealPath();
      Path possibleBinDir = resolvedLink.getParent();
      // check if the parent is "bin", we actually depend on that for other resolution
      if (possibleBinDir != null && possibleBinDir.getFileName().toString().equals("bin")) {
        Path possibleCloudSdkHome = possibleBinDir.getParent();
        if (possibleCloudSdkHome != null && Files.exists(possibleCloudSdkHome)) {
          possiblePaths.add(possibleCloudSdkHome.toString());
        }
      }
    } catch (IOException ioe) {
      // intentionally ignore exception
      logger.log(Level.FINE, "Non-critical exception when searching for cloud-sdk", ioe);
    }
  }

  @Nullable
  private static String getProgramFilesLocation() {
    String programFiles = System.getenv("ProgramFiles");
    if (programFiles == null) {
      programFiles = System.getenv("ProgramFiles(x86)");
    }
    if (programFiles != null) {
      return programFiles + "\\Google\\Cloud SDK\\google-cloud-sdk";
    } else {
      return null;
    }
  }

  @Nullable
  private static Path searchPaths(List<String> possiblePaths) {
    for (String pathString : possiblePaths) {
      if (pathString != null) {
        try {
          Path path = Paths.get(pathString);
          if (Files.exists(path)) {
            return path;
          }
        } catch (InvalidPathException ex) {
          // ignore
        }
      }
    }
    return null;
  }

  @Override
  public int getRank() {
    // Should be near-last but allow option for last-ditch resolvers that may choose
    // to prompt the user for a location
    return Integer.MAX_VALUE / 2;
  }
}
