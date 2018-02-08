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

import com.google.common.base.Preconditions;

/** Default implementation of a child listener. Should satisfy the simple use case. */
public class ChildProgressListener implements ProgressListener {

  private final ProgressListener parent;
  private boolean isStarted = false;
  private boolean isDone = false;

  private long totalWork;
  private long totalWorkDone = 0;

  private final long totalAllocatedWork;
  private long totalReportedAllocatedWorkDone = 0;

  /**
   * Create a child progress listener that performs a subtask for a parent.
   *
   * @param parent the parent ProgressListener that created this child
   * @param totalAllocatedWork the amount of work from the parent this child is expected to report.
   */
  public ChildProgressListener(ProgressListener parent, long totalAllocatedWork) {
    this.parent = parent;
    this.totalAllocatedWork = totalAllocatedWork;
  }

  @Override
  public void start(String message, long totalWork) {
    Preconditions.checkArgument(!isStarted && !isDone);
    isStarted = true;
    parent.update(message);
    this.totalWork = totalWork;
  }

  @Override
  public void update(long workDone) {
    Preconditions.checkArgument(isStarted && !isDone);
    totalWorkDone += workDone;

    if (totalWork == UNKNOWN) {
      // there is room here for an implementation of an unknown amount of work
      // algorithm, but for now we just ignore any update.
      return;
    }

    long totalAllocatedWorkDone = totalWorkDone * totalAllocatedWork / totalWork;

    if (totalAllocatedWorkDone > totalReportedAllocatedWorkDone) {
      parent.update(totalAllocatedWorkDone - totalReportedAllocatedWorkDone);
      totalReportedAllocatedWorkDone = totalAllocatedWorkDone;
    }
  }

  @Override
  public void update(String message) {
    Preconditions.checkArgument(isStarted && !isDone);
    parent.update(message);
  }

  @Override
  public void done() {
    Preconditions.checkArgument(isStarted && !isDone);
    isDone = true;
    if (totalReportedAllocatedWorkDone < totalAllocatedWork) {
      parent.update(totalAllocatedWork - totalReportedAllocatedWorkDone);
    }
  }

  @Override
  public ProgressListener newChild(long allocation) {
    return new ChildProgressListener(this, allocation);
  }
}
