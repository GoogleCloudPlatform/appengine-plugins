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

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class StandardDownloaderTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testValidateAndCreateFiles_targetDirectoryIsFile()
      throws IOException, InterruptedException {
    Path fileWithBadParent = tmp.newFile().toPath().resolve("xyz");

    StandardDownloader downloader = new StandardDownloader(null, fileWithBadParent, null, null);
    try {
      downloader.call();
      Assert.fail("NotDirectoryException expected but not thrown");
    } catch (NotDirectoryException nde) {
      // pass
    }
  }

  @Test
  public void testValidateAndCreateFiles_fileAlreadyExists()
      throws IOException, InterruptedException {
    Path fileThatExists = tmp.newFile().toPath();

    StandardDownloader downloader = new StandardDownloader(null, fileThatExists, null, null);
    try {
      downloader.call();
      Assert.fail("FileAlreadyExists expected but not thrown");
    } catch (FileAlreadyExistsException faee) {
      // pass
    }
  }

  @Test
  public void testDownloadURL_createsNewDirectory() throws IOException, InterruptedException {
    Path destination = tmp.getRoot().toPath().resolve("dir-to-create").resolve("destination-file");
    Path testSourceFile = createTestRemoteResource(1);
    URL fakeRemoteResource = testSourceFile.toUri().toURL();

    StandardDownloader downloader =
        new StandardDownloader(fakeRemoteResource, destination, null, null);

    Path downloaderDestination = downloader.call();
    Assert.assertEquals(destination, downloaderDestination);
  }

  @Test
  public void testDownloadURL_worksWithNullProgressListener()
      throws IOException, InterruptedException {

    Path destination = tmp.getRoot().toPath().resolve("destination-file");
    Path testSourceFile = createTestRemoteResource(1);
    URL fakeRemoteResource = testSourceFile.toUri().toURL();

    StandardDownloader downloader =
        new StandardDownloader(fakeRemoteResource, destination, null, null);

    Path downloaderDestination = downloader.call();
    Assert.assertEquals(destination, downloaderDestination);
  }

  @Test
  public void testDownloadURL() throws IOException, InterruptedException {
    Path destination = tmp.getRoot().toPath().resolve("destination-file");
    Path testSourceFile = createTestRemoteResource(StandardDownloader.BUFFER_SIZE * 10 + 1);
    URL fakeRemoteResource = testSourceFile.toUri().toURL();
    DownloadProgressListener mockListener = Mockito.mock(DownloadProgressListener.class);

    StandardDownloader downloader =
        new StandardDownloader(fakeRemoteResource, destination, null, mockListener);

    Path downloaderDestination = downloader.call();
    Assert.assertEquals(destination, downloaderDestination);

    Assert.assertArrayEquals(Files.readAllBytes(destination), Files.readAllBytes(testSourceFile));

    ArgumentCaptor<Long> lastChunkCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> completedCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> totalCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(mockListener, Mockito.atLeastOnce())
        .updateProgress(
            lastChunkCaptor.capture(), completedCaptor.capture(), totalCaptor.capture());

    // check we always present the same and correct file size
    for (Long total : totalCaptor.getAllValues()) {
      Assert.assertEquals(testSourceFile.toFile().length(), total.longValue());
    }

    // check out chunks add up to total
    long sum = 0;
    for (Long chunk : lastChunkCaptor.getAllValues()) {
      sum += chunk;
    }
    Assert.assertEquals(testSourceFile.toFile().length(), sum);

    // check completed matches chunk data
    long last = 0;
    for (int i = 0; i < completedCaptor.getAllValues().size(); i++) {
      Assert.assertEquals(
          last + lastChunkCaptor.getAllValues().get(i),
          completedCaptor.getAllValues().get(i).longValue());
      last = completedCaptor.getAllValues().get(i);
    }
  }

  private Path createTestRemoteResource(long sizeInBytes) throws IOException {

    Path testFile = tmp.newFile().toPath();
    try (BufferedWriter writer = Files.newBufferedWriter(testFile, Charset.defaultCharset())) {
      for (long i = 0; i < sizeInBytes; i++) {
        writer.write('a');
      }
    }
    return testFile;
  }

  @Test
  public void testDownloadURL_userAgentSet() throws IOException, InterruptedException {
    Path destination = tmp.getRoot().toPath().resolve("destination-file");

    final URLConnection mockConnection = Mockito.mock(URLConnection.class);
    URLStreamHandler testHandler =
        new URLStreamHandler() {
          @Override
          protected URLConnection openConnection(URL url) throws IOException {
            return mockConnection;
          }
        };

    // create a URL with a custom streamHandler so we can get our mock connection
    URL testURL = new URL("", "", 80, "", testHandler);
    StandardDownloader downloader =
        new StandardDownloader(testURL, destination, "test-user-agent", null);

    try {
      downloader.call();
    } catch (Exception e) {
      // ignore, we're only looking for user agent being set
    }
    Mockito.verify(mockConnection, Mockito.times(1))
        .setRequestProperty("User-Agent", "test-user-agent");
  }
}
