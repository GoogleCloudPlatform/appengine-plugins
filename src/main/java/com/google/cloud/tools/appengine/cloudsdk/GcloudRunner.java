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

import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessBuilderFactory;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class GcloudRunner {

  private static final Logger logger = Logger.getLogger(GcloudRunner.class.getName());

  private final CloudSdk sdk;
  private final String metricsEnvironment;
  private final String metricsEnvironmentVersion;
  private final File credentialFile;
  private final String outputFormat;
  private final String showStructuredLogs;
  private final ProcessBuilderFactory processBuilderFactory;
  private final ProcessHandler processHandler;

  GcloudRunner(
      CloudSdk sdk,
      String metricsEnvironment,
      String metricsEnvironmentVersion,
      File credentialFile,
      String outputFormat,
      String showStructuredLogs,
      ProcessBuilderFactory processBuilderFactory,
      ProcessHandler processHandler) {
    this.sdk = sdk;
    this.metricsEnvironment = metricsEnvironment;
    this.metricsEnvironmentVersion = metricsEnvironmentVersion;
    this.credentialFile = credentialFile;
    this.outputFormat = outputFormat;
    this.showStructuredLogs = showStructuredLogs;
    this.processBuilderFactory = processBuilderFactory;
    this.processHandler = processHandler;
  }

  void run(List<String> arguments, File workingDirectory)
      throws ProcessHandlerException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException, IOException {

    sdk.validateCloudSdk();

    List<String> command = new ArrayList<>();
    command.add(sdk.getGCloudPath().toAbsolutePath().toString());

    command.addAll(arguments);
    command.addAll(GcloudArgs.get("format", outputFormat));

    if (credentialFile != null) {
      command.addAll(GcloudArgs.get("credential-file-override", credentialFile));
    }

    logger.info("submitting command: " + Joiner.on(" ").join(command));

    ProcessBuilder processBuilder = processBuilderFactory.newProcessBuilder();
    processBuilder.command(command);
    processBuilder.directory(workingDirectory);
    processBuilder.environment().putAll(getGcloudCommandEnvironment());
    Process process = processBuilder.start();
    processHandler.handleProcess(process);
  }

  @VisibleForTesting
  Map<String, String> getGcloudCommandEnvironment() {
    Map<String, String> environment = Maps.newHashMap();
    if (credentialFile != null) {
      environment.put("CLOUDSDK_APP_USE_GSUTIL", "0");
    }
    if (metricsEnvironment != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT", metricsEnvironment);
    }
    if (metricsEnvironmentVersion != null) {
      environment.put("CLOUDSDK_METRICS_ENVIRONMENT_VERSION", metricsEnvironmentVersion);
    }
    if (showStructuredLogs != null) {
      environment.put("CLOUDSDK_CORE_SHOW_STRUCTURED_LOGS", showStructuredLogs);
    }
    // This is to ensure IDE credentials get correctly passed to the gcloud commands, in Windows.
    // It's a temporary workaround until a fix is released.
    // https://github.com/GoogleCloudPlatform/google-cloud-intellij/issues/985
    if (System.getProperty("os.name").contains("Windows")) {
      environment.put("CLOUDSDK_APP_NUM_FILE_UPLOAD_PROCESSES", "1");
    }

    environment.put("CLOUDSDK_CORE_DISABLE_PROMPTS", "1");

    return environment;
  }

  static class Factory {
    private final ProcessBuilderFactory processBuilderFactory;

    Factory() {
      this(new ProcessBuilderFactory());
    }

    Factory(ProcessBuilderFactory processBuilderFactory) {
      this.processBuilderFactory = processBuilderFactory;
    }

    GcloudRunner newRunner(
        CloudSdk sdk,
        String metricsEnvironment,
        String metricsEnvironmentVersion,
        File credentialFile,
        String outputFormat,
        String showStructuredLogs,
        ProcessHandler processHandler) {
      return new GcloudRunner(
          sdk,
          metricsEnvironment,
          metricsEnvironmentVersion,
          credentialFile,
          outputFormat,
          showStructuredLogs,
          processBuilderFactory,
          processHandler);
    }
  }
}
