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

package com.google.cloud.tools.appengine.cloudsdk.internal.process;

import com.google.cloud.tools.appengine.cloudsdk.process.ProcessExitListener;
import javax.annotation.Nullable;

/** A {@link ProcessExitListener} that records the most recent process exit code it encountered. */
public class ExitCodeRecorderProcessExitListener implements ProcessExitListener {

  @Nullable private Integer mostRecentExitCode;

  @Override
  public void onExit(int exitCode) {
    this.mostRecentExitCode = exitCode;
  }

  @Nullable
  public Integer getMostRecentExitCode() {
    return mostRecentExitCode;
  }
}
