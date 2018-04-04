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

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

/** Utilities for files on Posix based systems. */
final class PosixUtil {

  /** Convert integer mode to {@link PosixFilePermission} object. */
  static Set<PosixFilePermission> getPosixFilePermissions(int mode) {
    Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);

    if ((mode & 0400) != 0) {
      result.add(PosixFilePermission.OWNER_READ);
    }
    if ((mode & 0200) != 0) {
      result.add(PosixFilePermission.OWNER_WRITE);
    }
    if ((mode & 0100) != 0) {
      result.add(PosixFilePermission.OWNER_EXECUTE);
    }
    if ((mode & 040) != 0) {
      result.add(PosixFilePermission.GROUP_READ);
    }
    if ((mode & 020) != 0) {
      result.add(PosixFilePermission.GROUP_WRITE);
    }
    if ((mode & 010) != 0) {
      result.add(PosixFilePermission.GROUP_EXECUTE);
    }
    if ((mode & 04) != 0) {
      result.add(PosixFilePermission.OTHERS_READ);
    }
    if ((mode & 02) != 0) {
      result.add(PosixFilePermission.OTHERS_WRITE);
    }
    if ((mode & 01) != 0) {
      result.add(PosixFilePermission.OTHERS_EXECUTE);
    }
    return result;
  }
}
