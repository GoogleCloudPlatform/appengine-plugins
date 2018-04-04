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

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** Tests for {@link SdkInstaller} */
public class SdkInstallerTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private FileResourceProviderFactory fileResourceProviderFactory;
  @Mock private ProgressListener progressListener;
  @Mock private ConsoleListener consoleListener;

  @Mock private DownloaderFactory successfulDownloaderFactory;
  @Mock private Downloader successfulDownloader;
  @Mock private ExtractorFactory successfulLatestExtractorFactory;
  @Mock private Extractor<ExtractorProvider> successfulLatestExtractor;
  @Mock private ExtractorFactory successfulVersionedExtractorFactory;
  @Mock private Extractor<ExtractorProvider> successfulVersionedExtractor;
  @Mock private InstallerFactory successfulInstallerFactory;
  @Mock private Installer<InstallScriptProvider> successfulInstaller;

  @Mock private DownloaderFactory failureDownloaderFactory;
  @Mock private Downloader failureDownloader;
  @Mock private ExtractorFactory failureExtractorFactory;
  @Mock private Extractor<ExtractorProvider> failureExtractor;
  @Mock private InstallerFactory failureInstallerFactory;
  @Mock private Installer<InstallScriptProvider> failureInstaller;

  private FileResourceProvider fakeFileResourceProvider;
  private URL fakeArchiveSource;
  private Path fakeArchiveDestination;
  private Path fakeArchiveExtractionDestination;
  private Path fakeSdkHome;
  private String fakeGcloudExecutable;
  private Path fakeGcloud;

  @Before
  public void setUpMocksAndFakes()
      throws IOException, InterruptedException, UnknownArchiveTypeException, CommandExitException,
          CommandExecutionException {
    MockitoAnnotations.initMocks(this);

    Path managedSdkRoot = testDir.newFolder("managed-sdk-test-home").toPath();
    fakeArchiveSource = new URL("file:///some/fake/url");
    fakeArchiveDestination = managedSdkRoot.resolve("test-downloads");
    fakeArchiveExtractionDestination = managedSdkRoot.resolve("test-version");
    fakeGcloudExecutable = "test-gcloud";

    fakeFileResourceProvider =
        new FileResourceProvider(
            fakeArchiveSource,
            fakeArchiveDestination,
            fakeArchiveExtractionDestination,
            fakeGcloudExecutable);

    fakeSdkHome = fakeFileResourceProvider.getExtractedSdkHome();
    fakeGcloud = fakeFileResourceProvider.getExtractedGcloud();

    Mockito.when(fileResourceProviderFactory.newFileResourceProvider())
        .thenReturn(fakeFileResourceProvider);

    Mockito.when(progressListener.newChild(Mockito.any(Long.class))).thenReturn(progressListener);

    // SUCCESS MOCKS
    Mockito.when(
            successfulDownloaderFactory.newDownloader(
                fakeArchiveSource, fakeArchiveDestination, progressListener))
        .thenReturn(successfulDownloader);
    Mockito.doAnswer(createPathAnswer(fakeArchiveDestination, false))
        .when(successfulDownloader)
        .download();

    // A "LATEST" extractor will result in a cloud sdk home with no gcloud file until install
    Mockito.<Extractor<? extends ExtractorProvider>>when(
            successfulLatestExtractorFactory.newExtractor(
                fakeArchiveDestination, fakeArchiveExtractionDestination, progressListener))
        .thenReturn(successfulLatestExtractor);
    Mockito.doAnswer(
            createPathAnswer(fakeArchiveExtractionDestination.resolve("google-cloud-sdk"), true))
        .when(successfulLatestExtractor)
        .extract();

    // A "versioned" extractor will result in a gcloud file
    Mockito.<Extractor<? extends ExtractorProvider>>when(
            successfulVersionedExtractorFactory.newExtractor(
                fakeArchiveDestination, fakeArchiveExtractionDestination, progressListener))
        .thenReturn(successfulVersionedExtractor);
    Mockito.doAnswer(createPathAnswer(fakeGcloud, false))
        .when(successfulVersionedExtractor)
        .extract();

    Mockito.<Installer<? extends InstallScriptProvider>>when(
            successfulInstallerFactory.newInstaller(fakeSdkHome, progressListener, consoleListener))
        .thenReturn(successfulInstaller);
    Mockito.doAnswer(createPathAnswer(fakeGcloud, false)).when(successfulInstaller).install();

    // FAIL MOCKS
    Mockito.when(
            failureDownloaderFactory.newDownloader(
                fakeArchiveSource, fakeArchiveDestination, progressListener))
        .thenReturn(failureDownloader);
    Mockito.doNothing().when(failureDownloader).download();

    Mockito.<Extractor<? extends ExtractorProvider>>when(
            failureExtractorFactory.newExtractor(
                fakeArchiveDestination, fakeArchiveExtractionDestination, progressListener))
        .thenReturn(failureExtractor);
    Mockito.doNothing().when(failureExtractor).extract();

    Mockito.<Installer<? extends InstallScriptProvider>>when(
            failureInstallerFactory.newInstaller(fakeSdkHome, progressListener, consoleListener))
        .thenReturn(failureInstaller);
    Mockito.doNothing().when(failureInstaller).install();
  }

  private Answer<Void> createPathAnswer(final Path pathToCreate, final boolean isDirectory) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        if (!pathToCreate.startsWith(testDir.getRoot().toPath())) {
          throw new IllegalArgumentException("Test should not create files outside the test root");
        }
        if (isDirectory) {
          Files.createDirectories(pathToCreate);
        } else {
          Files.createDirectories(pathToCreate.getParent());
          Files.createFile(pathToCreate);
        }
        return null;
      }
    };
  }

  @Test
  public void testDownloadSdk_successRun()
      throws CommandExecutionException, InterruptedException, IOException, CommandExitException,
          SdkInstallerException {

    SdkInstaller testInstaller =
        new SdkInstaller(
            fileResourceProviderFactory,
            successfulDownloaderFactory,
            successfulLatestExtractorFactory,
            successfulInstallerFactory);
    Path result = testInstaller.install(progressListener, consoleListener);

    Assert.assertEquals(fakeSdkHome, result);
  }

  @Test
  public void testDownloadSdk_successRunWithoutExplicitInstall()
      throws CommandExecutionException, InterruptedException, IOException, CommandExitException,
          SdkInstallerException {
    SdkInstaller testInstaller =
        new SdkInstaller(
            fileResourceProviderFactory,
            successfulDownloaderFactory,
            successfulVersionedExtractorFactory,
            null);
    Path result = testInstaller.install(progressListener, consoleListener);

    Assert.assertEquals(fakeSdkHome, result);
  }

  @Test
  public void testDownloadSdk_failedDownload()
      throws InterruptedException, CommandExecutionException, CommandExitException, IOException {

    SdkInstaller testInstaller =
        new SdkInstaller(
            fileResourceProviderFactory,
            failureDownloaderFactory,
            successfulLatestExtractorFactory,
            successfulInstallerFactory);
    try {
      testInstaller.install(progressListener, consoleListener);
      Assert.fail("SdKInstallerException expected but not thrown");
    } catch (SdkInstallerException ex) {
      Assert.assertEquals(
          "Download succeeded but valid archive not found at " + fakeArchiveDestination.toString(),
          ex.getMessage());
    }
  }

  @Test
  public void testDownloadSdk_failedExtraction()
      throws InterruptedException, IOException, CommandExitException, CommandExecutionException {

    SdkInstaller testInstaller =
        new SdkInstaller(
            fileResourceProviderFactory,
            successfulDownloaderFactory,
            failureExtractorFactory,
            successfulInstallerFactory);
    try {
      testInstaller.install(progressListener, consoleListener);
      Assert.fail("SdKInstallerException expected but not thrown");
    } catch (SdkInstallerException ex) {
      Assert.assertEquals(
          "Extraction succeeded but valid sdk home not found at " + fakeSdkHome.toString(),
          ex.getMessage());
    }
  }

  @Test
  public void testDownloadSdk_failedInstallation()
      throws InterruptedException, IOException, CommandExitException, CommandExecutionException {

    SdkInstaller testInstaller =
        new SdkInstaller(
            fileResourceProviderFactory,
            successfulDownloaderFactory,
            successfulLatestExtractorFactory,
            failureInstallerFactory);
    try {
      testInstaller.install(progressListener, consoleListener);
      Assert.fail("SdKInstallerException expected but not thrown");
    } catch (SdkInstallerException ex) {
      Assert.assertEquals(
          "Installation succeeded but gcloud executable not found at " + fakeGcloud.toString(),
          ex.getMessage());
    }
  }
}
