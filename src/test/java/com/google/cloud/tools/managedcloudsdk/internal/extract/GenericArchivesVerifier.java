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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

/** Helper for archives in src/test/resources/genericArchives */
public class GenericArchivesVerifier {

  public static void assertArchiveExtraction(Path root) {
    Path file1 = root.resolve("file1.txt");
    Path dir = root.resolve("dir");
    Path file2 = dir.resolve("file2.txt");

    Assert.assertTrue(Files.isRegularFile(file1));
    Assert.assertTrue(Files.isDirectory(dir));
    Assert.assertTrue(Files.isRegularFile(file2));
  }

  public static void assertFilePermissions(Path root) throws IOException {
    Path file1 = root.resolve("file1.txt"); // mode = 640
    PosixFileAttributeView allAttributesFile1 =
        Files.getFileAttributeView(file1, PosixFileAttributeView.class);
    Assert.assertThat(
        allAttributesFile1.readAttributes().permissions(),
        Matchers.containsInAnyOrder(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.GROUP_READ));

    Path file2 = root.resolve("dir").resolve("file2.txt"); // mode = 777
    PosixFileAttributeView allAttributesFile2 =
        Files.getFileAttributeView(file2, PosixFileAttributeView.class);
    Assert.assertThat(
        allAttributesFile2.readAttributes().permissions(),
        Matchers.containsInAnyOrder(PosixFilePermission.values()));
  }

  public static void assertListenerReceivedExtractionMessages(
      ExtractorMessageListener listener, Path testDir, Path archive) {
    assert MockUtil.isMock(listener);

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(listener, Mockito.times(3)).message(messageCaptor.capture());

    Assert.assertThat(
        messageCaptor.getAllValues(),
        Matchers.containsInAnyOrder(
            testDir.resolve("dir").toString(),
            testDir.resolve("dir").resolve("file2.txt").toString(),
            testDir.resolve("file1.txt").toString()));
  }
}
