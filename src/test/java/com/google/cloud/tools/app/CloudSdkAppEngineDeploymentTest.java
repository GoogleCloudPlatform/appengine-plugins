/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.app;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.DeployConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link CloudSdkAppEngineDeployment}
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDeploymentTest {

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  private File appYaml1;
  private File appYaml2;

  @Mock
  private CloudSdk sdk;


  @Before
  public void setUp() throws IOException {
    appYaml1 = tmpDir.newFile("app1.yaml");
    appYaml2 = tmpDir.newFile("app2.yaml");
  }

  @Test
  public void testNewDeployAction_allFlags() throws AppEngineException, ProcessRunnerException {

    DeployConfiguration configuration = mock(DeployConfiguration.class);
    when(configuration.getDeployables()).thenReturn(Arrays.asList(appYaml1));
    when(configuration.getBucket()).thenReturn("gs://a-bucket");
    when(configuration.getDockerBuild()).thenReturn("cloud");
    when(configuration.isForce()).thenReturn(true);
    when(configuration.getImageUrl()).thenReturn("imageUrl");
    when(configuration.isPromote()).thenReturn(false);
    when(configuration.getServer()).thenReturn("appengine.google.com");
    when(configuration.isStopPreviousVersion()).thenReturn(true);
    when(configuration.getVersion()).thenReturn("v1");

    CloudSdkAppEngineDeployment deployment = new CloudSdkAppEngineDeployment(sdk);

    deployment.deploy(configuration);

    List<String> expectedCommand = ImmutableList
        .of("cloud", appYaml1.toString(), "--bucket", "gs://a-bucket", "--docker-build",
            "cloud", "--force", "--image-url", "imageUrl", "--server", "appengine.google.com",
            "--stop-previous-version", "--version", "v1", "--quiet");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }
/*
  @Test
  public void testNewDeployAction_noFlags() throws ExecutorException {

    DeployConfiguration configuration = DefaultDeployConfiguration.newBuilder(appYaml1)
        .build();

    DeployAction action = new DeployAction(configuration, appExecutor);

    List<String> expectedCommand = ImmutableList.of("cloud", appYaml1.toString(), "--quiet");

    action.execute();
    verify(appExecutor, times(1)).runCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_multipleDeployables() throws ExecutorException {

    DeployConfiguration configuration = DefaultDeployConfiguration.newBuilder(appYaml1, appYaml2)
        .build();

    DeployAction action = new DeployAction(configuration, appExecutor);

    List<String> expectedCommand = ImmutableList
        .of("cloud", appYaml1.toString(), appYaml2.toString(), "--quiet");

    action.execute();
    verify(appExecutor, times(1)).runCommand(eq(expectedCommand));
  }
  */
}
