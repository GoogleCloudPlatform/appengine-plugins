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

package com.google.cloud.tools.appengine.api.deploy;

import com.google.cloud.tools.appengine.api.AppEngineException;

/**
 * Main interface for staging Java application for App Engine flexible environment before
 * deployment.
 */
public interface AppEngineFlexibleStaging {

  /**
   * Stages a Java JAR/WAR artifact for App Engine flexible environment deployment. Copies app.yaml,
   * Dockerfile and the application artifact to the staging area.
   *
   * @param config Specifies artifacts and staging destination
   * @throws AppEngineException When staging fails
   */
  void stageFlexible(StageFlexibleConfiguration config) throws AppEngineException;
}
