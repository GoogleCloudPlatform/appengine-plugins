/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk;

public interface ProgressListener {
  /**
   * For tasks where total work cannot be determined pass UNKNOWN. update(long) can still be called
   * with increments of work done.
   */
  long UNKNOWN = -1;

  /**
   * Start tracking progress for this task.
   *
   * @param message a message to display on the progress bar
   * @param totalWork a total amount of work this progress listener handles, can special case to -1
   *     where it is an unknown amount of work.
   */
  void start(String message, long totalWork);

  /**
   * Update the progress with an increment of work done since start or update was last called. It is
   * NOT the total work done so far by this task.
   */
  void update(long workDone);

  /** Update the progress message. */
  void update(String message);

  /** Task is complete. */
  void done();

  /**
   * Create a new progressListener child, allocation is how the amount of work of this child has
   * been assigned by the parent. The implementer of child listeners must normalize their values to
   * allocation.
   *
   * <pre>normalizedWorkDone = workDone * allocation / totalWork</pre>
   */
  ProgressListener newChild(long allocation);
}
