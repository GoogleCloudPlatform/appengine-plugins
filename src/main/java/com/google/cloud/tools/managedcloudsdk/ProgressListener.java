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
  void start(String message, long totalWork);

  void update(long workDone); // not cumulative

  void done();

  /**
   * Create a new progressListener child, allocation is how the amount of work of this child has
   * been assigned by the parent. The implementer of child listeners must normalize their values to
   * allocation.
   *
   * <pre>totalWorkDoneNormalized = totalWorkDone * allocation / totalWork</pre>
   */
  ProgressListener newChild(long allocation);
}
