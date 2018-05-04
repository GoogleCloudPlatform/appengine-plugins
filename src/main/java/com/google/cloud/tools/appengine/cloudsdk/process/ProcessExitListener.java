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

package com.google.cloud.tools.appengine.cloudsdk.process;

import com.google.cloud.tools.appengine.api.AppEngineException;

/**
 * Process listener that can be used with {@link LegacyProcessHandler} to retrieve the process exit
 * code after it terminates.
 */
public interface ProcessExitListener {

  /**
   * This hook will be called immediately <em>after</em> the process has completed execution.
   *
   * @param exitCode the process exit code returned at the end of execution
   */
  void onExit(int exitCode) throws AppEngineException;
}
