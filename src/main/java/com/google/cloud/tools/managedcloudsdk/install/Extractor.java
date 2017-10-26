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

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Extractor for extracting files from a single archive. Use {@link ExtractorProvider} to provide
 * extractor implementation.
 */
final class Extractor<T extends ExtractorProvider> {
  private final Path archive;
  private final Path destination;
  private final ExtractorProvider extractorProvider;
  private final MessageListener messageListener;

  /** Use {@link ExtractorFactory} to instantiate. */
  Extractor(Path archive, Path destination, T extractorProvider, MessageListener messageListener) {
    this.archive = archive;
    this.destination = destination;
    this.extractorProvider = extractorProvider;
    this.messageListener = messageListener;
  }

  /** Extract and return a {@link Path} to the extraction root. */
  public Path extract() throws IOException, InterruptedException {
    messageListener.message("Extracting archive: " + archive);

    try {
      extractorProvider.extract(archive, destination, messageListener);
    } catch (IOException ex) {
      try {
        messageListener.message("Extraction failed, cleaning up " + destination);
        cleanUp(destination);
      } catch (Exception exx) {
        messageListener.message("Failed to cleanup directory");
      }
      // intentional rethrow after cleanup
      throw ex;
    }

    // we do not allow interrupt mid extraction, so catch it here, we still end up
    // with a valid directory though, so don't clean it up.
    if (Thread.currentThread().isInterrupted()) {
      messageListener.message("Process was interrupted");
      throw new InterruptedException("Process was interrupted");
    }

    return destination;
  }

  @VisibleForTesting
  ExtractorProvider getExtractorProvider() {
    return extractorProvider;
  }

  // TODO: After move to Java8, use guava 21.0 recursive delete.
  private void cleanUp(final Path target) throws IOException {
    Preconditions.checkArgument(Files.isDirectory(target));
    Files.walkFileTree(
        target,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
            if (ex == null) {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            } else {
              // directory iteration failed
              throw ex;
            }
          }
        });
  }
}
