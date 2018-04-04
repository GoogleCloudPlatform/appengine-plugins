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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import org.hamcrest.Matchers;
import org.junit.Assert;

/** Helper for archives in src/test/resources/genericArchives */
public class GenericArchivesVerifier {

  private static final Path ROOT = Paths.get("root");
  private static final Path FILE_1 = ROOT.resolve("file1.txt");
  private static final Path SUB = ROOT.resolve("sub");
  private static final Path FILE_2 = SUB.resolve("file2.txt");

  public static void assertArchiveExtraction(Path testRoot) {
    Assert.assertTrue(Files.isDirectory(testRoot.resolve(ROOT)));
    Assert.assertTrue(Files.isRegularFile(testRoot.resolve(FILE_1)));
    Assert.assertTrue(Files.isDirectory(testRoot.resolve(SUB)));
    Assert.assertTrue(Files.isRegularFile(testRoot.resolve(FILE_2)));
  }

  public static void assertFilePermissions(Path testRoot) throws IOException {
    Path file1 = testRoot.resolve(FILE_1); // mode 664
    PosixFileAttributeView allAttributesFile1 =
        Files.getFileAttributeView(file1, PosixFileAttributeView.class);
    Assert.assertThat(
        allAttributesFile1.readAttributes().permissions(),
        Matchers.containsInAnyOrder(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.GROUP_READ));

    Path file2 = testRoot.resolve(FILE_2); // mode 777
    PosixFileAttributeView allAttributesFile2 =
        Files.getFileAttributeView(file2, PosixFileAttributeView.class);
    Assert.assertThat(
        allAttributesFile2.readAttributes().permissions(),
        Matchers.containsInAnyOrder(PosixFilePermission.values()));
  }
}
