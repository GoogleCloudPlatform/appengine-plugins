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

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.OsInfo;
import com.google.cloud.tools.managedcloudsdk.Version;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/** Install an SDK by downloading, extracting and if necessary installing. */
public class SdkInstaller {

  private final ListeningExecutorService executorService;
  private final FileResourceProviderFactory fileResourceProviderFactory;
  private final ExtractorFactory extractorFactory;
  private final DownloaderFactory downloaderFactory;
  private final InstallerFactory installerFactory;

  /** Use {@link #newInstaller} to instantiate. */
  SdkInstaller(
      FileResourceProviderFactory fileResourceProviderFactory,
      DownloaderFactory downloaderFactory,
      ExtractorFactory extractorFactory,
      InstallerFactory installerFactory,
      ListeningExecutorService executorService) {
    this.fileResourceProviderFactory = fileResourceProviderFactory;
    this.executorService = executorService;
    this.downloaderFactory = downloaderFactory;
    this.extractorFactory = extractorFactory;
    this.installerFactory = installerFactory;
  }

  /**
   * Download a Cloud SDK asynchronously.
   *
   * @param messageListener a listener on installer output
   * @return a path to the Cloud SDK home directory (wrapped in a listenable future)
   */
  public ListenableFuture<Path> downloadSdk(final MessageListener messageListener) {
    if (executorService.isShutdown()) {
      throw new IllegalStateException("downloadSdk has already run");
    }
    ListenableFuture<Path> resultFuture =
        executorService.submit(
            new Callable<Path>() {
              @Override
              public Path call() throws Exception {
                return downloadSdkSync(messageListener);
              }
            });
    executorService.shutdown(); // shutdown executor after install
    return resultFuture;
  }

  Path downloadSdkSync(final MessageListener messageListener)
      throws IOException, InterruptedException, SdkInstallerException, UnknownArchiveTypeException,
          ExecutionException, CommandExitException {

    FileResourceProvider fileResourceProvider =
        fileResourceProviderFactory.newFileResourceProvider();

    // CLEANUP: remove old downloaded archive if exists
    if (Files.isRegularFile(fileResourceProvider.getArchiveDestination())) {
      messageListener.message(
          "Removing stale archive: " + fileResourceProvider.getArchiveDestination() + "\n");
      Files.delete(fileResourceProvider.getArchiveDestination());
    }

    // CLEANUP: Remove old SDK directory if exists
    if (Files.exists(fileResourceProvider.getArchiveExtractionDestination())) {
      messageListener.message(
          "Removing stale install: "
              + fileResourceProvider.getArchiveExtractionDestination()
              + "\n");
      Files.walkFileTree(
          fileResourceProvider.getArchiveExtractionDestination(), new FileDeleteVisitor());
    }

    // Download and verify
    downloaderFactory
        .newDownloader(
            fileResourceProvider.getArchiveSource(),
            fileResourceProvider.getArchiveDestination(),
            messageListener)
        .download();
    if (!Files.isRegularFile(fileResourceProvider.getArchiveDestination())) {
      throw new SdkInstallerException(
          "Download succeeded but valid archive not found at "
              + fileResourceProvider.getArchiveDestination());
    }

    // extract and verify
    extractorFactory
        .newExtractor(
            fileResourceProvider.getArchiveDestination(),
            fileResourceProvider.getArchiveExtractionDestination(),
            messageListener)
        .extract();
    if (!Files.isDirectory(fileResourceProvider.getExtractedSdkHome())) {
      throw new SdkInstallerException(
          "Extraction succeeded but valid sdk home not found at "
              + fileResourceProvider.getExtractedSdkHome());
    }

    // install if necessary
    if (installerFactory != null) {
      installerFactory
          .newInstaller(fileResourceProvider.getExtractedSdkHome(), messageListener)
          .install();
    }

    // verify final state
    if (!Files.isRegularFile(fileResourceProvider.getExtractedGcloud())) {
      throw new SdkInstallerException(
          "Installation succeeded but gcloud executable not found at "
              + fileResourceProvider.getExtractedGcloud());
    }

    return fileResourceProvider.getExtractedSdkHome();
  }

  /**
   * Configure and create a new Installer instance.
   *
   * @param managedSdkDirectory Home directory of google cloud java managed cloud sdks
   * @param version Version of the cloud sdk we want to install
   * @param osInfo Target operating system for installation
   * @param userAgentString User agent string for https requests
   * @param usageReporting Enable client side usage reporting on gcloud
   * @return a new configured Cloud sdk Installer
   */
  public static SdkInstaller newInstaller(
      Path managedSdkDirectory,
      Version version,
      OsInfo osInfo,
      String userAgentString,
      boolean usageReporting) {
    DownloaderFactory downloaderFactory = new DownloaderFactory(userAgentString);
    ExtractorFactory extractorFactory = new ExtractorFactory();

    InstallerFactory installerFactory =
        version == Version.LATEST ? new InstallerFactory(osInfo, usageReporting) : null;

    FileResourceProviderFactory fileResourceProviderFactory =
        new FileResourceProviderFactory(version, osInfo, managedSdkDirectory);

    ListeningExecutorService executorService =
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    return new SdkInstaller(
        fileResourceProviderFactory,
        downloaderFactory,
        extractorFactory,
        installerFactory,
        executorService);
  }
}
