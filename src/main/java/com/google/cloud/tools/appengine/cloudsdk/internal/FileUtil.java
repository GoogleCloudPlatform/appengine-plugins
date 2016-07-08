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

package com.google.cloud.tools.appengine.cloudsdk.internal;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Internal file utilities.
 */
public class FileUtil {

  /**
   * Implementation of recursive directory copy, does NOT overwrite
   *
   * @param source an existing source directory to copy from.
   * @param destination an existing destination directory to copy to.
   */
  public static void copyDirectory(final Path source, final Path destination) throws IOException {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(destination);
    Preconditions.checkArgument(Files.isDirectory(source));
    Preconditions.checkArgument(Files.isDirectory(destination));
    Preconditions.checkArgument(!source.equals(destination));
    Preconditions.checkArgument(!destination.startsWith(source), "destination is child of source");

    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
      final CopyOption[] copyOptions = new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES };

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {

        if (dir.equals(source)) {
          return FileVisitResult.CONTINUE;
        }

        Files.copy(dir, destination.resolve(source.relativize(dir)), copyOptions);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        Files.copy(file, destination.resolve(source.relativize(file)), copyOptions);
        return FileVisitResult.CONTINUE;
      }
    });

  }

}
