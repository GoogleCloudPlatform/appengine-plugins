package com.google.cloud.tools.app.impl.cloudsdk.internal.sdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * Resolve paths with CloudSdk and Python defaults
 */
public enum PathResolver {

  INSTANCE;

  public Path getCloudSdkPath() throws FileNotFoundException {
    String sdkDir = System.getenv("GOOGLE_CLOUD_SDK_HOME");
    if (sdkDir == null) {
      boolean isWindows = System.getProperty("os.name").contains("Windows");
      if (isWindows) {
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles == null) {
          programFiles = System.getenv("ProgramFiles(x86)");
        }
        if (programFiles == null) {
          throw new FileNotFoundException(
              "Could not find ProgramFiles, please set the GOOGLE_CLOUD_SDK_HOME environment variable");
        } else {
          sdkDir = programFiles + "\\Google\\Cloud SDK\\google-cloud-sdk";
        }
      } else {
        sdkDir = System.getProperty("user.home") + "/google-cloud-sdk";
        if (!new File(sdkDir).exists()) {
          // try devshell VM:
          sdkDir = "/google/google-cloud-sdk";
          if (!new File(sdkDir).exists()) {
            // try bitnami Jenkins VM:
            sdkDir = "/usr/local/share/google/google-cloud-sdk";
          }
        }
      }
    }
    File file = new File(sdkDir);
    if (file.exists()) {
      return file.toPath();
    } else {
      return null;
    }
  }
}
