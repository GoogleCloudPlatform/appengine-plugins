/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.io;

import static org.junit.Assume.assumeTrue;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test for {@link FileUtil} */
public class FileUtilTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Test
  public void testCopyDirectory_nested() throws IOException {
    Path src = testDir.newFolder("src").toPath();
    Path dest = testDir.newFolder("dest").toPath();

    Path rootFile = Files.createFile(src.resolve("root.file"));
    Path subDir = Files.createDirectory(src.resolve("sub"));
    Path subFile = Files.createFile(subDir.resolve("sub.file"));

    FileUtil.copyDirectory(src, dest);

    Assert.assertTrue(Files.isRegularFile(dest.resolve(src.relativize(rootFile))));
    Assert.assertTrue(Files.isDirectory(dest.resolve(src.relativize(subDir))));
    Assert.assertTrue(Files.isRegularFile(dest.resolve(src.relativize(subFile))));
  }

  @Test
  public void testCopyDirectory_posixPermissions() throws IOException {
    assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    Set<PosixFilePermission> permission = Sets.newHashSet();
    permission.add(PosixFilePermission.OWNER_READ);
    permission.add(PosixFilePermission.GROUP_READ);
    permission.add(PosixFilePermission.OTHERS_READ);
    permission.add(PosixFilePermission.OTHERS_EXECUTE);
    permission.add(PosixFilePermission.OTHERS_WRITE);

    Path src = testDir.newFolder("src").toPath();
    Path dest = testDir.newFolder("dest").toPath();

    Path rootFile = Files.createFile(src.resolve("root1.file"));
    Assert.assertNotEquals(
        "This test is useless - modified permissions are default permissions",
        Files.getPosixFilePermissions(rootFile),
        permission);
    Files.setPosixFilePermissions(rootFile, permission);

    FileUtil.copyDirectory(src, dest);

    Assert.assertEquals(
        permission, Files.getPosixFilePermissions(dest.resolve(src.relativize(rootFile))));
  }

  @Test
  public void testCopyDirectory_aclPermissions() {
    assumeTrue(System.getProperty("os.name").startsWith("Windows"));
    // TODO : write windows tests
  }

  @Test
  public void testCopyDirectory_badArgs() throws IOException {
    Path dir = testDir.newFolder().toPath();
    Path file = testDir.newFile().toPath();

    try {
      FileUtil.copyDirectory(dir, file);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    try {
      FileUtil.copyDirectory(file, dir);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    try {
      FileUtil.copyDirectory(dir, dir);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testCopyDirectory_childPath() throws IOException {
    Path src = testDir.newFolder().toPath();
    Path dest = Files.createDirectory(src.resolve("subdir"));

    try {
      FileUtil.copyDirectory(src, dest);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals("destination is child of source", ex.getMessage());
    }
  }

  @Test
  public void testCopyDirectory_sameFile() throws IOException {
    Path src = testDir.newFolder().toPath();
    Path dest = Paths.get(src.toString(), "..", src.getFileName().toString());

    try {
      FileUtil.copyDirectory(src, dest);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals("Source and destination are the same", ex.getMessage());
    }
  }

  @Test
  public void testWeirdNames() throws IOException {
    Path src = testDir.newFolder("funny").toPath();
    Path dest = testDir.newFolder("funny2").toPath();
    FileUtil.copyDirectory(src, dest);
  }
}
