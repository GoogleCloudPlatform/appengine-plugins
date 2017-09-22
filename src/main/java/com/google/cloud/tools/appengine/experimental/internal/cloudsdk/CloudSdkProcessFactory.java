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

package com.google.cloud.tools.appengine.experimental.internal.cloudsdk;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/** Factory for raw process creation. */
public class CloudSdkProcessFactory {

  private final List<String> command;
  private final Map<String, String> environment;
  private final Logger logger = Logger.getLogger(CloudSdkProcessFactory.class.getName());
  private final ProcessBuilder processBuilder = new ProcessBuilder();

  public CloudSdkProcessFactory(List<String> command, Map<String, String> environment) {
    this.command = command;
    this.environment = environment;
  }

  /** Create a new raw process. */
  public Process newProcess() throws IOException {
    logger.info(command.toString());

    processBuilder.command(command);
    processBuilder.environment().putAll(environment);

    return processBuilder.start();
  }
}
