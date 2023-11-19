/*
 * Copyright 2017 Google LLC.
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

package com.google.cloud.tools.managedcloudsdk.command;

import javax.annotation.Nullable;

/** Exception when sdk command fails. */
public class CommandExitException extends Exception {
  private final int exitCode;
  @Nullable private final String errorLog;

  /**
   * Create a new exception.
   *
   * @param exitCode the process exit code
   * @param errorLog additional loggable error information
   */
  public CommandExitException(int exitCode, @Nullable String errorLog) {
    super("Process failed with exit code: " + exitCode + (errorLog != null ? "\n" + errorLog : ""));
    this.exitCode = exitCode;
    this.errorLog = errorLog;
  }

  /**
   * Create a new exception.
   *
   * @param exitCode the process exit code
   */
  public CommandExitException(int exitCode) {
    this(exitCode, null);
  }

  public int getExitCode() {
    return exitCode;
  }

  @Nullable
  public String getErrorLog() {
    return errorLog;
  }
}
