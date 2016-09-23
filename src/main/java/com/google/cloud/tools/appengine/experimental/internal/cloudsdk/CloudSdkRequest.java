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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.experimental.AppEngineRequest;
import com.google.cloud.tools.appengine.experimental.OutputHandler;
import com.google.cloud.tools.appengine.experimental.internal.process.CliProcessManagerProvider;
import com.google.cloud.tools.appengine.experimental.internal.process.io.NullOutputHandler;
import com.google.cloud.tools.appengine.experimental.internal.process.io.StringResultConverter;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * The CloudSdkRequest template, all requests will essentially be instances of this class
 * while providing all the parts to actually initiate a request.
 *
 * @param <R> Request result type
 */
public class CloudSdkRequest<R> implements AppEngineRequest<R> {
  private final CloudSdkProcessFactory processFactory;
  private final CliProcessManagerProvider<R> processManagerProvider;
  private final StringResultConverter<R> resultConverter;
  private OutputHandler outputHandler;
  private boolean mutable = true;

  /**
   * Create a new CloudSdkRequest, there are no subclasses, just configure the providers correctly.
   */
  public CloudSdkRequest(CloudSdkProcessFactory processFactory,
                         CliProcessManagerProvider<R> processManagerProvider,
                         StringResultConverter<R> resultConverter) {
    this.processFactory = processFactory;
    this.processManagerProvider = processManagerProvider;
    this.resultConverter = resultConverter;
    this.outputHandler = new NullOutputHandler();
  }

  @Override
  public Future<R> execute() throws AppEngineException {
    mutable = false;
    try {
      return processManagerProvider
          .manage(processFactory.newProcess(), resultConverter, outputHandler);
    } catch (IOException ex) {
      throw new AppEngineException("Error executing request", ex);
    }
  }

  @Override
  public AppEngineRequest<R> outputHandler(OutputHandler outputHandler) {
    Preconditions.checkState(mutable, "Already executed");
    this.outputHandler = outputHandler;
    return this;
  }

}
