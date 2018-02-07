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
import java.nio.file.Path;

/** Provide a archive extractor implementation. */
interface ExtractorProvider {

  /**
   * Extracts a single file archive into target destination folder.
   *
   * @param archive the archive to extract
   * @param destination the destination folder for extracted files
   * @param progressListener the progress listener passthrough from the extractor
   * @throws IOException if extractor fails
   */
  void extract(Path archive, Path destination, ProgressListener progressListener)
      throws IOException;
}
