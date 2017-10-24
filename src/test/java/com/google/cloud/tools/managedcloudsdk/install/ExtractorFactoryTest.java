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
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExtractorFactoryTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testNewExtractor_isZip() throws IOException, UnknownArchiveTypeException {
    Path archive = tmp.newFile("test-zip.zip").toPath();
    Extractor testExtractor = new ExtractorFactory().newExtractor(archive, null, null);
    Assert.assertTrue(testExtractor.getExtractorProvider() instanceof ZipExtractorProvider);
  }

  @Test
  public void testNewExtractor_isTarGz() throws IOException, UnknownArchiveTypeException {
    Path archive = tmp.newFile("test-tar-gz.tar.gz").toPath();
    Extractor testExtractor = new ExtractorFactory().newExtractor(archive, null, null);
    Assert.assertTrue(testExtractor.getExtractorProvider() instanceof TarGzExtractorProvider);
  }

  @Test
  public void testNewExtractor_unknownArchiveType() throws IOException {
    // make sure out check starts from end of filename
    Path archive = tmp.newFile("test-bad.tar.gz.zip.bad").toPath();

    try {
      new ExtractorFactory().newExtractor(archive, null, null);
      Assert.fail("UnknownArchiveTypeException expected but not thrown");
    } catch (UnknownArchiveTypeException ex) {
      Assert.assertEquals("Unknown archive: " + archive, ex.getMessage());
    }
  }
}
