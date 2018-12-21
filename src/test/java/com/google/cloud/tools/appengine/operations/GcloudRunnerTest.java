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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.operations.cloudsdk.internal.process.ProcessBuilderFactory;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
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
public class GcloudRunnerTest {

  @Rule public TemporaryFolder testFolder = new TemporaryFolder();

  @Mock private CloudSdk sdk;
  private Path gcloudPath;
  private Path credentialFile;
  private Path workingDirectory;
  @Mock private ProcessHandler processHandler;
  @Mock private ProcessBuilderFactory processBuilderFactory;
  @Mock private ProcessBuilder processBuilder;
  @Mock private Process process;
  @Mock private Map<String, String> processEnv;

  @Before
  public void setUp() throws IOException {
    credentialFile = testFolder.newFile("credential.file").toPath();
    gcloudPath = testFolder.getRoot().toPath().resolve("gcloud");
    workingDirectory = testFolder.getRoot().toPath();
    when(sdk.getGCloudPath()).thenReturn(gcloudPath);

    when(processBuilderFactory.newProcessBuilder()).thenReturn(processBuilder);
    when(processBuilder.start()).thenReturn(process);
    when(processBuilder.environment()).thenReturn(processEnv);
  }

  @Test
  public void testRun_builtFromFactory()
      throws CloudSdkOutOfDateException, CloudSdkNotFoundException, ProcessHandlerException,
          CloudSdkVersionFileException, IOException {
    GcloudRunner gcloudRunner =
        new GcloudRunner.Factory(processBuilderFactory)
            .newRunner(
                sdk,
                "intellij", // metrics env
                "99", // metrics env version
                credentialFile, // credential file
                "some-format", // output format
                "always", // show structured logs
                processHandler);

    gcloudRunner.run(ImmutableList.of("some", "command"), workingDirectory);

    Mockito.verify(processBuilder).environment();
    Mockito.verify(processEnv).putAll(gcloudRunner.getGcloudCommandEnvironment());
    Mockito.verify(processBuilder).directory(workingDirectory.toFile());

    Mockito.verify(processHandler).handleProcess(process);
    Mockito.verify(processBuilder)
        .command(
            ImmutableList.of(
                gcloudPath.toString(),
                "some",
                "command",
                "--format",
                "some-format",
                "--credential-file-override",
                credentialFile.toAbsolutePath().toString()));
    Mockito.verify(processBuilder).start();
    Mockito.verifyNoMoreInteractions(processBuilder);

    Mockito.verify(processHandler).handleProcess(process);
  }

  @Test
  public void testGcloudCommandEnvironment() {
    GcloudRunner gcloudRunner =
        new GcloudRunner(
            sdk,
            "intellij", // metrics env
            "99", // metrics env version
            mock(Path.class), // credential file
            "irrelevant-to-test", // output format
            "always", // show structured logs
            mock(ProcessBuilderFactory.class),
            mock(ProcessHandler.class));

    Map<String, String> env = gcloudRunner.getGcloudCommandEnvironment();
    assertEquals("0", env.get("CLOUDSDK_APP_USE_GSUTIL"));
    assertEquals("always", env.get("CLOUDSDK_CORE_SHOW_STRUCTURED_LOGS"));
    assertEquals("intellij", env.get("CLOUDSDK_METRICS_ENVIRONMENT"));
    assertEquals("99", env.get("CLOUDSDK_METRICS_ENVIRONMENT_VERSION"));
    assertEquals("1", env.get("CLOUDSDK_CORE_DISABLE_PROMPTS"));
  }
}
