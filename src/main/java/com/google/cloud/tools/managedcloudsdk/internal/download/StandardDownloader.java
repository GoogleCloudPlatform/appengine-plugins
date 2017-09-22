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

package com.google.cloud.tools.managedcloudsdk.internal.download;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

/** Standard implementation of {@link Downloader}. */
public final class StandardDownloader implements Downloader {

  static final int BUFFER_SIZE = 8 * 1024;
  private final URL address;
  private final Path destinationFile;
  private final String userAgentString;
  private final DownloadProgressListener downloadProgressListener;

  /** Use {@link DownloaderFactory} to instantiate. */
  StandardDownloader(
      URL source,
      Path destinationFile,
      String userAgentString,
      DownloadProgressListener downloadProgressListener) {
    this.address = source;
    this.destinationFile = destinationFile;
    this.userAgentString = userAgentString;
    this.downloadProgressListener = downloadProgressListener;
  }

  @Override
  public Path call() throws IOException, InterruptedException {
    Path destinationDir = destinationFile.getParent();
    if (Files.exists(destinationDir) && !Files.isDirectory(destinationDir)) {
      throw new NotDirectoryException(
          "Cannot download to " + destinationDir + " because it is not a directory");
    }
    if (Files.exists(destinationFile)) {
      throw new FileAlreadyExistsException(
          "Cannot write to " + destinationFile + " because it already exists");
    }
    if (!Files.exists(destinationDir)) {
      Files.createDirectories(destinationDir);
    }

    URLConnection conn;
    conn = address.openConnection();
    conn.setRequestProperty("User-Agent", userAgentString);

    try (BufferedOutputStream out =
        new BufferedOutputStream(Files.newOutputStream(destinationFile))) {
      try (InputStream in = conn.getInputStream()) {

        long contentLength = conn.getContentLengthLong();
        // Progress is updated every 1%
        long updateThreshold = contentLength / 100;

        byte[] buffer = new byte[BUFFER_SIZE];
        long lastUpdated = 0;
        long totalBytesRead = 0;

        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Downloader was interrupted.");
          }
          totalBytesRead += bytesRead;
          long bytesSinceLastUpdate = totalBytesRead - lastUpdated;
          if (totalBytesRead == contentLength || bytesSinceLastUpdate > updateThreshold) {
            if (downloadProgressListener != null) {
              downloadProgressListener.updateProgress(
                  bytesSinceLastUpdate, totalBytesRead, contentLength);
            }
            lastUpdated = totalBytesRead;
          }
          out.write(buffer, 0, bytesRead);
        }
      }
    }
    return destinationFile;
  }
}
