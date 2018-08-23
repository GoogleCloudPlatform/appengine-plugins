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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ZipExtractorProviderTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();
  @Mock private ProgressListener mockProgressListener;

  private final ZipExtractorProvider zipExtractorProvider = new ZipExtractorProvider();

  @Test
  public void testCall() throws URISyntaxException, IOException {
    Path extractionRoot = tmp.getRoot().toPath();
    Path testArchive = getResource("genericArchives/test.zip");

    zipExtractorProvider.extract(testArchive, extractionRoot, mockProgressListener);

    GenericArchivesVerifier.assertArchiveExtraction(extractionRoot);
    // only check file permissions on non-windows
    if (!System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
      GenericArchivesVerifier.assertFilePermissions(extractionRoot);
    }

    ProgressVerifier.verifyUnknownProgress(
        mockProgressListener, "Extracting archive: " + testArchive.getFileName());
  }

  @Test
  public void testZipSlipVulnerability_windows() throws URISyntaxException {
    Assume.assumeTrue(System.getProperty("os.name").startsWith("Windows"));

    Path extractionRoot = tmp.getRoot().toPath();
    Path testArchive = getResource("zipSlipSamples/zip-slip-win.zip");
    try {
      zipExtractorProvider.extract(testArchive, extractionRoot, mockProgressListener);
      Assert.fail("IOException expected");
    } catch (IOException ex) {
      Assert.assertThat(
          ex.getMessage(), Matchers.startsWith("Blocked unzipping files outside destination: "));
    }
  }

  @Test
  public void testZipSlipVulnerability_unix() throws URISyntaxException {
    Assume.assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    Path extractionRoot = tmp.getRoot().toPath();
    Path testArchive = getResource("zipSlipSamples/zip-slip.zip");
    try {
      zipExtractorProvider.extract(testArchive, extractionRoot, mockProgressListener);
      Assert.fail("IOException expected");
    } catch (IOException ex) {
      Assert.assertThat(
          ex.getMessage(), Matchers.startsWith("Blocked unzipping files outside destination: "));
    }
  }

  private Path getResource(String resourcePath) throws URISyntaxException {
    Path resource = Paths.get(getClass().getClassLoader().getResource(resourcePath).toURI());
    Assert.assertTrue(Files.exists(resource));
    return resource;
  }
}
