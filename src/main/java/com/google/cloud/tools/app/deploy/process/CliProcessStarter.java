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

package com.google.cloud.tools.app.deploy.process;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

// kind of a weird wrapper around ProcessBuilder, maybe we don't need to wrap at all?
// does give the shutdown hook option though
public class CliProcessStarter implements ProcessStarter {

  private final boolean disableShutdownHook;
  private final Map<String, String> environment;
  private Process process;

  private CliProcessStarter(Map<String, String> environment, boolean disableShutdownHook) {
    this.environment = environment;
    this.disableShutdownHook = disableShutdownHook;
  }

  @Override
  public Process startProcess(String[] command) throws IOException {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.environment().putAll(environment);
    process = pb.start();

    if (!disableShutdownHook) {
      Runtime.getRuntime().addShutdownHook(new Thread("destroy-process") {
        @Override
        public void run() {
          if (process != null) {
            process.destroy();
          }
        }
      });
    }

    return process;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private boolean disableShutdownHook;
    private Map<String, String> environment = Collections.emptyMap();

    private Builder() {}

    public Builder disableShutdownHook() {
      disableShutdownHook = true;
      return this;
    }

    public Builder environment(Map<String, String> environment) {
      this.environment = environment;
      return this;
    }

    public CliProcessStarter build() {
      return new CliProcessStarter(environment, disableShutdownHook);
    }

  }

}
