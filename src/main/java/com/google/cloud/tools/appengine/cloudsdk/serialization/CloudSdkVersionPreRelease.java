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

package com.google.cloud.tools.appengine.cloudsdk.serialization;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the pre-release field in a Cloud SDK Version. The pre-release field is comprised of
 * one or more dot-separated segments, which are can be treated as either numeric-only, or
 * alphanumeric, depending on their contents.
 *
 * <p>For example, in a Cloud SDK Version like <code>"0.1.0-beta.1"</code>, the pre-release field is
 * <code>"beta.1"</code>.
 */
class CloudSdkVersionPreRelease implements Comparable<CloudSdkVersionPreRelease> {

  private List<PreReleaseSegment> segments;
  private final String preRelease;

  /** Constructs a new CloudSdkVersionPreRelease from a string representation. */
  public CloudSdkVersionPreRelease(String preRelease) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(preRelease));

    this.segments = new ArrayList<>();
    this.preRelease = preRelease;

    Iterable<String> segmentParts = Splitter.on('.').split(preRelease);
    for (String segment : segmentParts) {
      segments.add(new PreReleaseSegment(segment));
    }
  }

  /**
   * Compares this to another CloudSdkVersionPreRelease.
   *
   * <p>Precedence for two pre-release versions MUST be determined by comparing each dot separated
   * identifier from left to right until a difference is found as follows: identifiers consisting of
   * only digits are compared numerically and identifiers with letters or hyphens are compared
   * lexically in ASCII sort order. Numeric identifiers always have lower precedence than
   * non-numeric identifiers.
   */
  @Override
  public int compareTo(CloudSdkVersionPreRelease other) {
    Preconditions.checkNotNull(other);

    // Compare segments from left to right. A smaller number of pre-release segments comes before a
    // higher number, if all preceding segments are equal.
    int index = 0;
    while (index < this.segments.size() && index < other.segments.size()) {
      int result = this.segments.get(index).compareTo(other.segments.get(index));
      if (result != 0) {
        return result;
      }
      index++;
    }

    // If we've reached this point, the smaller list comes first.
    if (this.segments.size() < other.segments.size()) {
      return -1;
    } else if (this.segments.size() > other.segments.size()) {
      return 1;
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    CloudSdkVersionPreRelease other = (CloudSdkVersionPreRelease) obj;
    return this.preRelease.equals(other.preRelease);
  }

  @Override
  public int hashCode() {
    return Objects.hash(preRelease);
  }

  @Override
  public String toString() {
    return preRelease;
  }

  /** Represents a dot-separated segment of the pre-release string. */
  private static class PreReleaseSegment implements Comparable<PreReleaseSegment> {

    private final String segment;
    private final boolean isNumericOnly;

    public PreReleaseSegment(String segment) {
      this.segment = segment;
      this.isNumericOnly = isNumericOnly(segment);
    }

    @Override
    public int compareTo(PreReleaseSegment other) {
      Preconditions.checkNotNull(other);

      if (this.isNumericOnly) {
        if (other.isNumericOnly) {
          return compareNumericOnly(this, other);
        } else {
          return -1;
        }
      } else {
        if (!other.isNumericOnly) {
          return compareAlphaNumeric(this, other);
        } else {
          return 1;
        }
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      PreReleaseSegment other = (PreReleaseSegment) obj;
      return this.segment.equals(other.segment);
    }

    @Override
    public int hashCode() {
      return Objects.hash(segment);
    }

    @Override
    public String toString() {
      return segment;
    }

    private static int compareAlphaNumeric(PreReleaseSegment first, PreReleaseSegment second) {
      return first.segment.compareTo(second.segment);
    }

    private static int compareNumericOnly(PreReleaseSegment first, PreReleaseSegment second) {
      Integer firstInt = Integer.parseInt(first.segment);
      Integer secondInt = Integer.parseInt(second.segment);
      return firstInt.compareTo(secondInt);
    }

    private static boolean isNumericOnly(String num) {
      return num.matches("[0-9]+");
    }
  }
}
