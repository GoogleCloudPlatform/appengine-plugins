/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.io;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Non-IDE specific utility methods for working with file systems.
 * We hope to migrate these into Guava. Clients should beware that these methods are
 * not yet stable and may change without notice. 
 */
@Beta
public class FilePermissions {

  /**
   * Check whether the current process can create a directory at the specified path.
   * This is useful for providing immediate feedback to an end user that a path they have 
   * selected or typed may not be suitable before attempting to create the directory;
   * e.g. in a tooltip.
   * 
   * @param path tentative location for directory
   * @throws IOException if a directory cannot be created at the specified path
   */
  public static void verifyDirectoryCreatable(Path path) throws IOException {
    
    // Can't create a directory if a non-directory file already exists with that name
    // somewhere in the path. 
    for (Path segment = path; segment != null; segment = segment.getParent()) {
      if (Files.isRegularFile(segment)) {
        throw new FileAlreadyExistsException(segment + " is a file");
      }
    }
    
    // Can't create a directory if the bottom most currently existing directory in
    // the path is not writable. 
    for (Path segment = path; segment != null; segment = segment.getParent()) {
      if (Files.isDirectory(segment)) {
        if (!Files.isWritable(segment)) {
          throw new IOException(path + " is not writable");
        }
        break;
      }
    }
    
  }

}
