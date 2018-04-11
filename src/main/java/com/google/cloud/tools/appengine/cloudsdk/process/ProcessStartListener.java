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

/**
 * Process listener that can be used with {@link LegacyProcessHandler} to retrieve the Process when
 * it's created and started.
 */
public interface ProcessStartListener {

  /**
   * This hook Will be called immediately <em>after</em> the process has been started.
   *
   * @param process the process that has just been started
   */
  void onStart(Process process);
}
