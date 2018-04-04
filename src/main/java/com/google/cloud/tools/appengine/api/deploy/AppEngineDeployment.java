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

/** Main interface for deploying to App Engine. */
public interface AppEngineDeployment {

  /**
   * Deploy the local code and/or configuration of your app to App Engine.
   *
   * @param config Deployment configuration
   * @throws AppEngineException When deployment process fails
   */
  void deploy(DeployConfiguration config) throws AppEngineException;

  /** Deploy cron configuration to App Engine. */
  void deployCron(DeployProjectConfigurationConfiguration config) throws AppEngineException;

  /** Deploy dos configuration to App Engine. */
  void deployDos(DeployProjectConfigurationConfiguration config) throws AppEngineException;

  /** Deploy dispatch configuration to App Engine. */
  void deployDispatch(DeployProjectConfigurationConfiguration config) throws AppEngineException;

  /** Deploy index configuration to App Engine. */
  void deployIndex(DeployProjectConfigurationConfiguration config) throws AppEngineException;

  /** Deploy queue configuration to App Engine. */
  void deployQueue(DeployProjectConfigurationConfiguration config) throws AppEngineException;
}
