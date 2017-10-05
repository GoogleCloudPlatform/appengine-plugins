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

package com.google.cloud.tools.managedcloudsdk.internal.extract;

import com.google.common.annotations.VisibleForTesting;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** {@link Extractor} implementation with configurable {@link ExtractorProvider} implementation. */
public final class ConfigurableExtractor<T extends ExtractorProvider> implements Extractor {
  private final Path archive;
  private final Path destination;
  private final ExtractorProvider extractorProvider;
  private final ExtractorMessageListener listener;

  /** Use {@link ExtractorFactory} to instantiate. */
  ConfigurableExtractor(
      Path archive, Path destination, T extractorProvider, ExtractorMessageListener listener) {
    this.archive = archive;
    this.destination = destination;
    this.extractorProvider = extractorProvider;
    this.listener = listener;
  }

  @Override
  public Path call() throws IOException {
    if (listener != null) {
      listener.message("Extracting archive: " + archive.toString());
    }

    extractorProvider.extract(archive, destination, listener);

    // this is convention
    Path cloudSdkHome = destination.resolve("google-cloud-sdk");
    if (!Files.isDirectory(cloudSdkHome)) {
      throw new FileNotFoundException(
          "After extraction, Cloud SDK home not found at " + cloudSdkHome);
    }
    return cloudSdkHome;
  }

  @VisibleForTesting
  ExtractorProvider getExtractorProvider() {
    return extractorProvider;
  }
}
