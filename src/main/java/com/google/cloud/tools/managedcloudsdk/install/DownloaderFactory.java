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
import java.net.URL;
import java.nio.file.Path;

/** Downloader factory. */
final class DownloaderFactory {

  private final String userAgentString;

  /**
   * Creates a new factory.
   *
   * @param userAgentString for server side tracking of clients downloading the sdk. For example,
   *     "Cloud Tools for Eclipse" or "com.google.cloud.tools.appengine-maven-plguin".
   */
  public DownloaderFactory(String userAgentString) {
    this.userAgentString = userAgentString;
  }

  /**
   * Returns a new {@link Downloader} implementation.
   *
   * @param source URL of file to download (remote)
   * @param destination Path on local file system to save the file
   * @param progressListener Progress feedback handler
   * @return a {@link Downloader} instance
   */
  public Downloader newDownloader(URL source, Path destination, ProgressListener progressListener) {
    return new Downloader(source, destination, userAgentString, progressListener);
  }
}
