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

import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessBuilderFactory;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppCfgRunnerTest {

  @Rule public TemporaryFolder testFolder = new TemporaryFolder();

  @Mock private CloudSdk sdk;
  @Mock private ProcessBuilderFactory processBuilderFactory;
  @Mock private ProcessBuilder processBuilder;
  @Mock private ProcessHandler processHandler;
  @Mock private Process process;
  private Path javaExecutablePath;
  private Path appengineToolsJar;
  private Path appengineJavaSdkPath;

  @Before
  public void setUp() throws IOException {
    javaExecutablePath = testFolder.getRoot().toPath().resolve("java.fake");
    appengineToolsJar = testFolder.getRoot().toPath().resolve("appengine.tools");
    appengineJavaSdkPath = testFolder.getRoot().toPath().resolve("appengine-sdk-root");
    when(sdk.getJavaExecutablePath()).thenReturn(javaExecutablePath);
    when(sdk.getAppEngineToolsJar()).thenReturn(appengineToolsJar);
    when(sdk.getAppEngineSdkForJavaPath()).thenReturn(appengineJavaSdkPath);

    when(processBuilderFactory.newProcessBuilder()).thenReturn(processBuilder);
    when(processBuilder.start()).thenReturn(process);
  }

  @Test
  public void testRun()
      throws InvalidJavaSdkException, ProcessHandlerException,
          AppEngineJavaComponentsNotInstalledException, IOException {
    AppCfgRunner appCfgRunner =
        new AppCfgRunner.Factory(processBuilderFactory).newRunner(sdk, processHandler);

    appCfgRunner.run(ImmutableList.of("some", "command"));

    Mockito.verify(processBuilder)
        .command(
            ImmutableList.of(
                javaExecutablePath.toString(),
                "-cp",
                appengineToolsJar.toString(),
                "com.google.appengine.tools.admin.AppCfg",
                "some",
                "command"));
    Mockito.verify(processBuilder).start();
    Mockito.verifyNoMoreInteractions(processBuilder);

    Mockito.verify(processHandler).handleProcess(process);
    Assert.assertEquals(appengineJavaSdkPath.toString(), System.getProperty("appengine.sdk.root"));
  }
}
