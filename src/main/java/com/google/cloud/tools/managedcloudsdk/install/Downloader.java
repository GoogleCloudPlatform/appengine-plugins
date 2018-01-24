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
import com.google.cloud.tools.managedcloudsdk.textbars.TextBarFactory;
import com.google.cloud.tools.managedcloudsdk.textbars.TextProgressBar;
import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Downloader for downloading a single Cloud SDK archive. */
final class Downloader {

  static final int BUFFER_SIZE = 8 * 1024;
  private final URL address;
  private final Path destinationFile;
  private final String userAgentString;
  private final MessageListener messageListener;

  /** Use {@link DownloaderFactory} to instantiate. */
  Downloader(
      URL source, Path destinationFile, String userAgentString, MessageListener messageListener) {
    this.address = source;
    this.destinationFile = destinationFile;
    this.userAgentString = userAgentString;
    this.messageListener = messageListener;
  }

  /** Download an archive, this will NOT overwrite a previously existing file. */
  public void download() throws IOException, InterruptedException {
    download(new TextBarFactory());
  }

  @VisibleForTesting
  void download(TextBarFactory textBarFactory) throws IOException, InterruptedException {
    if (!Files.exists(destinationFile.getParent())) {
      Files.createDirectories(destinationFile.getParent());
    }

    if (Files.exists(destinationFile)) {
      throw new FileAlreadyExistsException(destinationFile.toString());
    }
    URLConnection connection = address.openConnection();
    connection.setRequestProperty("User-Agent", userAgentString);

    try (InputStream in = connection.getInputStream()) {
      long contentLength = connection.getContentLengthLong();

      messageListener.message("Downloading " + address + "\n");

      try (BufferedOutputStream out =
          new BufferedOutputStream(
              Files.newOutputStream(destinationFile, StandardOpenOption.CREATE_NEW))) {

        textBarFactory.newDividerBar(messageListener).show();
        textBarFactory
            .newInfoBar(
                messageListener, "Downloading SDK (" + String.valueOf(contentLength) + " bytes)")
            .show();
        TextProgressBar progressBar = textBarFactory.newProgressBar(messageListener, contentLength);
        progressBar.start();

        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];

        while ((bytesRead = in.read(buffer)) != -1) {
          if (Thread.currentThread().isInterrupted()) {
            messageListener.message("Download was interrupted\n");
            messageListener.message("Cleaning up...\n");
            cleanUp();
            throw new InterruptedException("Download was interrupted");
          }

          out.write(buffer, 0, bytesRead);
          // update progress
          progressBar.update(bytesRead);
        }
        progressBar.done();
      }
    }
  }

  private void cleanUp() throws IOException {
    Files.deleteIfExists(destinationFile);
  }
}
