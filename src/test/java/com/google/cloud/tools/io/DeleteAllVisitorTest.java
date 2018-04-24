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

package com.google.cloud.tools.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class DeleteAllVisitorTest {

  private DeleteAllVisitor visitor = new DeleteAllVisitor();
  
  @Test
  public void testDeleteFile() throws IOException {
    Path tempFile = Files.createTempFile("prefix", "suffix");
    visitor.visitFile(tempFile, null);
    Assert.assertFalse(Files.exists(tempFile));
  }
  
  @Test
  public void testDeleteDirectory() throws IOException {
    Path tempDirectory = Files.createTempFile("prefix", "suffix");
    DeleteAllVisitor visitor = new DeleteAllVisitor();
    visitor.postVisitDirectory(tempDirectory, null);
    Assert.assertFalse(Files.exists(tempDirectory));
  }
  
  @Test
  public void testDeleteDirectory_exception() throws IOException {
    Path tempDirectory = Files.createTempFile("prefix", "suffix");
    DeleteAllVisitor visitor = new DeleteAllVisitor();
    try {
      visitor.postVisitDirectory(tempDirectory, new IOException());
      Assert.fail("did not rethrow");
    } catch (IOException expected) {
    }
    Assert.assertTrue(Files.exists(tempDirectory));
  }
  
}
