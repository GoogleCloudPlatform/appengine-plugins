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

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class FilePermissionsTest {

  private Path parent;

  @Before
  public void setUp() throws IOException {
    parent = Files.createTempDirectory("foo");
  }

  @Test
  public void testNullDirectory() throws AccessDeniedException, NotDirectoryException {
    try {
      FilePermissions.verifyDirectoryCreatable(null);
      Assert.fail();
    } catch (NullPointerException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testDirectoryCanBeCreated() throws IOException {
    FilePermissions.verifyDirectoryCreatable(Paths.get(parent.toString(), "bar"));
  }

  @Test
  public void testSubDirectoryCanBeCreated() throws IOException {
    FilePermissions.verifyDirectoryCreatable(Paths.get(parent.toString(), "bar", "baz"));
  }

  @Test // Non-Windows only
  public void testSubDirectoryCannotBeCreatedInDevNull() {
    Assume.assumeTrue(!System.getProperty("os.name").startsWith("Windows"));
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get("/dev/null/foo/bar"));
      Assert.fail("Can create directory in /dev/null");
    } catch (IOException ex) {
      Assert.assertNotNull(ex.getMessage());
      Assert.assertTrue(ex.getMessage(), ex.getMessage().contains("/dev/null"));
    }
  }

  @Test
  public void testDirectoryCannotBeCreatedDueToPreexistingFile() throws IOException {
    Path file = Files.createTempFile(parent, "prefix", "suffix");
    try {
      FilePermissions.verifyDirectoryCreatable(file);
      Assert.fail("Can create directory over file");
    } catch (NotDirectoryException ex) {
      Assert.assertNotNull(ex.getMessage());
      Assert.assertTrue(ex.getMessage().contains(file.getFileName().toString()));
    }
  }

  @Test
  public void testSubDirectoryCannotBeCreatedDueToPreexistingFile() throws IOException {
    Path file = Files.createTempFile(parent, "prefix", "suffix");
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get(file.toString(), "bar", "baz"));
      Assert.fail("Can create directory over file");
    } catch (NotDirectoryException ex) {
      Assert.assertNotNull(ex.getMessage());
      Assert.assertTrue(ex.getMessage().contains(file.getFileName().toString()));
    }
  }

  @Test
  public void testDirectoryCannotBeCreatedDueToUnwritableParent() throws IOException {
    Path dir = Files.createDirectory(Paths.get(parent.toString(), "child"));
    Assume.assumeTrue(dir.toFile().setWritable(false)); // On windows this isn't true
    dir.toFile().setWritable(false);
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get(dir.toString(), "bar"));
      Assert.fail("Can create directory in non-writable parent");
    } catch (AccessDeniedException ex) {
      Assert.assertNotNull(ex.getMessage());
      Assert.assertTrue(ex.getMessage().contains(dir.getFileName().toString()));
    }
  }

  @Test
  public void testRootNotWritable() throws IOException {
    Assume.assumeFalse(Files.isWritable(Paths.get("/")));
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get("/bar"));
      Assert.fail("Can create directory in root");
    } catch (AccessDeniedException ex) {
      Assert.assertNotNull(ex.getMessage());
      Assert.assertEquals("/ is not writable", ex.getMessage());
    }
  }
}
