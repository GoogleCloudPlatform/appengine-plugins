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

package com.google.cloud.tools.appengine.api.devserver;

import com.google.cloud.tools.appengine.api.AppEngineException;

/** Interface for running App Engine Local Development Server. */
public interface AppEngineDevServer {

  /**
   * Runs the App Engine Development Server synchronously.
   *
   * @param config Run configuration
   * @throws AppEngineException When the server exits abnormally.
   */
  void run(RunConfiguration config) throws AppEngineException;

  /**
   * Stops a running App Engine Development Server.
   *
   * @param config Identity of the server to stop.
   * @throws AppEngineException When the operation encounters an error.
   */
  void stop(StopConfiguration config) throws AppEngineException;
}
