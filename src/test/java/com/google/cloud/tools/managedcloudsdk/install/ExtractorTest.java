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

import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExtractorTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();
  @Mock private ProgressListener mockProgressListener;
  @Mock private ExtractorProvider mockExtractorProvider;

  @Test
  public void testExtract_success() throws Exception {
    final Path extractionDestination = tmp.newFolder("target").toPath();
    Path extractionSource = tmp.newFile("fake.archive").toPath();

    Mockito.doAnswer(
            invocation -> {
              Files.createDirectory(extractionDestination.resolve("some-dir"));
              Files.createFile(extractionDestination.resolve("some-file"));
              return null;
            })
        .when(mockExtractorProvider)
        .extract(extractionSource, extractionDestination, mockProgressListener);

    Extractor extractor =
        new Extractor(
            extractionSource, extractionDestination, mockExtractorProvider, mockProgressListener);

    extractor.extract();

    Assert.assertTrue(Files.exists(extractionDestination));
    Mockito.verify(mockExtractorProvider)
        .extract(extractionSource, extractionDestination, mockProgressListener);
  }

  @Test
  public void testExtract_cleanupAfterException() throws Exception {
    final Path extractionDestination = tmp.newFolder("target").toPath();
    Path extractionSource = tmp.newFile("fake.archive").toPath();

    Mockito.doAnswer(
            invocation -> {
              // pretend to extract by creating the expected final directory (for success!)
              Files.createDirectory(extractionDestination.resolve("some-dir"));
              Files.createFile(extractionDestination.resolve("some-file"));
              throw new IOException("Failed during extraction");
            })
        .when(mockExtractorProvider)
        .extract(extractionSource, extractionDestination, mockProgressListener);

    Extractor extractor =
        new Extractor(
            extractionSource, extractionDestination, mockExtractorProvider, mockProgressListener);

    try {
      extractor.extract();
      Assert.fail("IOException expected but thrown - test infrastructure failure");
    } catch (IOException ex) {
      // ensure we are rethrowing after cleanup
      Assert.assertEquals("Failed during extraction", ex.getMessage());
    }

    Assert.assertFalse(Files.exists(extractionDestination));
    Mockito.verify(mockExtractorProvider)
        .extract(extractionSource, extractionDestination, mockProgressListener);
  }
}
