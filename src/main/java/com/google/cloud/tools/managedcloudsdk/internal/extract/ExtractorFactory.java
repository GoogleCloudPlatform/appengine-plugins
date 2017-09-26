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

import java.nio.file.Path;

/** {@link Extractor} Factory. */
public final class ExtractorFactory {

  /**
   * Creates a new extractor based on filetype. Filetype determination is based on the filename
   * string, this method makes no attempt to validate the actual file contents to verify they are
   * indeed of the type defined by the file extension.
   *
   * @param archive The archive to extract
   * @param destination The destination folder for extracted files
   * @param extractorMessageListener An listener for extraction messages
   * @return {@link ConfigurableExtractor} with {@link TarGzExtractorProvider} for ".tar.gz", {@link
   *     ZipExtractorProvider} for ".zip"
   * @throws UnknownArchiveTypeException if not ".tar.gz" or ".zip"
   */
  public Extractor newExtractor(
      Path archive, Path destination, ExtractorMessageListener extractorMessageListener)
      throws UnknownArchiveTypeException {

    if (archive.toString().endsWith(".tar.gz")) {
      return new ConfigurableExtractor<>(
          archive, destination, new TarGzExtractorProvider(), extractorMessageListener);

    } else if (archive.toString().endsWith(".zip")) {
      return new ConfigurableExtractor<>(
          archive, destination, new ZipExtractorProvider(), extractorMessageListener);

    } else {
      throw new UnknownArchiveTypeException(archive);
    }
  }
}
