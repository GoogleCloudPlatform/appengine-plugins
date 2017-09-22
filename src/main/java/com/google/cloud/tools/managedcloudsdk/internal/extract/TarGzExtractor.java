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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Extractor for *.tar.gz google cloud sdk archives.
 *
 * <p>NOTE: this does not handle links or symlinks or any other kind of special types in the tar. It
 * will only create files and directories.
 */
public class TarGzExtractor implements Extractor {

  private final Path archive;
  private final Path destination;
  private final ExtractorMessageListener listener;

  TarGzExtractor(Path archive, Path destination, ExtractorMessageListener listener) {
    this.archive = archive;
    this.destination = destination;
    this.listener = listener;
  }

  @Override
  public Path call() throws IOException {
    listener.message("Extracting archive: " + archive.toString());

    GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(Files.newInputStream(archive));
    try (TarArchiveInputStream in = new TarArchiveInputStream(gzipIn)) {
      TarArchiveEntry entry;
      while ((entry = in.getNextTarEntry()) != null) {
        final Path entryPath = destination.resolve(entry.getName());
        if (listener != null) {
          listener.message(entryPath.toString());
        }
        if (entry.isDirectory()) {
          if (!Files.exists(entryPath)) {
            Files.createDirectories(entryPath);
          }
        } else if (entry.isFile()) {
          try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(entryPath))) {
            IOUtils.copy(in, out);
            PosixFileAttributeView attributeView =
                Files.getFileAttributeView(entryPath, PosixFileAttributeView.class);
            if (attributeView != null) {
              attributeView.setPermissions(PosixUtil.getPosixFilePermissions(entry.getMode()));
            }
          }
        } else {
          // we don't know what kind of entry this is (we only process directories and files).
        }
      }
    }

    // this is convention
    return destination.resolve("google-cloud-sdk");
  }
}
