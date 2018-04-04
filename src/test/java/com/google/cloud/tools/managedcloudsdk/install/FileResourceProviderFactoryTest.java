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

import com.google.cloud.tools.managedcloudsdk.BadCloudSdkVersionException;
import com.google.cloud.tools.managedcloudsdk.OsInfo;
import com.google.cloud.tools.managedcloudsdk.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class FileResourceProviderFactoryTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  private Path fakeSdkHome;
  private Path fakeDownloadsDir;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            new OsInfo(OsInfo.Name.WINDOWS, OsInfo.Architecture.X86),
            "google-cloud-sdk-windows-bundled-python.zip",
            "windows-x86-bundled-python.zip",
            "gcloud.cmd"
          },
          {
            new OsInfo(OsInfo.Name.WINDOWS, OsInfo.Architecture.X86_64),
            "google-cloud-sdk-windows-x86_64-bundled-python.zip",
            "windows-x86_64-bundled-python.zip",
            "gcloud.cmd"
          },
          {
            new OsInfo(OsInfo.Name.MAC, OsInfo.Architecture.X86),
            "google-cloud-sdk.tar.gz",
            "darwin-x86.tar.gz",
            "gcloud"
          },
          {
            new OsInfo(OsInfo.Name.MAC, OsInfo.Architecture.X86_64),
            "google-cloud-sdk.tar.gz",
            "darwin-x86_64.tar.gz",
            "gcloud"
          },
          {
            new OsInfo(OsInfo.Name.LINUX, OsInfo.Architecture.X86),
            "google-cloud-sdk.tar.gz",
            "linux-x86.tar.gz",
            "gcloud"
          },
          {
            new OsInfo(OsInfo.Name.LINUX, OsInfo.Architecture.X86_64),
            "google-cloud-sdk.tar.gz",
            "linux-x86_64.tar.gz",
            "gcloud"
          },
        });
  }

  @Parameterized.Parameter(0)
  public OsInfo osInfo;

  @Parameterized.Parameter(1)
  public String latestFilename;

  @Parameterized.Parameter(2)
  public String versionedFilenameTail;

  @Parameterized.Parameter(3)
  public String gcloudExecutable;

  @Before
  public void setUp() {
    fakeSdkHome = testDir.getRoot().toPath();
    fakeDownloadsDir = fakeSdkHome.resolve("downloads");
  }

  @Test
  public void testNewFileResourceProvider_latest() throws MalformedURLException {
    FileResourceProviderFactory factory =
        new FileResourceProviderFactory(Version.LATEST, osInfo, fakeSdkHome);
    FileResourceProvider provider = factory.newFileResourceProvider();

    Assert.assertEquals(
        new URL(FileResourceProviderFactory.LATEST_BASE_URL + latestFilename),
        provider.getArchiveSource());
    Assert.assertEquals(fakeDownloadsDir.resolve(latestFilename), provider.getArchiveDestination());
    Assert.assertEquals(fakeSdkHome.resolve("LATEST"), provider.getArchiveExtractionDestination());
    Assert.assertEquals(
        fakeSdkHome.resolve("LATEST").resolve("google-cloud-sdk"), provider.getExtractedSdkHome());
    Assert.assertEquals(
        fakeSdkHome
            .resolve("LATEST")
            .resolve("google-cloud-sdk")
            .resolve("bin")
            .resolve(gcloudExecutable),
        provider.getExtractedGcloud());
  }

  @Test
  public void testNewFileResourceProvider_versioned()
      throws MalformedURLException, BadCloudSdkVersionException {
    FileResourceProviderFactory factory =
        new FileResourceProviderFactory(new Version("123.123.123"), osInfo, fakeSdkHome);
    FileResourceProvider provider = factory.newFileResourceProvider();

    Assert.assertEquals(
        new URL(
            FileResourceProviderFactory.VERSIONED_BASE_URL
                + "google-cloud-sdk-123.123.123-"
                + versionedFilenameTail),
        provider.getArchiveSource());
    Assert.assertEquals(
        fakeDownloadsDir.resolve("google-cloud-sdk-123.123.123-" + versionedFilenameTail),
        provider.getArchiveDestination());
    Assert.assertEquals(
        fakeSdkHome.resolve("123.123.123"), provider.getArchiveExtractionDestination());
    Assert.assertEquals(
        fakeSdkHome.resolve("123.123.123").resolve("google-cloud-sdk"),
        provider.getExtractedSdkHome());
    Assert.assertEquals(
        fakeSdkHome
            .resolve("123.123.123")
            .resolve("google-cloud-sdk")
            .resolve("bin")
            .resolve(gcloudExecutable),
        provider.getExtractedGcloud());
  }
}
