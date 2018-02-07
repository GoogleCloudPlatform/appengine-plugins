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

import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Extractor for extracting files from a single archive. Use {@link ExtractorProvider} to provide
 * extractor implementation.
 */
final class Extractor<T extends ExtractorProvider> {

  private final Logger logger = Logger.getLogger(Extractor.class.getName());

  private final Path archive;
  private final Path destination;
  private final ExtractorProvider extractorProvider;
  private final ProgressListener progressListener;

  /** Use {@link ExtractorFactory} to instantiate. */
  Extractor(
      Path archive, Path destination, T extractorProvider, ProgressListener progressListener) {
    this.archive = archive;
    this.destination = destination;
    this.extractorProvider = extractorProvider;
    this.progressListener = progressListener;
  }

  /** Extract an archive. */
  public void extract() throws IOException, InterruptedException {

    try {
      extractorProvider.extract(archive, destination, progressListener);
    } catch (IOException ex) {
      try {
        logger.warning("Extraction failed, cleaning up " + destination);
        cleanUp(destination);
      } catch (IOException exx) {
        logger.warning("Failed to cleanup directory");
      }
      // intentional rethrow after cleanup
      throw ex;
    }

    // we do not allow interrupt mid extraction, so catch it here, we still end up
    // with a valid directory though, so don't clean it up.
    if (Thread.currentThread().isInterrupted()) {
      logger.warning("Process was interrupted");
      throw new InterruptedException("Process was interrupted");
    }
  }

  @VisibleForTesting
  ExtractorProvider getExtractorProvider() {
    return extractorProvider;
  }

  // TODO: After move to Java8, use guava 21.0 recursive delete.
  private void cleanUp(final Path target) throws IOException {
    Files.walkFileTree(target, new FileDeleteVisitor());
  }
}
