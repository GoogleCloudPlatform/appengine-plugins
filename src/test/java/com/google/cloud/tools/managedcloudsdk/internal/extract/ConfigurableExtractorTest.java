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

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ConfigurableExtractorTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testCall_success() throws Exception {
    Path extractionDestination = tmp.getRoot().toPath();
    Path extractionSource = tmp.newFile("fake.archive").toPath();
    final Path expectedCloudSdkHome = tmp.getRoot().toPath().resolve("google-cloud-sdk");

    ExtractorProvider mockProvider = Mockito.mock(ExtractorProvider.class);
    ExtractorMessageListener mockListener = Mockito.mock(ExtractorMessageListener.class);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        // pretend to extract by creating the expected final directory (for success!)
        Files.createDirectory(expectedCloudSdkHome);
        return null;
      }
    }).when(mockProvider).extract(extractionSource, extractionDestination, mockListener);

    Extractor extractor = new ConfigurableExtractor<>(extractionSource, extractionDestination, mockProvider, mockListener);
    Path cloudSdkHomeUnderTest = extractor.call();

    Assert.assertEquals(expectedCloudSdkHome, cloudSdkHomeUnderTest);
    Mockito.verify(mockListener, Mockito.times(1)).message("Extracting archive: " + extractionSource.toString());
  }

  @Test
  public void testCall_archiveStructureFailure() throws Exception {
    ExtractorProvider mockProvider = Mockito.mock(ExtractorProvider.class);
    Path extractionDestination = tmp.getRoot().toPath();
    Path extractionSource = tmp.newFile("fake.archive").toPath();

    Extractor extractor = new ConfigurableExtractor<>(extractionSource, extractionDestination, mockProvider, null);
    try {
      Path cloudSdkHome = extractor.call();
      Assert.fail("FileNotFoundException expected but not thrown");
    } catch (FileNotFoundException ex) {
      Assert.assertEquals("After extraction, Cloud SDK home not found at " + tmp.getRoot().toPath().resolve("google-cloud-sdk"), ex.getMessage());
    }
  }
}
