/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.internal.install;

import java.io.InputStream;

/** Listener on {@link Installer} tasks. */
public interface InstallProcessStreamHandler {

  /**
   * Use this method to display output from the install process.
   *
   * @param stdOut install process standard output stream
   * @param stdErr install process standard error stream
   */
  void handleStreams(InputStream stdOut, InputStream stdErr);
}
