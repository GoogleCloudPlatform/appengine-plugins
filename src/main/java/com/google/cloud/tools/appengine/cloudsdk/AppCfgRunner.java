/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessBuilderFactory;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.base.Joiner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class AppCfgRunner {

  private static final Logger logger = Logger.getLogger(AppCfgRunner.class.getName());

  private final CloudSdk sdk;
  private final ProcessBuilderFactory processBuilderFactory;
  private final ProcessHandler processHandler;

  AppCfgRunner(
      CloudSdk sdk, ProcessBuilderFactory processBuilderFactory, ProcessHandler processHandler) {
    this.sdk = sdk;
    this.processBuilderFactory = processBuilderFactory;
    this.processHandler = processHandler;
  }

  /**
   * Executes an App Engine SDK CLI command.
   *
   * @throws AppEngineJavaComponentsNotInstalledException when the App Engine Java components are
   *     not installed in the Cloud SDK
   * @throws InvalidJavaSdkException java not found
   */
  void run(List<String> args)
      throws ProcessHandlerException, AppEngineJavaComponentsNotInstalledException,
          InvalidJavaSdkException, IOException {
    sdk.validateAppEngineJavaComponents();
    sdk.validateJdk();

    // App Engine Java Sdk requires this system property to be set.
    // TODO: perhaps we should send this in directly to the command instead of changing the global
    // state here (see DevAppServerRunner)
    System.setProperty("appengine.sdk.root", sdk.getAppEngineSdkForJavaPath().toString());

    List<String> command = new ArrayList<>();
    command.add(sdk.getJavaExecutablePath().toString());
    command.add("-cp");
    command.add(sdk.getAppEngineToolsJar().toString());
    command.add("com.google.appengine.tools.admin.AppCfg");
    command.addAll(args);

    logger.info("submitting command: " + Joiner.on(" ").join(command));

    ProcessBuilder processBuilder = processBuilderFactory.newProcessBuilder();
    processBuilder.command(command);
    Process process = processBuilder.start();
    processHandler.handleProcess(process);
  }

  static class Factory {
    private final ProcessBuilderFactory processBuilderFactory;

    Factory() {
      this(new ProcessBuilderFactory());
    }

    Factory(ProcessBuilderFactory processBuilderFactory) {
      this.processBuilderFactory = processBuilderFactory;
    }

    AppCfgRunner newRunner(CloudSdk sdk, ProcessHandler processHandler) {
      return new AppCfgRunner(sdk, processBuilderFactory, processHandler);
    }
  }
}
