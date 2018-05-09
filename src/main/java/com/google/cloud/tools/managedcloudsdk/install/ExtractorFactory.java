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
import java.nio.file.Path;

/** {@link Extractor} Factory. */
final class ExtractorFactory {

  /**
   * Creates a new extractor based on filetype. Filetype determination is based on the filename
   * string, this method makes no attempt to validate the file contents to verify they are the type
   * defined by the file extension.
   *
   * @param archive the archive to extract
   * @param destination the destination folder for extracted files
   * @param messageListener a listener for extraction messages
   * @return {@link Extractor} with {@link TarGzExtractorProvider} for ".tar.gz", {@link
   *     ZipExtractorProvider} for ".zip"
   * @throws UnknownArchiveTypeException if not ".tar.gz" or ".zip"
   */
  public Extractor newExtractor(Path archive, Path destination, ProgressListener progressListener)
      throws UnknownArchiveTypeException {

    if (archive.toString().toLowerCase().endsWith(".tar.gz")) {
      return new Extractor(archive, destination, new TarGzExtractorProvider(), progressListener);
    }
    if (archive.toString().toLowerCase().endsWith(".zip")) {
      return new Extractor(archive, destination, new ZipExtractorProvider(), progressListener);
    }
    throw new UnknownArchiveTypeException(archive);
  }
}
