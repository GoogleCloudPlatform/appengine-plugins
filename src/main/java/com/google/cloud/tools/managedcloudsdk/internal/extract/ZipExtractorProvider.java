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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

/** {@link ExtractorProvider} implementation for *.zip files. */
public final class ZipExtractorProvider implements ExtractorProvider {

  /** Only instantiated in {@link ExtractorFactory}. */
  @VisibleForTesting
  ZipExtractorProvider() {}

  @Override
  public void extract(Path archive, Path destination, ExtractorMessageListener extractorMessageListener) throws IOException {
    try (ZipArchiveInputStream in = new ZipArchiveInputStream(Files.newInputStream(archive))) {
      ZipArchiveEntry entry;
      while ((entry = in.getNextZipEntry()) != null) {
        final Path entryPath = destination.resolve(entry.getName());
        if (extractorMessageListener != null) {
          extractorMessageListener.message(entryPath.toString());
        }
        if (entry.isDirectory()) {
          if (!Files.exists(entryPath)) {
            Files.createDirectories(entryPath);
          }
        } else {
          try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(entryPath))) {
            IOUtils.copy(in, out);
          }
        }
      }
    }

  }
}
