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

package com.google.cloud.tools.appengine.api.deploy;

import java.io.File;
import javax.annotation.Nullable;

/** Configuration for {@link AppEngineFlexibleStaging#stageFlexible(StageFlexibleConfiguration)}. */
public interface StageFlexibleConfiguration {

  /** Directory containing {@code app.yaml}. */
  File getAppEngineDirectory();

  /** Directory containing {@code Dockerfile} and other resources used by it. */
  @Nullable
  File getDockerDirectory();

  /** Artifact to deploy such as WAR or JAR. */
  File getArtifact();

  /**
   * Directory where {@code app.yaml}, files in docker directory, and the artifact to deploy will be
   * copied for deploying.
   */
  File getStagingDirectory();
}
