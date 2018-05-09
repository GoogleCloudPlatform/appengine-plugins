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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TarGzExtractorProviderTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();
  @Mock private ProgressListener mockProgressListener;

  @Test
  public void testCall() throws Exception {
    Path extractionRoot = tmp.getRoot().toPath();
    Path testArchive =
        Paths.get(getClass().getClassLoader().getResource("genericArchives/test.tar.gz").toURI());
    Assert.assertTrue(Files.exists(testArchive));

    TarGzExtractorProvider tarGzExtractorProvider = new TarGzExtractorProvider();

    tarGzExtractorProvider.extract(testArchive, extractionRoot, mockProgressListener);

    GenericArchivesVerifier.assertArchiveExtraction(extractionRoot);
    // only check file permissions on non-windows
    if (!System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
      GenericArchivesVerifier.assertFilePermissions(extractionRoot);
    }

    ProgressVerifier.verifyUnknownProgress(
        mockProgressListener, "Extracting archive: " + testArchive.getFileName());
  }
}
