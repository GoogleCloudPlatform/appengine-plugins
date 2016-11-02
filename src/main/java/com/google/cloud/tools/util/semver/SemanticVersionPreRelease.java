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

package com.google.cloud.tools.util.semver;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class SemanticVersionPreRelease implements Comparable<SemanticVersionPreRelease> {

  public SemanticVersionPreRelease(String preRelease) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(preRelease));
    String[] split = preRelease.split("\\.");
  }

  /**
   * Compares this to another SemanticVersionPreRelease.
   *
   * <p>Precedence for two pre-release versions with the same major, minor, and patch version MUST
   * be determined by comparing each dot separated identifier from left to right until a difference
   * is found as follows: identifiers consisting of only digits are compared numerically and
   * identifiers with letters or hyphens are compared lexically in ASCII sort order. Numeric
   * identifiers always have lower precedence than non-numeric identifiers.</p>
   */
  @Override
  public int compareTo(SemanticVersionPreRelease other) {
    if (other == null) {
      return -1;
    }



    return 0;
  }

  /**
   * Helper class that represents a component of the pre-release.
   */
  private static class PreReleaseComponent implements Comparable<PreReleaseComponent> {

    @Override
    public int compareTo(PreReleaseComponent other) {
      // TODO
      return 0;
    }
  }
}
