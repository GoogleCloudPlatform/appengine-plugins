package com.google.cloud.tools.appengine.cloudsdk;

import java.nio.file.Path;

public interface PathResolver {

  /**
   * Attempts to find the path to Google Cloud SDK.
   *
   * @return Path to Google Cloud SDK or null
   */
  Path getCloudSdkPath();

}