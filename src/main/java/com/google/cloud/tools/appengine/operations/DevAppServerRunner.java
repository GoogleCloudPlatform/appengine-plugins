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

package com.google.cloud.tools.appengine.operations;

import com.google.cloud.tools.appengine.operations.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.operations.cloudsdk.InvalidJavaSdkException;
import com.google.cloud.tools.appengine.operations.cloudsdk.internal.process.ProcessBuilderFactory;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class DevAppServerRunner {
  private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
  private static final Logger logger = Logger.getLogger(DevAppServerRunner.class.getName());

  private final CloudSdk sdk;
  private final ProcessBuilderFactory processBuilderFactory;
  private final ProcessHandler processHandler;

  DevAppServerRunner(
      CloudSdk sdk, ProcessBuilderFactory processBuilderFactory, ProcessHandler processHandler) {
    this.sdk = sdk;
    this.processBuilderFactory = processBuilderFactory;
    this.processHandler = processHandler;
  }

  /**
   * Uses the process runner to execute the classic Java SDK devappsever command.
   *
   * @param jvmArgs the arguments to pass to the Java Virtual machine that launches the devappserver
   * @param args the arguments to pass to devappserver
   * @param environment the environment to set on the devappserver process
   * @param workingDirectory if null then the working directory of current Java process.
   * @throws ProcessHandlerException when process runner encounters an error
   * @throws AppEngineJavaComponentsNotInstalledException Cloud SDK is installed but App Engine Java
   *     components are not
   * @throws InvalidJavaSdkException when the specified JDK does not exist
   */
  public void runV1(
      List<String> jvmArgs,
      List<String> args,
      Map<String, String> environment,
      @Nullable Path workingDirectory)
      throws ProcessHandlerException, AppEngineJavaComponentsNotInstalledException,
          InvalidJavaSdkException, IOException {
    sdk.validateAppEngineJavaComponents();
    sdk.validateJdk();

    List<String> command = new ArrayList<>();

    command.add(sdk.getJavaExecutablePath().toAbsolutePath().toString());

    command.addAll(jvmArgs);
    command.add(
        "-Dappengine.sdk.root="
            + sdk.getAppEngineSdkForJavaPath().getParent().toAbsolutePath().toString());
    command.add("-cp");
    command.add(sdk.getAppEngineToolsJar().toAbsolutePath().toString());
    command.add("com.google.appengine.tools.development.DevAppServerMain");

    command.addAll(args);

    logger.info("submitting command: " + Joiner.on(" ").join(command));

    Map<String, String> devServerEnvironment = Maps.newHashMap(environment);
    devServerEnvironment.put("JAVA_HOME", sdk.getJavaHomePath().toAbsolutePath().toString());

    ProcessBuilder processBuilder = processBuilderFactory.newProcessBuilder();
    processBuilder.command(command);
    if (workingDirectory != null) {
      processBuilder.directory(workingDirectory.toFile());
    }
    processBuilder.environment().putAll(devServerEnvironment);
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

    DevAppServerRunner newRunner(CloudSdk sdk, ProcessHandler processHandler) {
      return new DevAppServerRunner(sdk, processBuilderFactory, processHandler);
    }
  }
}
