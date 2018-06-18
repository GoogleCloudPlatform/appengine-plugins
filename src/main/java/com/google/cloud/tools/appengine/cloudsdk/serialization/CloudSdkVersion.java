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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Version of the Cloud SDK, which follows the <a href="http://semver.org/spec/v2.0.0.html">Semantic
 * Version 2.0.0 spec</a>.
 */
public class CloudSdkVersion implements Comparable<CloudSdkVersion> {

  private static final Pattern SEMVER_PATTERN = Pattern.compile(getSemVerRegex());

  private final String version;
  @VisibleForTesting final int majorVersion;
  @VisibleForTesting final int minorVersion;
  @VisibleForTesting final int patchVersion;

  @Nullable
  private final CloudSdkVersionPreRelease preRelease; // optional pre-release component of version

  @Nullable private final String buildIdentifier; // optional build ID component of version string

  /**
   * Constructs a new CloudSdkVersion.
   *
   * @param version the semantic version string
   * @throws IllegalArgumentException if the argument is not a valid semantic version string
   */
  public CloudSdkVersion(String version) {
    Preconditions.checkNotNull(version, "Null version");
    Preconditions.checkArgument(!version.isEmpty(), "empty version");

    if ("HEAD".equals(version)) {
      majorVersion = -1;
      minorVersion = -1;
      patchVersion = -1;
      preRelease = null;
      buildIdentifier = null;
    } else {
      Matcher matcher = SEMVER_PATTERN.matcher(version);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(
            String.format("Pattern \"%s\" is not a valid CloudSdkVersion.", version));
      }

      majorVersion = Integer.parseInt(matcher.group("major"));
      minorVersion = Integer.parseInt(matcher.group("minor"));
      patchVersion = Integer.parseInt(matcher.group("patch"));

      preRelease =
          matcher.group("prerelease") != null
              ? new CloudSdkVersionPreRelease(matcher.group("prerelease"))
              : null;
      buildIdentifier = matcher.group("build");
    }
    this.version = version;
  }

  private static String getSemVerRegex() {
    // Only digits, with no leading zeros.
    String digits = "(?:0|[1-9][0-9]*)";
    // Digits, letters and dashes
    String alphaNum = "[-0-9A-Za-z]+";
    // This is an alphanumeric string that must have at least one letter (or else it would be
    // considered digits).
    String strictAlphaNum = "[-0-9A-Za-z]*[-A-Za-z]+[-0-9A-Za-z]*";

    String preReleaseIdentifier = "(?:" + digits + "|" + strictAlphaNum + ")";
    String preRelease = "(?:" + preReleaseIdentifier + "(?:\\." + preReleaseIdentifier + ")*)";
    String build = "(?:" + alphaNum + "(?:\\." + alphaNum + ")*)";

    return "^(?<major>"
        + digits
        + ")\\.(?<minor>"
        + digits
        + ")\\.(?<patch>"
        + digits
        + ")"
        + "(?:\\-(?<prerelease>"
        + preRelease
        + "))?(?:\\+(?<build>"
        + build
        + "))?$";
  }

  @Override
  public String toString() {
    return version;
  }

  /**
   * Compares this to another CloudSdkVersion, per the Semantic Versioning 2.0.0 specification.
   *
   * <p>Note that the build identifier field is excluded for comparison. Thus, <code>
   * new CloudSdkVersion("0.0.1+v1").compareTo(new CloudSdkVersion("0.0.1+v2")) == 0</code>
   */
  @Override
  public int compareTo(CloudSdkVersion other) {
    Preconditions.checkNotNull(other);

    if ("HEAD".equals(version) && !"HEAD".equals(other.version)) {
      return 1;
    } else if (!"HEAD".equals(version) && "HEAD".equals(other.version)) {
      return -1;
    }

    // First, compare required fields
    List<Integer> mine = ImmutableList.of(majorVersion, minorVersion, patchVersion);
    List<Integer> others =
        ImmutableList.of(other.majorVersion, other.minorVersion, other.patchVersion);
    for (int i = 0; i < mine.size(); i++) {
      int result = mine.get(i).compareTo(others.get(i));
      if (result != 0) {
        return result;
      }
    }

    // Compare pre-release components
    if (preRelease != null && other.getPreRelease() != null) {
      return preRelease.compareTo(other.getPreRelease());
    }

    // A SemVer with a pre-release string has lower precedence than one without.
    if (preRelease == null && other.getPreRelease() != null) {
      return 1;
    }
    if (preRelease != null && other.getPreRelease() == null) {
      return -1;
    }

    return 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(majorVersion, minorVersion, patchVersion, preRelease, buildIdentifier);
  }

  /**
   * Compares this to another CloudSdkVersion for equality. Unlike compareTo, this method considers
   * two CloudSdkVersions to be equal if all of their components are identical, including their
   * build identifiers. Thus, "0.0.1+v1" is not equal to "0.0.1+v2".
   */
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
    CloudSdkVersion otherVersion = (CloudSdkVersion) obj;

    return Objects.equals(majorVersion, otherVersion.majorVersion)
        && Objects.equals(minorVersion, otherVersion.minorVersion)
        && Objects.equals(patchVersion, otherVersion.patchVersion)
        && Objects.equals(preRelease, otherVersion.preRelease)
        && Objects.equals(buildIdentifier, otherVersion.buildIdentifier);
  }

  @Nullable
  protected CloudSdkVersionPreRelease getPreRelease() {
    return preRelease;
  }

  /**
   * Returns the version's build identifier - an optional component of the semantic version which
   * signifies a specific build release. Note that the build identifier is never considered for
   * comparison or equality testing.
   */
  @Nullable
  public String getBuildIdentifier() {
    return buildIdentifier;
  }
}
