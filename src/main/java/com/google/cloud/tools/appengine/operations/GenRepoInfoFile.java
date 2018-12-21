/*
 * Copyright 2016 Google LLC.
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

package com.google.cloud.tools.appengine.operations;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.GenRepoInfoFileConfiguration;
import com.google.cloud.tools.appengine.operations.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Generate source context files using gcloud, needed by the Cloud Debugger. */
public class GenRepoInfoFile {

  private final GcloudRunner runner;

  GenRepoInfoFile(GcloudRunner runner) {
    this.runner = Preconditions.checkNotNull(runner);
  }

  /**
   * Generates source context files.
   *
   * <p>It is possible for the process to return an error code. In that case, no exception is
   * thrown, but the code must be caught with a {@link
   * com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessExitListener} attached to
   * the {@link CloudSdk} object used to run the command.
   *
   * @param configuration contains the source and output directories
   * @throws AppEngineException when there is an issue running the gcloud process
   */
  public void generate(GenRepoInfoFileConfiguration configuration) throws AppEngineException {
    List<String> arguments = new ArrayList<>();

    arguments.add("beta");
    arguments.add("debug");
    arguments.add("source");
    arguments.add("gen-repo-info-file");
    arguments.addAll(GcloudArgs.get("output-directory", configuration.getOutputDirectory()));
    arguments.addAll(GcloudArgs.get("source-directory", configuration.getSourceDirectory()));

    try {
      runner.run(arguments, null);
    } catch (ProcessHandlerException | IOException ex) {
      throw new AppEngineException(ex);
    }
  }
}
