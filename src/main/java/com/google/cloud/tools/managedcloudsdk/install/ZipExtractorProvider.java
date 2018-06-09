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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

/**
 * {@link ExtractorProvider} implementation for *.zip files.
 *
 * <p>NOTE: this does not handle links or symlinks or any other kind of special types in the tar. It
 * will only create files and directories.
 */
final class ZipExtractorProvider implements ExtractorProvider {

  private static final Logger logger = Logger.getLogger(ZipExtractorProvider.class.getName());

  /** Only instantiated in {@link ExtractorFactory}. */
  @VisibleForTesting
  ZipExtractorProvider() {}

  @Override
  public void extract(Path archive, Path destination, ProgressListener progressListener)
      throws IOException {

    progressListener.start(
        "Extracting archive: " + archive.getFileName(), ProgressListener.UNKNOWN);

    String canonicalDestination = destination.toFile().getCanonicalPath();

    // Use ZipFile instead of ZipArchiveInputStream so that we can obtain file permissions
    // on unix-like systems via getUnixMode(). ZipArchiveInputStream doesn't have access to
    // all the zip file data and will return "0" for any call to getUnixMode().
    try (ZipFile zipFile = new ZipFile(archive.toFile())) {
      // TextProgressBar progressBar = textBarFactory.newProgressBar(messageListener, count);
      Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
      while (zipEntries.hasMoreElements()) {
        ZipArchiveEntry entry = zipEntries.nextElement();
        Path entryTarget = destination.resolve(entry.getName());

        String canonicalTarget = entryTarget.toFile().getCanonicalPath();
        if (!canonicalTarget.startsWith(canonicalDestination + File.separator)) {
          throw new IOException("Blocked unzipping files outside destination: " + entry.getName());
        }

        progressListener.update(1);
        logger.fine(entryTarget.toString());

        if (entry.isDirectory()) {
          if (!Files.exists(entryTarget)) {
            Files.createDirectories(entryTarget);
          }
        } else {
          if (!Files.exists(entryTarget.getParent())) {
            Files.createDirectories(entryTarget.getParent());
          }
          try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(entryTarget))) {
            try (InputStream in = zipFile.getInputStream(entry)) {
              IOUtils.copy(in, out);
              PosixFileAttributeView attributeView =
                  Files.getFileAttributeView(entryTarget, PosixFileAttributeView.class);
              if (attributeView != null) {
                attributeView.setPermissions(
                    PosixUtil.getPosixFilePermissions(entry.getUnixMode()));
              }
            }
          }
        }
      }
    }
    progressListener.done();
  }
}
