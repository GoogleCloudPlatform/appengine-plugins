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

package com.google.cloud.tools.libraries.json;

import javax.annotation.Nullable;

/** Holds details about a single Cloud library client's Maven artifact. */
public final class CloudLibraryClientMavenCoordinates {

  @Nullable private String groupId;
  @Nullable private String artifactId;
  @Nullable private String version;

  /** Prevents direct instantiation. GSON instantiates these objects using dark magic. */
  private CloudLibraryClientMavenCoordinates() {}

  /** Returns the group ID of this client's Maven artifact. */
  @Nullable
  public String getGroupId() {
    return groupId;
  }

  /** Returns the artifact ID of this client's Maven artifact. */
  @Nullable
  public String getArtifactId() {
    return artifactId;
  }

  /** Returns the version of this client's Maven artifact. */
  @Nullable
  public String getVersion() {
    return version;
  }
}
