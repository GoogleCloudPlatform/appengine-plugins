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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class FileResourceProviderTest {

  @Test
  public void testFileResourceProvider_smokeTest() throws MalformedURLException {
    URL testSrc = new URL("https://www.example.com");
    Path testDest = Paths.get("/tmp/file.archive");
    Path testExtractionDest = Paths.get("/tmp/extract");
    String testGcloudExecutableName = "gcloud.xyz";
    FileResourceProvider testProvider =
        new FileResourceProvider(testSrc, testDest, testExtractionDest, testGcloudExecutableName);

    Assert.assertEquals(testSrc, testProvider.getArchiveSource());
    Assert.assertEquals(testDest, testProvider.getArchiveDestination());
    Assert.assertEquals(testExtractionDest, testProvider.getArchiveExtractionDestination());
    Assert.assertEquals(
        testExtractionDest.resolve("google-cloud-sdk"), testProvider.getExtractedSdkHome());
    Assert.assertEquals(
        testExtractionDest
            .resolve("google-cloud-sdk")
            .resolve("bin")
            .resolve(testGcloudExecutableName),
        testProvider.getExtractedGcloud());
  }
}
