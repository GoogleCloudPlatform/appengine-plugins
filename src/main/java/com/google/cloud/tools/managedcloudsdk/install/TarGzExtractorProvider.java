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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * {@link ExtractorProvider} implementation for *.tar.gz files.
 *
 * <p>NOTE: this does not handle links or symlinks or any other kind of special types in the tar. It
 * will only create files and directories.
 */
final class TarGzExtractorProvider implements ExtractorProvider {

  private static final Logger logger = Logger.getLogger(TarGzExtractorProvider.class.getName());

  /** Only instantiated in {@link ExtractorFactory}. */
  TarGzExtractorProvider() {}

  @Override
  public void extract(Path archive, Path destination, ProgressListener progressListener)
      throws IOException {

    progressListener.start(
        "Extracting archive: " + archive.getFileName(), ProgressListener.UNKNOWN);

    GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(Files.newInputStream(archive));
    try (TarArchiveInputStream in = new TarArchiveInputStream(gzipIn)) {
      TarArchiveEntry entry;
      while ((entry = in.getNextTarEntry()) != null) {
        final Path entryTarget = destination.resolve(entry.getName());

        progressListener.update(1);
        logger.fine(entryTarget.toString());

        if (entry.isDirectory()) {
          if (!Files.exists(entryTarget)) {
            Files.createDirectories(entryTarget);
          }
        } else if (entry.isFile()) {
          if (!Files.exists(entryTarget.getParent())) {
            Files.createDirectories(entryTarget.getParent());
          }
          try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(entryTarget))) {
            IOUtils.copy(in, out);
            PosixFileAttributeView attributeView =
                Files.getFileAttributeView(entryTarget, PosixFileAttributeView.class);
            if (attributeView != null) {
              attributeView.setPermissions(PosixUtil.getPosixFilePermissions(entry.getMode()));
            }
          }
        } else {
          // we don't know what kind of entry this is (we only process directories and files).
          logger.warning("Skipping entry (unknown type): " + entry.getName());
        }
      }
      progressListener.done();
    }
  }
}
