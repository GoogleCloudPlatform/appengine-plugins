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
import com.google.cloud.tools.managedcloudsdk.textbars.TextDividerBar;
import com.google.cloud.tools.managedcloudsdk.textbars.TextInfoBar;
import com.google.cloud.tools.managedcloudsdk.textbars.TextProgressBar;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DownloaderTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();
  @Mock private MessageListener mockMessageListener;

  @Mock private TextBarFactory mockTextBarFactory;
  @Mock private TextDividerBar mockDividerBar;
  @Mock private TextInfoBar mockInfoBar;
  @Mock private TextProgressBar mockProgressBar;

  @Before
  public void setupMocks() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockTextBarFactory.newDividerBar(mockMessageListener)).thenReturn(mockDividerBar);
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
  public void testDownload_createsNewDirectory() throws IOException, InterruptedException {
    Path destination = tmp.getRoot().toPath().resolve("dir-to-create").resolve("destination-file");
    Path testSourceFile = createTestRemoteResource(1);
    URL fakeRemoteResource = testSourceFile.toUri().toURL();

    Downloader downloader =
        new Downloader(fakeRemoteResource, destination, null, mockMessageListener);

    downloader.download();
    Assert.assertTrue(Files.exists(destination));
  }

  @Test
  public void testDownload_worksWithNullProgressListener()
      throws IOException, InterruptedException {

    Path destination = tmp.getRoot().toPath().resolve("destination-file");
    Path testSourceFile = createTestRemoteResource(1);
    URL fakeRemoteResource = testSourceFile.toUri().toURL();

    Downloader downloader =
        new Downloader(fakeRemoteResource, destination, null, mockMessageListener);

    downloader.download();
    Assert.assertTrue(Files.exists(destination));
  }

  @Test
  public void testDownload() throws IOException, InterruptedException {
    Path destination = tmp.getRoot().toPath().resolve("destination-file");
    long testFileSize = Downloader.BUFFER_SIZE * 10 + 1;
    Path testSourceFile = createTestRemoteResource(testFileSize);
    URL fakeRemoteResource = testSourceFile.toUri().toURL();

    Mockito.when(
            mockTextBarFactory.newInfoBar(
                mockMessageListener,
                "Downloading SDK (" + String.valueOf(testFileSize) + " bytes)"))
        .thenReturn(mockInfoBar);
    Mockito.when(mockTextBarFactory.newProgressBar(mockMessageListener, testFileSize))
        .thenReturn(mockProgressBar);

    Downloader downloader =
        new Downloader(fakeRemoteResource, destination, null, mockMessageListener);

    downloader.download(mockTextBarFactory);
    Assert.assertTrue(Files.exists(destination));
    Assert.assertArrayEquals(Files.readAllBytes(destination), Files.readAllBytes(testSourceFile));

    Mockito.verify(mockMessageListener)
        .message("Downloading " + fakeRemoteResource.toString() + "\n");

    Mockito.verify(mockDividerBar).show();
    Mockito.verify(mockInfoBar).show();
    Mockito.verify(mockProgressBar).start();
    Mockito.verify(mockProgressBar, Mockito.atLeastOnce()).update(Mockito.anyLong());
    Mockito.verify(mockProgressBar).done();
  }

  @Test
  public void testDownload_userAgentSet() throws IOException, InterruptedException {
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
    URL testUrl = new URL("", "", 80, "", testHandler);
    Downloader downloader =
        new Downloader(testUrl, destination, "test-user-agent", mockMessageListener);

    try {
      downloader.download();
    } catch (Exception ex) {
      // ignore, we're only looking for user agent being set
    }
    Mockito.verify(mockConnection).setRequestProperty("User-Agent", "test-user-agent");
  }

  @Test
  public void testDownload_failIfExists() throws IOException, InterruptedException {
    Path destination = tmp.getRoot().toPath().resolve("destination-file");
    Files.createFile(destination);

    Downloader downloader = new Downloader(null, destination, null, mockMessageListener);

    try {
      downloader.download();
      Assert.fail("FileAlreadyExistsException expected but not thrown.");
    } catch (FileAlreadyExistsException ex) {
      Assert.assertEquals(ex.getMessage(), destination.toString());
    }
  }

  @Test
  public void testDownload_interruptTriggersCleanup() throws IOException, InterruptedException {
    final Path destination = tmp.getRoot().toPath().resolve("destination-file");
    long testFileSize = Downloader.BUFFER_SIZE * 10 + 1;
    Path testSourceFile = createTestRemoteResource(testFileSize);
    final URL fakeRemoteResource = testSourceFile.toUri().toURL();

    Mockito.when(
            mockTextBarFactory.newInfoBar(Mockito.eq(mockMessageListener), Mockito.anyString()))
        .thenReturn(mockInfoBar);
    Mockito.when(mockTextBarFactory.newProgressBar(mockMessageListener, testFileSize))
        .thenReturn(mockProgressBar);
    // Start a new thread for this test to avoid mucking with Thread state when
    // junit reuses threads.
    Thread testThreadToInterrupt =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                Downloader downloader =
                    new Downloader(fakeRemoteResource, destination, null, mockMessageListener);
                Thread.currentThread().interrupt();
                try {
                  downloader.download(mockTextBarFactory);
                  Assert.fail("InterruptedException expected but not thrown.");
                } catch (InterruptedException ex) {
                  Assert.assertEquals("Download was interrupted", ex.getMessage());
                } catch (IOException e) {
                  Assert.fail("Test failed due to IOException");
                }
              }
            });
    testThreadToInterrupt.start();
    testThreadToInterrupt.join();

    Assert.assertFalse(Files.exists(destination));
    Mockito.verify(mockMessageListener)
        .message("Downloading " + fakeRemoteResource.toString() + "\n");
    Mockito.verify(mockMessageListener).message("Download was interrupted\n");
    Mockito.verify(mockMessageListener).message("Cleaning up...\n");
    Mockito.verify(mockProgressBar).start();
    Mockito.verify(mockProgressBar, Mockito.never()).done();
  }
}
