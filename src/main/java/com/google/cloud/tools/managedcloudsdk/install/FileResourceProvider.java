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

package com.google.cloud.tools.managedcloudsdk.install;

import java.net.URL;
import java.nio.file.Path;

/** Provider for all file information for an installation. */
class FileResourceProvider {

  private final URL archiveSource;
  private final Path archiveDestination;
  private final Path archiveExtractionDestination;
  private final String gcloudExecutableName;

  /** Instantiated by {@link FileResourceProviderFactory}. */
  FileResourceProvider(
      URL archiveSource,
      Path archiveDestination,
      Path archiveExtractionDestination,
      String gcloudExecutableName) {
    this.archiveSource = archiveSource;
    this.archiveDestination = archiveDestination;
    this.archiveExtractionDestination = archiveExtractionDestination;
    this.gcloudExecutableName = gcloudExecutableName;
  }

  public URL getArchiveSource() {
    return archiveSource;
  }

  public Path getArchiveDestination() {
    return archiveDestination;
  }

  public Path getArchiveExtractionDestination() {
    return archiveExtractionDestination;
  }

  public Path getExtractedSdkHome() {
    return getArchiveExtractionDestination().resolve("google-cloud-sdk");
  }

  public Path getExtractedGcloud() {
    return getExtractedSdkHome().resolve("bin").resolve(gcloudExecutableName);
  }
}
