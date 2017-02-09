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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolve paths with CloudSdk and Python defaults.
 */
public class PathResolver implements CloudSdkResolver {

  private static final Logger logger = Logger.getLogger(PathResolver.class.getName());

  /**
   * Attempts to find the path to Google Cloud SDK.
   *
   * @return Path to Google Cloud SDK or null
   */
  @Override
  public Path getCloudSdkPath() {
    List<String> possiblePaths = new ArrayList<>();

    // search system environment PATH
    getLocationsFromPath(possiblePaths);

    // try environment variable GOOGLE_CLOUD_SDK_HOME
    possiblePaths.add(System.getenv("GOOGLE_CLOUD_SDK_HOME"));

    // search program files
    if (System.getProperty("os.name").contains("Windows")) {
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
    logger.log(Level.FINE, "Resolved SDK path : " + finalPath.toString());
    return searchPaths(possiblePaths);
  }

  /** 
   * The default location for a single-user install of Cloud SDK on Windows.
   */
  private static String getLocalAppDataLocation() {
    String localAppData = System.getenv("LOCALAPPDATA");
    if (localAppData != null) {
      return localAppData + "\\Google\\Cloud SDK\\google-cloud-sdk";
    } else {
      return null;
    }
  }

  private static void getLocationsFromPath(List<String> possiblePaths) {
    String pathEnv = System.getenv("PATH");

    if (pathEnv != null) {
      for (String path : pathEnv.split(File.pathSeparator)) {
        // strip out trailing path separator
        if (path.endsWith(File.separator)) {
          path = path.substring(0, path.length() - 1);
        }
        if (path.endsWith("google-cloud-sdk" + File.separator + "bin")) {
          possiblePaths.add(path.substring(0, path.length() - 4));
        }

        Path possibleLink = Paths.get(path, "gcloud");
        if (Files.isSymbolicLink(possibleLink)) {
          getLocationsFromLink(possiblePaths, possibleLink);
        }
      }
    }
  }

  @VisibleForTesting
  // resolve symlinks to a path that could be the bin directory of the cloud sdk
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

  private static Path searchPaths(List<String> possiblePaths) {
    for (String pathString : possiblePaths) {
      if (pathString != null) {
        Path path = Paths.get(pathString);
        if (Files.exists(path)) {
          return path;
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
