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

import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.operations.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.operations.cloudsdk.InvalidJavaSdkException;
import com.google.cloud.tools.appengine.operations.cloudsdk.internal.process.ProcessBuilderFactory;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DevServersRunnerTest {

  @Rule public TemporaryFolder testFolder = new TemporaryFolder();

  @Mock private CloudSdk sdk;
  @Mock private ProcessHandler processHandler;
  @Mock private ProcessBuilderFactory processBuilderFactory;
  @Mock private ProcessBuilder processBuilder;
  @Mock private Process process;
  @Mock private Map<String, String> processEnv;
  private Path javaExecutablePath;
  private Path appengineToolsJar;
  private Path appengineSdkForJavaPath;
  private Path workingDirectory;
  private Path javaHomePath;

  @Before
  public void setUp() throws IOException {
    javaExecutablePath = testFolder.getRoot().toPath().resolve("java.fake");
    javaHomePath = testFolder.getRoot().toPath().resolve("java-sdk-root");
    appengineToolsJar = testFolder.getRoot().toPath().resolve("appengine.tools");
    appengineSdkForJavaPath = testFolder.getRoot().toPath().resolve("appengine-sdk-root");

    workingDirectory = testFolder.getRoot().toPath();
    when(sdk.getJavaExecutablePath()).thenReturn(javaExecutablePath);
    when(sdk.getAppEngineToolsJar()).thenReturn(appengineToolsJar);
    when(sdk.getAppEngineSdkForJavaPath()).thenReturn(appengineSdkForJavaPath);
    when(sdk.getJavaHomePath()).thenReturn(javaHomePath);

    when(processBuilderFactory.newProcessBuilder()).thenReturn(processBuilder);
    when(processBuilder.start()).thenReturn(process);
    when(processBuilder.environment()).thenReturn(processEnv);
  }

  @Test
  public void testRun()
      throws InvalidJavaSdkException, ProcessHandlerException,
          AppEngineJavaComponentsNotInstalledException, IOException {

    DevAppServerRunner devAppServerRunner =
        new DevAppServerRunner.Factory(processBuilderFactory).newRunner(sdk, processHandler);

    Map<String, String> inputMap = ImmutableMap.of("ENV_KEY", "somevalue");

    devAppServerRunner.run(
        ImmutableList.of("-XsomeArg"),
        ImmutableList.of("args1", "args2"),
        inputMap,
        workingDirectory);

    Mockito.verify(processBuilder)
        .command(
            ImmutableList.of(
                javaExecutablePath.toString(),
                "-XsomeArg",
                "-Dappengine.sdk.root=" + appengineSdkForJavaPath.getParent().toString(),
                "-cp",
                appengineToolsJar.toString(),
                "com.google.appengine.tools.development.DevAppServerMain",
                "args1",
                "args2"));

    Map<String, String> expectedEnv = new HashMap<>(inputMap);
    expectedEnv.put("JAVA_HOME", javaHomePath.toAbsolutePath().toString());

    Mockito.verify(processBuilder).environment();
    Mockito.verify(processEnv).putAll(expectedEnv);
    Mockito.verify(processBuilder).directory(workingDirectory.toFile());
    Mockito.verify(processBuilder).start();
    Mockito.verifyNoMoreInteractions(processBuilder);

    Mockito.verify(processHandler).handleProcess(process);
  }
}
