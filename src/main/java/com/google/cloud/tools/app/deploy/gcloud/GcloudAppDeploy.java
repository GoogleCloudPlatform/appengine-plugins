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

package com.google.cloud.tools.app.deploy.gcloud;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.DeployConfiguration;
import com.google.cloud.tools.app.deploy.Deploy;
import com.google.cloud.tools.app.deploy.Deploy.DeployRequest;
import com.google.cloud.tools.app.deploy.DeployResult;
import com.google.cloud.tools.app.deploy.process.CliProcessStarter;
import com.google.cloud.tools.app.deploy.process.NullOutputHandler;
import com.google.cloud.tools.app.deploy.process.OutputHandler;
import com.google.cloud.tools.app.deploy.process.ProcessStarter;
import com.google.cloud.tools.app.deploy.process.StreamConsumer;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import java.io.IOException;
import java.util.concurrent.Future;

public class GcloudAppDeploy implements DeployRequest {
  private final CloudSdk sdk;
  private final ConfigurationTranslator<DeployConfiguration> configurationTranslator;
  private final ProcessStarter processStarter;
  private final DeployConfiguration config;
  private OutputHandler outputHandler;

  private GcloudAppDeploy(CloudSdk sdk,
      ConfigurationTranslator<DeployConfiguration> configurationTranslator,
      ProcessStarter processStarter,
      DeployConfiguration config) {
    this.sdk = sdk;
    this.processStarter = processStarter;
    this.configurationTranslator = configurationTranslator;
    this.outputHandler = new NullOutputHandler();
    this.config = config;
  }

  @Override
  public Future<DeployResult> deploy() {
    try {
      String[] options = configurationTranslator.translate(config);
      String[] command = makeAppDeployCommand(sdk, options); // or something, this is made up

      Process process = processStarter.startProcess(command);
      StreamConsumer.startNewConsumer(process.getErrorStream(), outputHandler);

      return new GcloudFuture<DeployResult>(process, new DeployResultConverter());

    } catch(IOException e) {
      throw new AppEngineException("Error deploying", e);
    }
  }

  @Override
  public DeployRequest setStatusUpdater(OutputHandler handler) {
    this.outputHandler = handler;
    return this;
  };

  // this will probably be similar for all commands, but I don't know how to
  // abstract it out yet, or if I even should?
  public static class Builder implements Deploy.DeployRequestFactory {
    private CloudSdk sdk;
    // maybe these two don't have to come from the environment at all?
    private ProcessStarter processStarter;
    private ConfigurationTranslator<DeployConfiguration> configurationTranslator;

    public Builder(CloudSdk sdk) {
      this.sdk = sdk;
      // getEnvironment doesn't exist
      this.processStarter = CliProcessStarter.newBuilder().environment(sdk.getEnvironment()).build();
      this.configurationTranslator = new GcloudAppDeployTranslator();
    }

    @Override
    public DeployRequest newDeploymentRequest(DeployConfiguration config) {
      return new GcloudAppDeploy(sdk, configurationTranslator, processStarter, config);
    }
  }
}
