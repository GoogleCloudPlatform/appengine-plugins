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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class FilePermissionsTest {

  private Path parent;
  
  @Before
  public void setUp() throws IOException {
     parent = Files.createTempDirectory("foo");
  }

  @Test
  public void testDirectoryCanBeCreated() throws IOException {
    FilePermissions.verifyDirectoryCreatable(Paths.get(parent.toString(), "bar"));
  }

  @Test
  public void testSubDirectoryCanBeCreated() throws IOException {
    FilePermissions.verifyDirectoryCreatable(Paths.get(parent.toString(), "bar", "baz"));
  }
  
  @Test
  public void testSubDirectoryCannotBeCreatedInDevNull()  {
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get("/dev/null/foo/bar"));
      Assert.fail("Can create directory in /dev/null");
    } catch (IOException ex) {
      Assert.assertTrue(ex.getMessage(), ex.getMessage().contains("/dev/null"));
    }
  }
  
  @Test
  public void testDirectoryCannotBeCreatedDueToPreexistingFile() throws IOException {
    Path file = Files.createTempFile(parent, "prefix", "suffix");
    try {
      FilePermissions.verifyDirectoryCreatable(file);
      Assert.fail("Can create directory over file");
    } catch (FileAlreadyExistsException ex) {
      Assert.assertTrue(ex.getMessage().contains(file.getFileName().toString()));
    }
  }
  
  @Test
  public void testSubDirectoryCannotBeCreatedDueToPreexistingFile() throws IOException {
    Path file = Files.createTempFile(parent, "prefix", "suffix");
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get(file.toString(), "bar", "baz"));
      Assert.fail("Can create directory over file");
    } catch (FileAlreadyExistsException ex) {
      Assert.assertTrue(ex.getMessage().contains(file.getFileName().toString()));
    }
  }
  
  @Test
  public void testDirectoryCannotBeCreatedDueToUnwritableParent() throws IOException {
    Path dir = Files.createDirectory(Paths.get(parent.toString(), "child"));
    dir.toFile().setWritable(false);
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get(dir.toString(), "bar"));
      Assert.fail("Can create directory in non-writable parent");
    } catch (IOException ex) {
      Assert.assertTrue(ex.getMessage().contains(dir.getFileName().toString()));
    }
  }
  
  @Test
  public void testRootNotWritable() throws IOException {
    try {
      FilePermissions.verifyDirectoryCreatable(Paths.get("/bar"));
      Assert.fail("Can create directory in root");
    } catch (IOException ex) {
      Assert.assertEquals("/ is not writable", ex.getMessage());
    }
  }

}
